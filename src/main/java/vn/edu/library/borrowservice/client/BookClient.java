package vn.edu.library.borrowservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/** Gọi REST sang book-service (API nội bộ), vì borrow-service không được truy cập thẳng book_db. */
@Component
@RequiredArgsConstructor
public class BookClient {

    private final RestTemplate restTemplate;

    @Value("${book-service.base-url}")
    private String bookServiceBaseUrl;

    /**
     * Gọi sang book-service để trừ 1 bản sách. Trả về tên sách (để lưu vào phiếu mượn).
     * @throws IllegalStateException nếu book-service từ chối (hết bản, không tìm thấy sách)
     *                               hoặc không kết nối được (service đang tắt/timeout)
     */
    @SuppressWarnings("rawtypes")
    public String reserveCopy(Long bookId) {
        String url = bookServiceBaseUrl + "/internal/books/" + bookId + "/reserve-copy";
        try {
            Map body = restTemplate.exchange(url, HttpMethod.PATCH, null, Map.class).getBody();
            Object title = body == null ? null : body.get("title");
            return title == null ? ("Sách #" + bookId) : title.toString();
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalStateException("Sách đã hết bản");
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalStateException("Sách không tồn tại");
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new IllegalStateException("Không thể kết nối tới book-service, vui lòng thử lại sau");
        }
    }

    public void releaseCopy(Long bookId) {
        String url = bookServiceBaseUrl + "/internal/books/" + bookId + "/release-copy";
        try {
            restTemplate.exchange(url, HttpMethod.PATCH, null, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalStateException("Sách không tồn tại");
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new IllegalStateException("Không thể kết nối tới book-service, vui lòng thử lại sau");
        }
    }
}
