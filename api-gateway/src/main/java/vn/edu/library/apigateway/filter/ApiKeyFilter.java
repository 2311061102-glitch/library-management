package vn.edu.library.apigateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.edu.library.apigateway.cache.ApiKeyValidationCache;
import vn.edu.library.apigateway.client.AuthServiceClient;

/**
 * Bảo vệ API dành cho đối tác bên ngoài (/api/public/books) bằng API Key + scope.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;
    private final ApiKeyValidationCache cache;

    // Map từ path sang scope cần có - mở rộng ở đây khi thêm route đối tác mới
    private static final String PARTNER_PATH = "/api/public/books";
    private static final String REQUIRED_SCOPE = "books:read";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.startsWith(PARTNER_PATH)) {
            return chain.filter(exchange);
        }

        String apiKey = request.getHeaders().getFirst("X-API-KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return reject(exchange);
        }

        String cacheKey = apiKey + ":" + REQUIRED_SCOPE;
        Boolean cached = cache.get(cacheKey);

        if (cached != null) {
            // Có trong cache - không cần gọi sang auth-service
            return cached ? chain.filter(exchange) : reject(exchange);
        }

        // Chưa có trong cache - gọi sang auth-service kiểm tra rồi lưu lại cache
        return authServiceClient.isValidForScope(apiKey, REQUIRED_SCOPE)
                .flatMap(valid -> {
                    cache.put(cacheKey, valid);
                    return valid ? chain.filter(exchange) : reject(exchange);
                });
    }

    private Mono<Void> reject(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
