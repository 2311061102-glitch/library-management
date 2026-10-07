package vn.edu.library.authservice.service;

import vn.edu.library.authservice.dto.ApiKeyCreateRequestDTO;
import vn.edu.library.authservice.dto.ApiKeyResponseDTO;
import vn.edu.library.authservice.entity.ApiKey;
import vn.edu.library.authservice.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final String ACTIVE = "ACTIVE";
    private static final String REVOKED = "REVOKED";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyResponseDTO create(ApiKeyCreateRequestDTO dto) {
        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(generateRandomKey());
        apiKey.setOwnerName(dto.getOwnerName());
        apiKey.setScopes(dto.getScopes());
        apiKey.setStatus(ACTIVE);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(
                dto.getValidDays() != null ?
                        LocalDateTime.now().plusDays(dto.getValidDays()) : null
        );
        return toDTO(apiKeyRepository.save(apiKey));
    }

    public List<ApiKeyResponseDTO> getAll() {
        return apiKeyRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void revoke(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy API Key id = " + id));
        apiKey.setStatus(REVOKED);
        apiKeyRepository.save(apiKey);
    }

    /**
     * Kiểm tra 1 key có hợp lệ cho 1 scope cụ thể không.
     * Dùng bởi endpoint nội bộ mà api-gateway sẽ gọi sang.
     */
    public boolean isValidForScope(String keyValue, String requiredScope) {
        return apiKeyRepository.findByKeyValue(keyValue)
                .filter(k -> ACTIVE.equals(k.getStatus()))
                .filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> List.of(k.getScopes().split(",")).contains(requiredScope))
                .isPresent();
    }

    private String generateRandomKey() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        // Tiền tố "lib_" giúp nhận diện key của hệ thống thư viện khi đọc log
        return "lib_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ApiKeyResponseDTO toDTO(ApiKey k) {
        return new ApiKeyResponseDTO(
                k.getId(), k.getKeyValue(), k.getOwnerName(), k.getScopes(),
                k.getStatus(), k.getExpiresAt(), k.getCreatedAt()
        );
    }
}