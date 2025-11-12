package com.darian.financemanagement.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

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
     * Read cache without adding new entry
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
     * Atomically checks if a request is duplicate and caches it if not.
     * This prevents race conditions where multiple identical requests arrive simultaneously.
     *
     * @param idempotencyKey unique key representing a request
     * @param response the response string to cache if this is not a duplicate
     * @return null if this is a new request (and has been cached), or the cached response if duplicate
     */
    public String checkAndCacheResponse(String idempotencyKey, String response) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return null; // No idempotency key, treat as new request
        }

        // Atomic check-and-set operation using putIfAbsent
        // Only one thread can successfully put a new entry for this key;
        // others get the existing one when multiple threads race.
        // If no existing entry -> this is a new request -> return null.
        // If existing entry found -> this is a duplicate -> return existing cached response.
        CacheEntry newEntry = new CacheEntry(response);
        CacheEntry existingEntry = cache.putIfAbsent(idempotencyKey, newEntry);

        if (existingEntry == null) {
            return null;
        }

        // Check if existing entry is expired
        // Thread-safe replacement of expired entry:
        // - replace() succeeds only if existingEntry is still the same as the current entry (cache.get(idempotencyKey))
        // - if same → no other thread has modified it → safe to replace
        // - if different → another thread already replaced it → do not replace again
        if (isExpired(idempotencyKey)) {
            if (cache.replace(idempotencyKey, existingEntry, newEntry)) {
                return null;
            }
            existingEntry = cache.get(idempotencyKey);
        }

        // Return the cached response (this is a duplicate)
        return existingEntry != null ? existingEntry.getResponse() : null;
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