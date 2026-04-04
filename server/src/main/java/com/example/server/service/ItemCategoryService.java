package com.example.server.service;

import com.example.server.dto.ItemCategoryUpsertRequest;
import com.example.server.entity.ItemCategory;
import com.example.server.exception.BusinessException;
import com.example.server.repository.ItemCategoryRepository;
import com.example.server.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemCategoryService {

    private final ItemCategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    //查询所有分类
    public List<ItemCategory> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    //创建分类
    @Transactional
    public ItemCategory create(ItemCategoryUpsertRequest req) {
        ItemCategory c = ItemCategory.builder()
                .name(req.getName().trim())
                .icon(StringUtils.hasText(req.getIcon()) ? req.getIcon().trim() : null)
                .sortOrder(req.getSortOrder())
                .build();
        return categoryRepository.save(c);
    }

    //修改分类信息
    @Transactional
    public ItemCategory update(Long id, ItemCategoryUpsertRequest req) {
        ItemCategory c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        c.setName(req.getName().trim());
        c.setIcon(StringUtils.hasText(req.getIcon()) ? req.getIcon().trim() : null);
        c.setSortOrder(req.getSortOrder());
        return categoryRepository.save(c);
    }

    //删除分类
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(404, "分类不存在");
        }
        if (itemRepository.countByCategoryId(id) > 0) {
            throw new BusinessException(400, "该分类下仍有物品，无法删除");
        }
        categoryRepository.deleteById(id);
    }
}
