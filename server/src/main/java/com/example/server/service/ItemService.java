package com.example.server.service;

import com.example.server.dto.ItemCreateRequest;
import com.example.server.dto.ItemVO;
import com.example.server.dto.PageResponse;
import com.example.server.entity.Item;
import com.example.server.entity.ItemCategory;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.ItemCategoryRepository;
import com.example.server.repository.ItemCommentRepository;
import com.example.server.repository.ItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemCommentRepository commentRepository;
    private final UserService userService;

    private static final int TYPE_LOST = 0;
    private static final int TYPE_FOUND = 1;
    private static final int STATUS_SEARCHING = 0;
    private static final int STATUS_FOUND = 1;
    private static final int STATUS_CANCELLED = 2;
    private static final int STATUS_EXPIRED = 3;

    @Transactional
    public ItemVO create(Long userId, ItemCreateRequest req) {
        User user = userService.findById(userId);
        ItemCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new BusinessException(400, "分类不存在"));

        Item item = Item.builder()
                .user(user)
                .category(category)
                .type(req.getType())
                .title(req.getTitle())
                .description(req.getDescription())
                .location(req.getLocation())
                .images(req.getImages())
                .contact(req.getContact())
                .status(STATUS_SEARCHING)
                .eventTime(req.getEventTime())
                .build();
        item = itemRepository.save(item);
        return toItemVO(item, 0);
    }

    public PageResponse<ItemVO> list(String keyword, Long categoryId, Integer type, Integer status, int page, int size) {
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (status != null) {
                preds.add(cb.equal(root.get("status"), status));
            } else {
                preds.add(cb.equal(root.get("status"), STATUS_SEARCHING));
            }
            if (type != null) preds.add(cb.equal(root.get("type"), type));
            if (categoryId != null) preds.add(cb.equal(root.get("category").get("id"), categoryId));
            if (StringUtils.hasText(keyword)) {
                String k = "%" + keyword + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("title")), k.toLowerCase()),
                        cb.like(cb.lower(root.get("description")), k.toLowerCase()),
                        cb.like(cb.lower(root.get("location")), k.toLowerCase())
                ));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> p = itemRepository.findAll(spec, pageable);
        List<ItemVO> content = p.getContent().stream()
                .map(i -> toItemVO(i, commentRepository.findByItemIdOrderByCreatedAtAsc(i.getId()).size()))
                .collect(Collectors.toList());
        return PageResponse.<ItemVO>builder()
                .content(content)
                .total(p.getTotalElements())
                .page(p.getNumber())
                .size(p.getSize())
                .totalPages(p.getTotalPages())
                .build();
    }

    public ItemVO getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        int cc = commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size();
        return toItemVO(item, cc);
    }

    @Transactional
    public ItemVO updateStatus(Long itemId, Long userId, Integer newStatus) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (!item.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        item.setStatus(newStatus);
        item = itemRepository.save(item);
        return toItemVO(item, commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size());
    }

    public List<ItemVO> listMyItems(Long userId, int type) {
        List<Item> items = itemRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        return items.stream()
                .map(i -> toItemVO(i, commentRepository.findByItemIdOrderByCreatedAtAsc(i.getId()).size()))
                .collect(Collectors.toList());
    }

    public void delete(Long itemId, Long operatorId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (!isAdmin && !item.getUser().getId().equals(operatorId)) {
            throw new BusinessException(403, "无权操作");
        }
        itemRepository.delete(item);
    }

    private ItemVO toItemVO(Item item, int commentCount) {
        return ItemVO.builder()
                .id(item.getId())
                .userId(item.getUser().getId())
                .userNickname(item.getUser().getNickname())
                .userAvatarUrl(item.getUser().getAvatarUrl())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .type(item.getType())
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .images(item.getImages())
                .contact(item.getContact())
                .status(item.getStatus())
                .eventTime(item.getEventTime())
                .createdAt(item.getCreatedAt())
                .commentCount(commentCount)
                .build();
    }
}
