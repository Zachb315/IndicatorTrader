package com.trader.trader.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Historical {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private double volume;
    @Column(unique = true)
    private LocalDateTime date;
    private double price;
    private String rawTime;
    private String last;
    private char buyOrSell;

    public Historical(double volume, LocalDateTime date, double price, String rawTime, String last, char buyOrSell) {
        this.volume = volume;
        this.price=price;
        this.date = date;
        this.rawTime = rawTime;
        this.last = last;
        this.buyOrSell = buyOrSell;
    }

    public Historical() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
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
