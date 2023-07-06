package com.trader.trader.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trader.trader.models.OHLC;
import com.trader.trader.models.OrderLog;
import com.trader.trader.repository.OrderLogRepository;
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
import java.util.List;

@Service
public class OrderService {
    private HttpClient httpClient;
    private String domain="https://api.kraken.com";
    public Gson gson;
    private final OrderLogRepository orderLogRepository;
    @Value("$[api.key]")
    private String apiKey;
    @Value("$[api.secret]")
    private String apiSecret;


    @Autowired
    public OrderService(OrderLogRepository orderLogRepository) {
        this.orderLogRepository=orderLogRepository;
        this.httpClient=HttpClient.newHttpClient();
        this.gson=new Gson();
    }



    @Scheduled(fixedDelay=1500)
    public void checkOpenTrades() throws IOException, InterruptedException, URISyntaxException {
        Integer activeTrades=orderLogRepository.countByOpenOrClosed(true);
        double takeProfit = 0.01;
        if (activeTrades!=null && activeTrades>0) {
            String url = domain+"/0/public/Ticker?pair=XBTUSD";
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url))
                    .headers("User-Agent", "Kraken API Java Client")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject json = (gson.fromJson(response.body(), JsonObject.class).getAsJsonObject())
                    .get("result").getAsJsonObject().get("XXBTZUSD").getAsJsonObject();
            Double ask = json.get("a").getAsJsonArray().get(0).getAsDouble();
            Double bid = json.get("b").getAsJsonArray().get(0).getAsDouble();
            System.out.println("ASK: "+ask+" "+"BID: "+bid);
            OrderLog curr = orderLogRepository.findTopByOrderByIdDesc();
            if (Math.abs((bid-curr.getPrice())/curr.getPrice()) > takeProfit) {
                System.out.println("SOLD ORDER: ");
                curr.setOpenOrClosed(false);
                orderLogRepository.save(curr);
            }
        }

    }
}
