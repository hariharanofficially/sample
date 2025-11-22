package com.furtim.entitleguard;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.furtim.entitleguard.configuration.FileStorageConfiguration;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ FileStorageConfiguration.class })
public class EntitleguardApplication {

	public static void main(String[] args) {
		SpringApplication.run(EntitleguardApplication.class, args);
	}
	
	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}


}
