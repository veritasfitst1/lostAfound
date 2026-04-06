package com.example.server.service;

import com.example.server.dto.UserVO;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
//处理user信息的核心类
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WechatService wechatService;
    //获取个人资料
    public UserVO getProfile(Long userId) {
        User user = findById(userId);
        return toUserVO(user);
    }
    //更新个人资料
    @Transactional
    public UserVO updateProfile(Long userId, String nickname, String avatarUrl, String phone,
                                String username, String password) {
        User user = findById(userId);
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname.trim());
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (username != null && !username.isBlank()) {
            String u = username.trim();
            if (u.length() < 3 || u.length() > 20) {
                throw new BusinessException(400, "用户名长度须为 3-20 位");
            }
            userRepository.findByUsername(u)
                    .filter(other -> !other.getId().equals(userId))
                    .ifPresent(other -> {
                        throw new BusinessException(400, "用户名已被占用");
                    });
            user.setUsername(u);
        }
        if (password != null && !password.isBlank()) {
            if (password.length() < 6 || password.length() > 32) {
                throw new BusinessException(400, "密码长度须为 6-32 位");
            }
            user.setPassword(passwordEncoder.encode(password));
        }
        return toUserVO(userRepository.save(user));
    }

    /** 将当前登录用户与微信 openid 绑定（同一 openid 仅能绑定一个账号） */
    @Transactional
    public UserVO bindWx(Long userId, String code) {
        String openid = wechatService.getOpenid(code);
        User current = findById(userId);
        if (current.getOpenid() != null && !current.getOpenid().isBlank()) {
            throw new BusinessException(400, "已绑定微信");
        }
        userRepository.findByOpenid(openid)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new BusinessException(400, "该微信已被其他账号绑定");
                });
        current.setOpenid(openid);
        return toUserVO(userRepository.save(current));
    }
    //通过用户id寻找用户
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    /** 账号 status=1 封禁时禁止发布、留言、发私信等 */
    public void assertNotBanned(Long userId) {
        assertNotBanned(findById(userId));
    }

    public void assertNotBanned(User user) {
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被封禁，无法使用该功能");
        }
    }

    public UserVO toUserVO(User user) {
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
