package com.jk.apps.banking.fx.sor.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean(name = "quoteRestClient")
    public RestClient quoteRestClient(@Value("${fx.quote.base-url:http://127.0.0.1:8081}") String quoteBaseUrl) {
        return RestClient.builder().baseUrl(quoteBaseUrl).build();
    }

    @Bean(name = "routeExecRestClient")
    public RestClient routeExecRestClient(
            @Value("${fx.route-exec.base-url:http://127.0.0.1:8083}") String routeExecBaseUrl) {
        return RestClient.builder().baseUrl(routeExecBaseUrl).build();
    }
}
