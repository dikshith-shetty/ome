package com.htm.ome.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {

    @Value("${ome.assets:BTC,TST}")
    private String assetsProp;

    @Value("${matchengine.threadpool.size:5}")
    private int poolSize;

    @Bean
    public List<String> allowedAssets() {
        return Arrays.stream(assetsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Bean
    public ExecutorService matchEngineExecutor() {
        return Executors.newFixedThreadPool(poolSize);
    }
}
