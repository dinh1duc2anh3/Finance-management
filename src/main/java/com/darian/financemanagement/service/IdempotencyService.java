package com.darian.financemanagement.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service class for managing idempotency keys and their cached responses.
 * Used to prevent duplicate processing of requests (such as API form submissions)
 * by caching the result for a short period, based on an idempotency key.
 */
@Service
public class IdempotencyService {

    /**
     * The in-memory cache storing the mapping between idempotency keys and their corresponding responses and timestamps.
     */
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Time-to-live for cache entries, in minutes.
     * If the cached entry is older than this value, it is considered expired.
     */
    private final int CACHE_TTL_MINUTES = 5; 

    /**
     * Retrieves a cached response for the provided idempotency key, if it exists and isn't expired.
     *
     * @param idempotencyKey unique key representing a request
     * @return the cached response String if found and valid, otherwise null
     */
    public String getCacheResponse(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            // Return null if the key is invalid (null or empty)
            return null;
        }

        CacheEntry entry = cache.get(idempotencyKey);

        if (entry == null) {
            // No cached response for this idempotency key
            return null;
        }

        // If the entry is expired, remove from cache and return null
        if (entry != null && isExpired(idempotencyKey)) {
            cache.remove(idempotencyKey);
            return null;
        }

        // Return cached response if entry still valid
        return entry.getResponse();
    }

    /**
     * Caches a response for the provided idempotency key.
     *
     * @param idempotencyKey unique key representing a request
     * @param response the response string to cache
     */
    public void cacheResponse(String idempotencyKey, String response) {
        cache.put(idempotencyKey, new CacheEntry(response));
    }

    /**
     * Checks if a given idempotency key's cache entry is expired.
     *
     * @param idempotencyKey key to check expiration for
     * @return true if the entry is expired, false otherwise
     */
    private boolean isExpired(String idempotencyKey) {
        return System.currentTimeMillis() - cache.get(idempotencyKey).getTimestamp() > CACHE_TTL_MINUTES * 60 * 1000;
    }

    /**
     * Inner static class for wrapping cached responses with their creation timestamps.
     */
    private static class CacheEntry {
        private final String response;
        private final long timestamp;

        /**
         * CacheEntry constructor. Sets the response and current timestamp.
         * @param response the response string to cache
         */
        public CacheEntry(String response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * @return the cached response
         */
        public String getResponse() {
            return response;
        }

        /**
         * @return the timestamp when the entry was cached (in ms)
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}