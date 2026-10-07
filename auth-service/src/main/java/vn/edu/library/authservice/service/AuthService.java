package vn.edu.library.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.library.authservice.dto.LoginRequestDTO;
import vn.edu.library.authservice.dto.LoginResponseDTO;
import vn.edu.library.authservice.dto.RegisterRequestDTO;
import vn.edu.library.authservice.entity.Reader;
import vn.edu.library.authservice.entity.User;
import vn.edu.library.authservice.exception.InvalidCredentialsException;
import vn.edu.library.authservice.repository.ReaderRepository;
import vn.edu.library.authservice.repository.UserRepository;
import vn.edu.library.authservice.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ReaderRepository readerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Sai username hoặc password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Sai username hoặc password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponseDTO(user.getId(), token, user.getUsername(), user.getRole());
    }

    /** Độc giả tự đăng ký tài khoản. Luôn tạo role READER, không cho tự chọn role. */
    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.ROLE_READER);
        user = userRepository.save(user);

        Reader reader = new Reader();
        reader.setFullName(dto.getFullName().trim());
        reader.setUser(user);
        reader = readerRepository.save(reader);
        reader.setReaderCode(String.format("DG%06d", reader.getId()));
        readerRepository.save(reader);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponseDTO(user.getId(), token, user.getUsername(), user.getRole());
    }
}
