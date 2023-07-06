package com.trader.trader;

import com.trader.trader.controllers.MongoConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TraderApplication implements CommandLineRunner {

	private final MongoConfig mongoConfig;

	public static void main(String[] args) {
		SpringApplication.run(TraderApplication.class, args);
	}

	public TraderApplication(MongoConfig mongoConfig) {
		this.mongoConfig=mongoConfig;
	}

	@Override
	public void run(String... args) {
		mongoConfig.createCollections();
	}

//	@Bean
//	public MongoConfig mongoConfig(MongoTemplate mongoTemplate) {
//		return new MongoConfig(mongoTemplate);
//	}

}
