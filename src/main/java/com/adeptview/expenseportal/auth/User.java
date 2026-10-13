package com.adeptview.expenseportal.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    protected User() {
    }

    public User(String username, String displayName, String passwordHash, Role role, String departmentCode) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.departmentCode = departmentCode;
    }

    public void changePasswordHash(String newHash) {
        this.passwordHash = newHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }
}
