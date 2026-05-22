package com.qsd.admin.common.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {
    // Key: "ip:username" or "ip", Value: attempt info
    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    /**
     * Check if the request is allowed. Returns true if allowed, false if rate limited.
     */
    public boolean isAllowed(String key) {
        cleanup();
        AttemptInfo info = attempts.get(key);
        if (info == null) {
            return true;
        }
        if (info.isLockedOut()) {
            return false;
        }
        return true;
    }

    /**
     * Record a failed attempt.
     */
    public void recordFailure(String key) {
        attempts.compute(key, (k, existing) -> {
            if (existing == null) {
                return new AttemptInfo(1, System.currentTimeMillis());
            }
            existing.increment();
            return existing;
        });
    }

    /**
     * Clear attempts on successful login.
     */
    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    /**
     * Get remaining lockout time in seconds.
     */
    public long getRemainingLockoutSeconds(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null || !info.isLockedOut()) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - info.lastAttemptTime;
        long remaining = LOCKOUT_DURATION_MS - elapsed;
        return Math.max(0, remaining / 1000);
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(entry ->
            now - entry.getValue().lastAttemptTime > LOCKOUT_DURATION_MS
        );
    }

    private static class AttemptInfo {
        final AtomicInteger count;
        volatile long lastAttemptTime;

        AttemptInfo(int count, long lastAttemptTime) {
            this.count = new AtomicInteger(count);
            this.lastAttemptTime = lastAttemptTime;
        }

        void increment() {
            this.count.incrementAndGet();
            this.lastAttemptTime = System.currentTimeMillis();
        }

        boolean isLockedOut() {
            if (count.get() < MAX_ATTEMPTS) {
                return false;
            }
            long elapsed = System.currentTimeMillis() - lastAttemptTime;
            return elapsed < LOCKOUT_DURATION_MS;
        }
    }
}
