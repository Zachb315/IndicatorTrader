package com.trader.trader.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collation = "logs")
public class Logs {
    @Id
    private String id;
    private String message;
    private Instant date;

    public Logs(String message, Instant date) {
        this.message = message;
        this.date = date;
    }

    public Logs() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }
}
