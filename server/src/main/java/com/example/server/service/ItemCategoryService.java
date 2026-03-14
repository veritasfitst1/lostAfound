package com.example.server.service;

import com.example.server.entity.ItemCategory;
import com.example.server.repository.ItemCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemCategoryService {

    private final ItemCategoryRepository categoryRepository;

    public List<ItemCategory> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }
}
