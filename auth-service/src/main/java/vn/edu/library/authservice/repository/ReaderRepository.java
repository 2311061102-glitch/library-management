package vn.edu.library.authservice.repository;

import vn.edu.library.authservice.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReaderRepository extends JpaRepository<Reader, Long> {
    Optional<Reader> findByUserId(Long userId);
}
