package com.adeptview.expenseportal.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findFirstByDepartmentCodeAndRole(String departmentCode, Role role);

    List<User> findByPasswordHash(String passwordHash);
}
