package com.example.server.service;

import com.example.server.dto.UserVO;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserVO getProfile(Long userId) {
        User user = findById(userId);
        return toUserVO(user);
    }

    @Transactional
    public UserVO updateProfile(Long userId, String nickname, String avatarUrl, String phone) {
        User user = findById(userId);
        if (nickname != null) user.setNickname(nickname);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        if (phone != null) user.setPhone(phone);
        return toUserVO(userRepository.save(user));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
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
