package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController  //返回json
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    //发布物品信息  /api/items
    @PostMapping
    public CommonResponse<ItemVO> create(@RequestAttribute Long userId, @Valid @RequestBody ItemCreateRequest req) {
        return CommonResponse.ok(itemService.create(userId, req));
    }

    //查找or筛选 根据搜索栏或顶部栏选择 /api/items
    @GetMapping
    public CommonResponse<PageResponse<ItemVO>> list(
            @RequestParam(required = false) String keyword,   //参数可不传
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,      //前端没传就默认值
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.ok(itemService.list(keyword, categoryId, type, status, page, size));
    }

    //查看物品详细信息 主页/我的发布
    @GetMapping("/{id}")
    public CommonResponse<ItemVO> getById(@RequestAttribute Long userId, @PathVariable Long id) {
        return CommonResponse.ok(itemService.getByIdForViewer(id, userId));
    }

    //更改物品状态   已找回/撤销/取消撤销
    @PutMapping("/{id}/status")
    public CommonResponse<ItemVO> updateStatus(@RequestAttribute Long userId, @PathVariable Long id, @RequestParam Integer status) {
        return CommonResponse.ok(itemService.updateStatus(id, userId, status));
    }

    //获取我发布的丢失物品
    @GetMapping("/my/lost")
    public CommonResponse<java.util.List<ItemVO>> myLost(@RequestAttribute Long userId) {
        return CommonResponse.ok(itemService.listMyItems(userId, 0));
    }

    //获取我发布的招领物品
    @GetMapping("/my/found")
    public CommonResponse<java.util.List<ItemVO>> myFound(@RequestAttribute Long userId) {
        return CommonResponse.ok(itemService.listMyItems(userId, 1));
    }
}
