package com.trader.trader.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoConfig {
    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate=mongoTemplate;
    }

    public void createCollections() {
        List<String> collections = new ArrayList<>(Arrays.asList("historical", "ohlc", "logs",
                "macd_data", "signal_data", "order_log"));
        for (String s : collections) {
            if (!mongoTemplate.collectionExists(s)) {
                mongoTemplate.createCollection(s);

            }

        }
    }

}
