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
    private static final int MAX_CACHE_SIZE = 500;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public JsonNode get(String key, Supplier<JsonNode> loader) {
        // Evict expired entries if cache is large
        if (cache.size() > MAX_CACHE_SIZE) {
            evictExpired();
        }
        // If still too large after eviction, clear oldest entries
        if (cache.size() > MAX_CACHE_SIZE) {
            cache.clear();
        }

        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt() > now) {
            return cached.value().deepCopy();
        }

        JsonNode loaded = loader.get();
        cache.put(key, new CacheEntry(loaded.deepCopy(), now + TTL_MILLIS));
        return loaded.deepCopy();
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
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
