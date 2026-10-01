package vn.edu.crs.librarymanagement.service;

import org.springframework.stereotype.Service;
import vn.edu.crs.librarymanagement.entity.User;
import vn.edu.crs.librarymanagement.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> updateUser(Long id, User user) {
        return userRepository.findById(id).map(existing -> {
            existing.setUsername(user.getUsername());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existing.setPassword(user.getPassword());
            }
            existing.setRole(user.getRole());
            existing.setFullName(user.getFullName());
            existing.setStudentCode(user.getStudentCode());
            return userRepository.save(existing);
        });
    }

    public boolean deleteUser(Long id) {
        return userRepository.findById(id).map(u -> {
            userRepository.delete(u);
            return true;
        }).orElse(false);
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay nguoi dung"));

        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Mat khau hien tai khong dung");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
