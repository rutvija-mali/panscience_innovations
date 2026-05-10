package com.example.demo.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${app.cors.allowed-origins:http://localhost:5173}")
	private String allowedOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] origins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.toArray(String[]::new);

		registry.addMapping("/api/**")
				.allowedOrigins(origins.length == 0 ? new String[] { "http://localhost:5173" } : origins)
				.allowedMethods("GET", "POST", "OPTIONS")
				.allowedHeaders("*");
	}

}
