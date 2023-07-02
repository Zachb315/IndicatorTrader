package com.trader.trader.services;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.trader.trader.models.Historical;
import com.trader.trader.models.Logs;
import com.trader.trader.models.OHLC;
import com.trader.trader.repository.HistoricalRepository;
import com.trader.trader.repository.LogsRepository;
import com.trader.trader.repository.OHLCRepository;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class Kraken {
    private HttpClient httpClient;
    private String domain="https://api.kraken.com";
    public Gson gson;
    private final HistoricalRepository historicalRepository;
    private final OHLCRepository ohlcRepository;
    private final LogsRepository logsRepository;
    private boolean isTradesEnabled=true;
    private boolean isOHLCEnabled=true;
    public String timestamp="1616663618";
    @Value("$[api.key]")
    private String apiKey;
    @Value("$[api.secret]")
    private String apiSecret;

    @Autowired
    public Kraken(HistoricalRepository historicalRepository, OHLCRepository ohlcRepository, LogsRepository logsRepository) {
        this.historicalRepository = historicalRepository;
        this.ohlcRepository = ohlcRepository;
        this.logsRepository = logsRepository;
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

    public void getOHLCData(String pair, String interval) throws URISyntaxException, IOException, InterruptedException {
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
            return;
        }
        writeOHLCToDB(gson.fromJson(resp.body(), JsonObject.class).getAsJsonObject());

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

        JsonArray arr = resp.get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray();
        String last = resp.get("result").getAsJsonObject().get("last").getAsString();
        try {
            for (int i=0; i<arr.size(); i++) {
                JsonArray curr = arr.get(i).getAsJsonArray();
                Long timestamp = Long.parseLong(curr.get(2).getAsString().split("\\.")[0]);
                Date timestampToTime = new Date(timestamp*1000);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(timestampToTime);
                Date date = sdf.parse(format);
                LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                Historical data = new Historical(curr.get(1).getAsDouble(), localDateTime, timestamp.toString(),
                        last, curr.get(3).getAsString().charAt(0));
                historicalRepository.save(data);
            }
        }
        catch (Exception e) {
            Logs log = new Logs(e.getMessage(), LocalDateTime.now());
            logsRepository.save(log);
            e.getStackTrace();
            System.out.println(e.getMessage());
            setTradesEnabled(false);
        }

    }

    public void writeOHLCToDB(JsonObject resp) {
        JsonArray arr = resp.get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray();
        int added=0;
        long start = System.currentTimeMillis();
        try {
            for (int i=0; i<arr.size(); i++) {

                JsonArray curr = arr.get(i).getAsJsonArray();
                JsonArray processed = new JsonArray();

                Long timestamp = Long.parseLong(curr.get(0).getAsString());
                Date timeStampToTime = new Date(timestamp*1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(timeStampToTime);
                Date date = sdf.parse(format);
                LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();


                processed.add(format);
                for (int j=1; j<curr.size(); j++) processed.add(curr.get(j).getAsString());
                OHLC ohlc = new OHLC(ldt, curr.get(1).getAsDouble(), curr.get(2).getAsDouble(), curr.get(3).getAsDouble(), curr.get(4).getAsDouble(), curr.get(5).getAsDouble());
                if (!ohlcRepository.existsByDate(ldt)) {
                    ohlcRepository.save(ohlc);
                    added++;
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
        System.out.println("TOTAL NEW RECORDS: "+added);
        long end = System.currentTimeMillis();
        System.out.println("Time: "+(end-start));

    }

    public void storeTrades() throws URISyntaxException, IOException, InterruptedException {
        if (isTradesEnabled()) {
            String pair="XBTUSD";

            timestamp=getTradesHistory(pair, timestamp);
            System.out.println("LAST: "+timestamp);
        }
    }

    @Scheduled(fixedDelay=60000)
    public void storeOHLC() throws URISyntaxException, IOException, InterruptedException {
        if (isOHLCEnabled()) {
            String pair="XBTUSD";
            String interval="1";
            getOHLCData(pair,interval);
            System.out.println("Enabled: "+isOHLCEnabled());

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
