package com.heima.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.heima.common.redis.CacheService;
@Configuration
@ConditionalOnProperty("spring.redis.host")
public class configuration {
    @Bean
    public CacheService getCacheService() {
        return new CacheService();
    }
}
