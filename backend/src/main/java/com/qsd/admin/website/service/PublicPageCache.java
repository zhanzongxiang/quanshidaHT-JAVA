package com.qsd.admin.website.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class PublicPageCache {
    private static final long TTL_MILLIS = Duration.ofMinutes(5).toMillis();

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public JsonNode get(String key, Supplier<JsonNode> loader) {
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt() > now) {
            return cached.value().deepCopy();
        }

        JsonNode loaded = loader.get();
        cache.put(key, new CacheEntry(loaded.deepCopy(), now + TTL_MILLIS));
        return loaded.deepCopy();
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void evictByPrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private record CacheEntry(JsonNode value, long expiresAt) {
    }
}
