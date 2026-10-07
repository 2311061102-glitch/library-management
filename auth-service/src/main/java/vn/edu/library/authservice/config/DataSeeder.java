package vn.edu.library.authservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.library.authservice.entity.Reader;
import vn.edu.library.authservice.entity.User;
import vn.edu.library.authservice.repository.ReaderRepository;
import vn.edu.library.authservice.repository.UserRepository;

/** Tạo sẵn tài khoản demo: 1 thủ thư + 2 độc giả. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ReaderRepository readerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("librarian").isEmpty()) {
            User librarian = new User();
            librarian.setUsername("librarian");
            librarian.setPassword(passwordEncoder.encode("librarian123"));
            librarian.setRole(User.ROLE_LIBRARIAN);
            userRepository.save(librarian);
        }

        seedReader("reader1", "reader123", "Nguyễn Văn An");
        seedReader("reader2", "reader123", "Trần Thị Bình");
    }

    private void seedReader(String username, String rawPassword, String fullName) {
        if (userRepository.findByUsername(username).isPresent()) return;

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(User.ROLE_READER);
        user = userRepository.save(user);

        Reader reader = new Reader();
        reader.setFullName(fullName);
        reader.setUser(user);
        reader = readerRepository.save(reader);
        reader.setReaderCode(String.format("DG%06d", reader.getId()));
        readerRepository.save(reader);
    }
}
