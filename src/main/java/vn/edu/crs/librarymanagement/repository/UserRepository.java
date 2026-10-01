package vn.edu.crs.librarymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.librarymanagement.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}