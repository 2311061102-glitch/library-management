package vn.edu.library.apigateway.cache;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache kết quả xác thực API Key trong 30 giây để gateway không phải gọi
 * sang auth-service ở mỗi request của đối tác.
 */
@Component
public class ApiKeyValidationCache {

    private static final long TTL_SECONDS = 30;

    private record CacheEntry(boolean valid, Instant expiresAt) {}

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public Boolean get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            return null; // không có trong cache hoặc đã hết hạn
        }
        return entry.valid();
    }

    public void put(String cacheKey, boolean valid) {
        cache.put(cacheKey, new CacheEntry(valid, Instant.now().plusSeconds(TTL_SECONDS)));
    }
}
