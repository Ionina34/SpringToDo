package com.emobile.springtodo.core.config;

import com.emobile.springtodo.core.config.properties.AppCacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(AppCacheProperties.class)
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.redis", name = "enable", havingValue = "true")
    @ConditionalOnExpression("'${app.cache.cacheType}'.equals('redis')")
    CacheManager redisCacheManager(AppCacheProperties appCacheProperties, LettuceConnectionFactory lettuceConnectionFactory) {
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig();
        Map<String, RedisCacheConfiguration> redisCacheConfiguration = new HashMap<>();

        appCacheProperties.getCacheNames()
                .forEach(cacheName -> {
                    redisCacheConfiguration.put(cacheName, RedisCacheConfiguration.defaultCacheConfig().entryTtl(
                            appCacheProperties.getCaches().get(cacheName).getExpiry()
                    ));
                });
        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(redisCacheConfiguration)
                .build();
    }
}
