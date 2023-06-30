package com.trader.trader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TraderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraderApplication.class, args);
	}

}
