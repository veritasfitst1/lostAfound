package com.example.server.service;

import com.example.server.dto.CommentVO;
import com.example.server.entity.Item;
import com.example.server.entity.ItemComment;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.ItemCommentRepository;
import com.example.server.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final ItemCommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    //评论列表（已过期物品仅发布者可查看留言）
    public List<CommentVO> listByItemId(Long itemId, Long viewerUserId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (item.getStatus() == 3) {
            if (viewerUserId == null || !item.getUser().getId().equals(viewerUserId)) {
                throw new BusinessException(404, "物品不存在或已下架");
            }
        }
        return commentRepository.findByItemIdOrderByCreatedAtAsc(itemId).stream()
                .map(this::toCommentVO)
                .collect(Collectors.toList());
    }

    //添加评论
    @Transactional
    public CommentVO create(Long userId, Long itemId, String content) {
        userService.assertNotBanned(userId);
        User user = userService.findById(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (item.getStatus() != 0) {
            throw new BusinessException(400, "该信息已结束，无法留言");
        }

        ItemComment comment = ItemComment.builder()
                .item(item)
                .user(user)
                .content(content)
                .build();
        comment = commentRepository.save(comment);
        return toCommentVO(comment);
    }

    //封装成前端可用
    private CommentVO toCommentVO(ItemComment c) {
        return CommentVO.builder()
                .id(c.getId())
                .itemId(c.getItem().getId())
                .userId(c.getUser().getId())
                .userNickname(c.getUser().getNickname())
                .userAvatarUrl(c.getUser().getAvatarUrl())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
