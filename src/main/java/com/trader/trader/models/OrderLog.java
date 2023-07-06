package com.trader.trader.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "order_log")
public class OrderLog {

    @Id
    private String id;
    private Double price;
    private Double amount;
    private Double stopLoss;
    private Double takeProfit;

    private Instant date;
    private Character buyOrSell;
    @Field("open_or_closed")
    private Boolean openOrClosed;

    public OrderLog(Double price, Double amount, Instant date, Character buyOrSell, Boolean openOrClosed,
                    Double stopLoss, Double takeProfit) {
        this.price = price;
        this.amount = amount;
        this.date = date;
        this.buyOrSell = buyOrSell;
        this.openOrClosed=openOrClosed;
        this.stopLoss=stopLoss;
        this.takeProfit=takeProfit;
    }

    public OrderLog() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Character getBuyOrSell() {
        return buyOrSell;
    }

    public void setBuyOrSell(Character buyOrSell) {
        this.buyOrSell = buyOrSell;
    }

    public Boolean getOpenOrClosed() {
        return openOrClosed;
    }

    public void setOpenOrClosed(Boolean openOrClosed) {
        this.openOrClosed = openOrClosed;
    }

    public Double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(Double takeProfit) {
        this.takeProfit = takeProfit;
    }
}
