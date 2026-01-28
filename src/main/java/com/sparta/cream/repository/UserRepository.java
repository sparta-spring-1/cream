package com.sparta.cream.repository;

import java.util.Optional;

import com.sparta.cream.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
	boolean existsByEmail(String email);
	Optional<Users> findByEmail(String email);
}

