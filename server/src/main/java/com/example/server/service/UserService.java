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
@Transactional(readOnly = true)
//处理user信息的核心类
public class UserService {

    private final UserRepository userRepository;
    //获取个人资料
    public UserVO getProfile(Long userId) {
        User user = findById(userId);
        return toUserVO(user);
    }
    //更新个人资料
    @Transactional
    public UserVO updateProfile(Long userId, String nickname, String avatarUrl, String phone) {
        User user = findById(userId);
        if (nickname != null) user.setNickname(nickname);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        if (phone != null) user.setPhone(phone);
        return toUserVO(userRepository.save(user));
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
