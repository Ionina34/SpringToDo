package com.emobile.springtodo.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.cache")
public class AppCacheProperties {

    private final List<String> cacheNames = new ArrayList<>();
    private final Map<String, CacheProperties> caches = new HashMap<>();
    private CacheType cacheType;

    @Data
    public static class CacheProperties {
        private Duration expiry = Duration.ZERO;
    }

    public interface CacheNames {
        String TASK_BY_ID = "taskById";
        String TASKS_BY_USER = "tasksByUser";
        String TASK_COUNT = "taskCount";
        String USER_BY_ID = "userById";
    }

    public enum CacheType {
        REDIS
    }
}
