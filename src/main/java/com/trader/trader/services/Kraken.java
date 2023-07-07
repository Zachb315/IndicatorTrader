package com.trader.trader.services;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.trader.trader.models.*;
import com.trader.trader.repository.*;
import org.bson.Document;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
    private final OrderLogRepository orderLogRepository;
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
                  LogsRepository logsRepository, MacdRepository macdRepository,
                  SignalRepository signalRepository, OrderLogRepository orderLogRepository) {
        this.historicalRepository = historicalRepository;
        this.ohlcRepository = ohlcRepository;
        this.logsRepository = logsRepository;
        this.macdRepository = macdRepository;
        this.signalRepository = signalRepository;
        this.orderLogRepository = orderLogRepository;
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
                    , Instant.now());
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
                    , Instant.now());
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
                if (!historicalRepository.existsByDate(localDateTime.withSecond(0).toInstant(ZoneOffset.UTC))) {
                    Historical data = new Historical(curr.get(1).getAsDouble(), localDateTime.withSecond(0).toInstant(ZoneOffset.UTC),
                            curr.get(0).getAsDouble(), timestamp.toString(),
                            last, curr.get(3).getAsString().charAt(0));
                    historicalRepository.save(data);
                    count++;
                }
            }
        }
        catch (Exception e) {
            Logs log = new Logs(e.getMessage(), Instant.now());
            logsRepository.save(log);
            e.getStackTrace();
            System.out.println(e.getMessage());
            setTradesEnabled(false);
        }
        System.out.println("Rows Added: "+count);

    }

    public List<OHLC> writeOHLCToDB(JsonObject resp) {
        JsonArray arr = resp.get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray();
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



                OHLC ohlc = new OHLC(ldt.toInstant(ZoneOffset.UTC), curr.get(1).getAsDouble(), curr.get(4).getAsDouble(), curr.get(2).getAsDouble(), curr.get(3).getAsDouble(), curr.get(6).getAsDouble());
                if (!ohlcRepository.existsByDate(ldt.toInstant(ZoneOffset.UTC))) {
                    ohlcRepository.save(ohlc);
                    addedOHLC.add(ohlc);
                }

            }
        }
        catch (Exception e) {
            Logs log = new Logs(e.getMessage(), Instant.now());
            logsRepository.save(log);
            e.getStackTrace();
            System.out.println(e.getMessage());
            setOHLCEnabled(false);
        }
        System.out.println("TOTAL NEW RECORDS: "+addedOHLC.size());
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
            long start = System.currentTimeMillis();
            String pair="XBTUSD";
            String interval="60";
            List<OHLC> data = getOHLCData(pair,interval);
            List<Document> allPricesDocuments = ohlcRepository.findRecentOrders(200);
            List<Double> allPrices = new ArrayList<>();
            for (Document d : allPricesDocuments) {
                allPrices.add(d.getDouble("close"));
            }
            allPrices.add(0, getCurrentPrice());
            Collections.reverse(allPrices);
            Double lastPrice=allPrices.get(allPrices.size()-1);
            System.out.println("CURRENT PRICE: "+lastPrice);
            List<Double> macd = MACD.macdLine(allPrices, 12, 26);
            System.out.println("MACD: "+macd.get(macd.size()-1)+" "+macd.get(macd.size()-2));
            List<Double> signal = MACD.signalLine(macd, 9);
            System.out.println("SIGNAL: "+signal.get(signal.size()-1)+" "+signal.get(signal.size()-2));
            Instant currentDate = Instant.now();
            if (data.size()==1) {
                macdRepository.save(new MacdData(macd.get(macd.size()-1), currentDate));
                signalRepository.save(new SignalData(signal.get(signal.size()-1), currentDate));
            }
            Double currentMacd = macd.get(macd.size()-1);
            Double currentSignal = signal.get(signal.size()-1);
            Double prevMacd = macd.get(macd.size()-2);
            Double prevSignal = signal.get(signal.size()-2);

            if (currentMacd<0 && currentSignal<0) {
                if (currentMacd>currentSignal && prevMacd<prevSignal && orderLogRepository.countByOpenOrClosed()==0) {
                    System.out.println("BOUGHT LONG");
                    OrderLog orderLog = new OrderLog(lastPrice, 1.0, Instant.now(), 'b', true, lastPrice-(lastPrice*.005), lastPrice+(lastPrice*.01));
                    orderLogRepository.save(orderLog);
                }
            }
            else if (currentMacd>0 && currentSignal>0) {
                if (currentMacd<currentSignal && prevMacd>prevSignal && orderLogRepository.countByOpenOrClosed()==0) {
                    System.out.println("SOLD SHORT");
                    OrderLog orderLog = new OrderLog(allPrices.get(allPrices.size()-1), 1.0, Instant.now(), 's', true, lastPrice+(lastPrice*.01), lastPrice-(lastPrice*.005));
                    orderLogRepository.save(orderLog);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("TIME(ms): "+(end-start));
            System.out.println("________________________________________________________");

        }
    }

    public Double getCurrentPrice() throws URISyntaxException, IOException, InterruptedException {
        String url = domain+"/0/public/Trades?pair=XBTUSD&count=1";
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url))
                .headers("User-Agent", "Kraken API Java Client")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        JsonArray arr = (gson.fromJson(response.body(), JsonObject.class).getAsJsonObject())
                .get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray().get(0).getAsJsonArray();
        return arr.get(0).getAsDouble();


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
