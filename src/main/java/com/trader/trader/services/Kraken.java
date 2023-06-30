package com.trader.trader.services;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.trader.trader.models.Historical;
import com.trader.trader.repository.HistoricalRepository;
import com.trader.trader.repository.OHLCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
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
    private boolean enabled=true;
    public String timestamp="1616663618";

    @Autowired
    public Kraken(HistoricalRepository historicalRepository, OHLCRepository ohlcRepository) {
        this.historicalRepository = historicalRepository;
        this.ohlcRepository = ohlcRepository;
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
            enabled=false;
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
            enabled=false;
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
            e.getStackTrace();
            System.out.println(e.getMessage());
            setEnabled(false);
        }

    }

    public void writeOHLCToDB(JsonObject resp) {
        JsonArray arr = resp.get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonArray();
        try {
            for (int i=0; i<arr.size(); i++) {

                JsonArray curr = arr.get(i).getAsJsonArray();
                JsonArray processed = new JsonArray();

                Long timestamp = Long.parseLong(curr.get(0).getAsString());
                Date date = new Date(timestamp*1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(date);

                processed.add(format);
                for (int j=1; j<curr.size(); j++) processed.add(curr.get(j).getAsString());
                String table = "Data";
                String sql = "INSERT INTO \""+table+"\" (time, open, high, low, close, volume) VALUES (?, ?, ?, ?, ?, ?)";
//                PreparedStatement stmnt = connection.prepareStatement(sql);
//                stmnt.setString(1, processed.get(0).getAsString());
//                stmnt.setString(2, processed.get(1).getAsString());
//                stmnt.setString(3, processed.get(2).getAsString());
//                stmnt.setString(4, processed.get(3).getAsString());
//                stmnt.setString(5, processed.get(4).getAsString());
//                stmnt.setString(6, processed.get(6).getAsString());
//                System.out.println(stmnt.toString());
//                stmnt.executeUpdate();
//                stmnt.close();

            }
        }
        catch (Exception e) {
            e.getStackTrace();
        }

    }
    @Scheduled(fixedDelay = 1300)
    public void storeTrades() throws URISyntaxException, IOException, InterruptedException {
        if (isEnabled()) {
            String pair="XBTUSD";

            timestamp=getTradesHistory(pair, timestamp);
            System.out.println("LAST: "+timestamp);
        }


    }


    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
