package com.trader.trader.services;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.trader.trader.models.*;
import com.trader.trader.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class Kraken {
    private HttpClient httpClient;
    private String domain="https://api.kraken.com";
    public Gson gson;
    private final HistoricalRepository historicalRepository;
    private final OHLCRepository ohlcRepository;
    private final LogsRepository logsRepository;
    private final MacdRepository macdRepository;
    private final SignalRepository signalRepository;
    private boolean isTradesEnabled=true;
    private boolean isOHLCEnabled=true;
    public String timestamp="1682625671396801743";
    public boolean calculated=false;
    @Value("$[api.key]")
    private String apiKey;
    @Value("$[api.secret]")
    private String apiSecret;

    @Autowired
    public Kraken(HistoricalRepository historicalRepository, OHLCRepository ohlcRepository,
                  LogsRepository logsRepository, MacdRepository macdRepository, SignalRepository signalRepository) {
        this.historicalRepository = historicalRepository;
        this.ohlcRepository = ohlcRepository;
        this.logsRepository = logsRepository;
        this.macdRepository = macdRepository;
        this.signalRepository = signalRepository;
        httpClient=HttpClient.newHttpClient();
        gson=new Gson();
    }

    public void getOHLCData(String pair, String interval, String since) throws URISyntaxException, IOException, InterruptedException {
        String url=getDomain()+"/0/public/OHLC"+"?pair="+pair+"&interval="+interval+"&since="+since;
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(
                        new URI(url))
                .header("User-Agent", "Kraken API Java Client")
                .GET()
                .build();
        HttpResponse<String> resp = getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        writeOHLCToDB(gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject());
    }

    public List<OHLC> getOHLCData(String pair, String interval) throws URISyntaxException, IOException, InterruptedException {
        String url=getDomain()+"/0/public/OHLC"+"?pair="+pair+"&interval="+interval;
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(
                        new URI(url))
                .headers("User-Agent", "Kraken API Java Client")
                .GET()
                .build();
        HttpResponse<String> resp = getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (!gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject().get("error").getAsJsonArray().isEmpty()) {
            System.out.println("ERROR SHUTTING DOWN");
            Logs log = new Logs(gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject().get("error").getAsString()
                    , LocalDateTime.now());
            logsRepository.save(log);
            isOHLCEnabled=false;
            return new ArrayList<>();
        }
        return writeOHLCToDB(gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject());

    }

    public String getTradesHistory(String pair, String since) throws URISyntaxException, IOException, InterruptedException {

        String url=getDomain()+"/0/public/Trades"+"?pair="+pair+"&since="+since;
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(
                        new URI(url))
                .headers("User-Agent", "Kraken API Java Client")
                .GET()
                .build();
        HttpResponse<String> resp = getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (!gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject().get("error").getAsJsonArray().isEmpty()) {
            System.out.println("ERROR SHUTTING DOWN");
            Logs log = new Logs(gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject().get("error").getAsString()
                    , LocalDateTime.now());
            logsRepository.save(log);
            isTradesEnabled=false;
            return "N/A";
        }
        System.out.println(resp.statusCode());
        System.out.println(resp.body());
        writeTradesToDB(gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject());


        return gson.fromJson(resp.body(), JsonObject.class).get("result").getAsJsonObject().get("last").getAsString();
    }

    public void writeTradesToDB(JsonObject resp) {
        int count=0;
        JsonArray arr = resp.get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray();
        String last = resp.get("result").getAsJsonObject().get("last").getAsString();
        if (last.equals(timestamp)) {
            System.out.println("Completed");
            setTradesEnabled(false);
            return;
        }
        try {
            for (int i=0; i<arr.size(); i++) {
                JsonArray curr = arr.get(i).getAsJsonArray();
                Long timestamp = Long.parseLong(curr.get(2).getAsString().split("\\.")[0]);
                Date timestampToTime = new Date(timestamp*1000);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(timestampToTime);
                Date date = sdf.parse(format);
                LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                if (!historicalRepository.existsByDate(localDateTime.withSecond(0))) {
                    Historical data = new Historical(curr.get(1).getAsDouble(), localDateTime.withSecond(0), curr.get(0).getAsDouble(), timestamp.toString(),
                            last, curr.get(3).getAsString().charAt(0));
                    historicalRepository.save(data);
                    count++;
                }
            }
        }
        catch (Exception e) {
            Logs log = new Logs(e.getMessage(), LocalDateTime.now());
            logsRepository.save(log);
            e.getStackTrace();
            System.out.println(e.getMessage());
            setTradesEnabled(false);
        }
        System.out.println("Rows Added: "+count);

    }

    public List<OHLC> writeOHLCToDB(JsonObject resp) {
        JsonArray arr = resp.get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray();
        long start = System.currentTimeMillis();
        List<OHLC> addedOHLC = new ArrayList<>();
        try {
            for (int i=0; i<arr.size()-1; i++) {

                JsonArray curr = arr.get(i).getAsJsonArray();
                Long timestamp = Long.parseLong(curr.get(0).getAsString());
                Date timeStampToTime = new Date(timestamp*1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(timeStampToTime);
                Date date = sdf.parse(format);
                LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();



                OHLC ohlc = new OHLC(ldt, curr.get(1).getAsDouble(), curr.get(4).getAsDouble(), curr.get(2).getAsDouble(), curr.get(3).getAsDouble(), curr.get(6).getAsDouble());
                if (!ohlcRepository.existsByDate(ldt)) {
                    ohlcRepository.save(ohlc);
                    addedOHLC.add(ohlc);
                }

            }
        }
        catch (Exception e) {
            Logs log = new Logs(e.getMessage(), LocalDateTime.now());
            logsRepository.save(log);
            e.getStackTrace();
            System.out.println(e.getMessage());
            setOHLCEnabled(false);
        }
        System.out.println("TOTAL NEW RECORDS: "+addedOHLC.size());
        long end = System.currentTimeMillis();
        System.out.println("Time: "+(end-start));
        return addedOHLC;

    }

//    @Scheduled(fixedDelay=1250)
    public void storeTrades() throws URISyntaxException, IOException, InterruptedException {
        if (isTradesEnabled()) {
            String pair="XBTUSD";

            timestamp=getTradesHistory(pair, timestamp);
            System.out.println("LAST: "+timestamp);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void storeOHLC() throws URISyntaxException, IOException, InterruptedException {
        if (isOHLCEnabled()) {
            String pair="XBTUSD";
            String interval="1";
            List<OHLC> data = getOHLCData(pair,interval);
            List<Double> allPrices = ohlcRepository.findAllOrderById();
            Collections.reverse(allPrices);
            System.out.println("LAST PRICE: "+allPrices.get(allPrices.size()-1));
            List<Double> macd = MACD.macdLine(allPrices, 12, 26);
            System.out.println("MACD: "+macd.get(macd.size()-1)+" "+macd.get(macd.size()-2));
            List<Double> signal = MACD.signalLine(macd, 9);
            System.out.println("SIGNAL: "+signal.get(signal.size()-1)+" "+signal.get(signal.size()-2));
            LocalDateTime currentDate = LocalDateTime.now();
            macdRepository.save(new MacdData(macd.get(macd.size()-1), currentDate));
            signalRepository.save(new SignalData(signal.get(signal.size()-1), currentDate));




        }
    }


    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isTradesEnabled() {
        return isTradesEnabled;
    }

    public void setTradesEnabled(boolean isTradesEnabled) {
        this.isTradesEnabled = isTradesEnabled;
    }

    public boolean isOHLCEnabled() {
        return isOHLCEnabled;
    }

    public void setOHLCEnabled(boolean OHLCEnabled) {
        isOHLCEnabled = OHLCEnabled;
    }
}
