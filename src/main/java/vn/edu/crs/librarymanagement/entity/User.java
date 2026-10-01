package vn.edu.crs.librarymanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "student_code")
    private String studentCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 20)
    private String accountStatus = "ACTIVE";

    @Column(name = "card_expires_at")
    private LocalDate cardExpiresAt;

    @Column(name = "blocked_until")
    private LocalDate blockedUntil;

    public enum Role {
        ADMIN, CUSTOMER
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public LocalDate getCardExpiresAt() { return cardExpiresAt; }
    public void setCardExpiresAt(LocalDate cardExpiresAt) { this.cardExpiresAt = cardExpiresAt; }
    public LocalDate getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDate blockedUntil) { this.blockedUntil = blockedUntil; }
}