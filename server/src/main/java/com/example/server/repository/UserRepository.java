package com.example.server.repository;

import com.example.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 操纵user实体，主键long类型
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOpenid(String openid);

    Optional<User> findByUsername(String username);

    boolean existsByOpenid(String openid);

    boolean existsByUsername(String username);
}
