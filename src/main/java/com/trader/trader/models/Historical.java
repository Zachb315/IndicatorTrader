package com.trader.trader.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection="historical")
public class Historical {

    @Id
    private String id;
    private double volume;
    private Instant date;
    private double price;
    private String rawTime;
    private String last;
    private char buyOrSell;

    public Historical(double volume, Instant date, double price, String rawTime, String last, char buyOrSell) {
        this.volume = volume;
        this.price=price;
        this.date = date;
        this.rawTime = rawTime;
        this.last = last;
        this.buyOrSell = buyOrSell;
    }

    public Historical() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getRawTime() {
        return rawTime;
    }

    public void setRawTime(String rawTime) {
        this.rawTime = rawTime;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public char getBuyOrSell() {
        return buyOrSell;
    }

    public void setBuyOrSell(char buyOrSell) {
        this.buyOrSell = buyOrSell;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
