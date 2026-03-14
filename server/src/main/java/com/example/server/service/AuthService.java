package com.example.server.service;

import com.example.server.dto.AuthResponse;
import com.example.server.dto.UserVO;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.UserRepository;
import com.example.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse wxLogin(String openid, String nickname, String avatarUrl) {
        User user = userRepository.findByOpenid(openid)
                .orElseGet(() -> {
                    User u = User.builder()
                            .openid(openid)
                            .nickname(nickname != null ? nickname : "微信用户")
                            .avatarUrl(avatarUrl)
                            .role("USER")
                            .status(0)
                            .build();
                    return userRepository.save(u);
                });
        if (user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被封禁");
        }
        if (nickname != null) user.setNickname(nickname);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return AuthResponse.builder()
                .token(token)
                .user(toUserVO(user))
                .build();
    }

    public AuthResponse adminLogin(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "非管理员账号");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被封禁");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return AuthResponse.builder()
                .token(token)
                .user(toUserVO(user))
                .build();
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
