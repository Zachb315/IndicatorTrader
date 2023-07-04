package com.trader.trader.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class OrderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double price;
    private Double amount;
    private Double stopLoss;
    private Double takeProfit;

    private LocalDateTime date;
    private Character buyOrSell;
    private Boolean openOrClosed;

    public OrderLog(Double price, Double amount, LocalDateTime date, Character buyOrSell, Boolean openOrClosed,
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
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
