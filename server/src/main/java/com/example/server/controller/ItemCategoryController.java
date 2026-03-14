package com.example.server.controller;

import com.example.server.dto.CommonResponse;
import com.example.server.entity.ItemCategory;
import com.example.server.service.ItemCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ItemCategoryController {

    private final ItemCategoryService categoryService;

    @GetMapping
    public CommonResponse<List<ItemCategory>> list() {
        return CommonResponse.ok(categoryService.listAll());
    }
}
