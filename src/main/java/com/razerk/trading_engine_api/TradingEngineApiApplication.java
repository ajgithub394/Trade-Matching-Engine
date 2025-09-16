package com.razerk.trading_engine_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TradingEngineApiApplication {
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {

		context = SpringApplication.run(TradingEngineApiApplication.class, args);
	}

	public static void shutdown(){
		context.close();
	}

}
