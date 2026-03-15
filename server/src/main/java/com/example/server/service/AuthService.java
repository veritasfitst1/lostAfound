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
import java.io.FileWriter;
import java.io.PrintWriter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse wxLogin(String openid, String nickname, String avatarUrl) {
        // #region agent log
        debugLog("{\"sessionId\":\"5795f3\",\"hypothesisId\":\"WX\",\"location\":\"AuthService.wxLogin\",\"message\":\"wxLogin called\",\"data\":{\"openid\":\"" + openid + "\",\"nickname\":\"" + nickname + "\"},\"timestamp\":" + System.currentTimeMillis() + "}");
        // #endregion
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
        // #region agent log
        debugLog("{\"sessionId\":\"5795f3\",\"hypothesisId\":\"A\",\"location\":\"AuthService.adminLogin\",\"message\":\"adminLogin called\",\"data\":{\"username\":\"" + username + "\"},\"timestamp\":" + System.currentTimeMillis() + "}");
        // #endregion
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        // #region agent log
        boolean pwMatch = passwordEncoder.matches(password, user.getPassword());
        debugLog("{\"sessionId\":\"5795f3\",\"hypothesisId\":\"A\",\"location\":\"AuthService.adminLogin\",\"message\":\"password check\",\"data\":{\"role\":\"" + user.getRole() + "\",\"status\":" + user.getStatus() + ",\"pwMatch\":" + pwMatch + ",\"storedHash\":\"" + (user.getPassword() != null ? user.getPassword().substring(0, 10) : "null") + "...\"},\"timestamp\":" + System.currentTimeMillis() + "}");
        // #endregion
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "非管理员账号");
        }
        if (!pwMatch) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被封禁");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        // #region agent log
        debugLog("{\"sessionId\":\"5795f3\",\"hypothesisId\":\"B\",\"location\":\"AuthService.adminLogin\",\"message\":\"login success\",\"data\":{\"tokenLen\":" + token.length() + "},\"timestamp\":" + System.currentTimeMillis() + "}");
        // #endregion
        return AuthResponse.builder()
                .token(token)
                .user(toUserVO(user))
                .build();
    }

    // #region agent log
    private void debugLog(String json) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("../debug-5795f3.log", true))) {
            pw.println(json);
        } catch (Exception ignored) {}
    }
    // #endregion

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
