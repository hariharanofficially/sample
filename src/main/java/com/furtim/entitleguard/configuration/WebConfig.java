package com.furtim.entitleguard.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "https://builders.entitleguard.com",
                "https://cdn.shopify.com",
                "https://entitleguard.flutterflow.app",
                "https://builders-staging.entitleguard.com",
                "https://app2.entitleguard.com",
                "https://app2-staging.entitleguard.com"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}

	
	  
