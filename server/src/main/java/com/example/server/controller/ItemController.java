package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public CommonResponse<ItemVO> create(@RequestAttribute Long userId, @Valid @RequestBody ItemCreateRequest req) {
        return CommonResponse.ok(itemService.create(userId, req));
    }

    @GetMapping
    public CommonResponse<PageResponse<ItemVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.ok(itemService.list(keyword, categoryId, type, status, page, size));
    }

    @GetMapping("/{id}")
    public CommonResponse<ItemVO> getById(@PathVariable Long id) {
        return CommonResponse.ok(itemService.getById(id));
    }

    @PutMapping("/{id}/status")
    public CommonResponse<ItemVO> updateStatus(@RequestAttribute Long userId, @PathVariable Long id, @RequestParam Integer status) {
        return CommonResponse.ok(itemService.updateStatus(id, userId, status));
    }

    @GetMapping("/my/lost")
    public CommonResponse<java.util.List<ItemVO>> myLost(@RequestAttribute Long userId) {
        return CommonResponse.ok(itemService.listMyItems(userId, 0));
    }

    @GetMapping("/my/found")
    public CommonResponse<java.util.List<ItemVO>> myFound(@RequestAttribute Long userId) {
        return CommonResponse.ok(itemService.listMyItems(userId, 1));
    }
}
