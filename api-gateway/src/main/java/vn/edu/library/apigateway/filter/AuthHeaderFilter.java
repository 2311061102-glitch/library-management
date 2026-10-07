package vn.edu.library.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Xác thực sơ bộ tại gateway: các đường dẫn không public bắt buộc phải có header Authorization.
 * Việc kiểm tra chữ ký JWT và phân quyền chi tiết do từng service tự thực hiện.
 */
@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    // Các đường dẫn KHÔNG cần header Authorization
    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/public/books"   // đối tác dùng X-API-KEY, được kiểm tra riêng ở ApiKeyFilter
    );

    // Các tài nguyên cho phép đọc (GET) công khai: xem sách, xem thể loại
    private static final List<String> PUBLIC_READ_PREFIXES = List.of(
            "/api/books",
            "/api/categories"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        boolean isOpen = OPEN_PATHS.stream().anyMatch(path::startsWith);
        boolean isPublicRead = "GET".equals(method)
                && PUBLIC_READ_PREFIXES.stream().anyMatch(path::startsWith);

        if (isOpen || isPublicRead || "OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        if (request.getHeaders().getFirst("Authorization") == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // chạy sớm, trước khi request được định tuyến đi
    }
}
