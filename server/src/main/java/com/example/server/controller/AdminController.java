package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.entity.ItemCategory;
import com.example.server.service.AdminService;
import com.example.server.service.ItemCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ItemCategoryService itemCategoryService;

    @GetMapping("/stats")
    public CommonResponse<AdminStatsVO> stats() {
        return CommonResponse.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public CommonResponse<PageResponse<UserVO>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.ok(adminService.listUsers(keyword, page, size));
    }

    @PutMapping("/users/{id}/ban")
    public CommonResponse<UserVO> toggleBan(@PathVariable Long id) {
        return CommonResponse.ok(adminService.toggleBan(id));
    }

    @GetMapping("/items")
    public CommonResponse<PageResponse<ItemVO>> items(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.ok(adminService.listItems(type, status, page, size));
    }

    @PutMapping("/items/{id}")
    public CommonResponse<ItemVO> updateItem(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return CommonResponse.ok(adminService.updateItem(id,
                body.get("title"), body.get("description"), body.get("location"), body.get("contact")));
    }

    @PostMapping("/items/{id}/status")
    public CommonResponse<ItemVO> updateItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        return CommonResponse.ok(adminService.updateItemStatus(id, status));
    }

    @DeleteMapping("/items/{id}")
    public CommonResponse<Void> deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
        return CommonResponse.ok(null);
    }

    @PostMapping("/items/expire")
    public CommonResponse<List<ItemVO>> expireItems(@RequestParam(defaultValue = "30") int days) {
        return CommonResponse.ok(adminService.expireOldItems(days));
    }

    @GetMapping("/reports")
    public CommonResponse<List<ReportVO>> reports(
            @RequestParam(value = "pendingOnly", defaultValue = "false") boolean pendingOnly) {
        return CommonResponse.ok(adminService.listReports(pendingOnly));
    }

    @PutMapping("/reports/{id}/approve")
    public CommonResponse<ReportVO> approveReport(@PathVariable Long id, @RequestParam(required = false) String note) {
        return CommonResponse.ok(adminService.approveReport(id, note));
    }

    @PutMapping("/reports/{id}/reject")
    public CommonResponse<ReportVO> rejectReport(@PathVariable Long id, @RequestParam(required = false) String note) {
        return CommonResponse.ok(adminService.rejectReport(id, note));
    }

    @PutMapping("/reports/{id}/revoke")
    public CommonResponse<ReportVO> revokeReport(@PathVariable Long id) {
        return CommonResponse.ok(adminService.revokeReport(id));
    }

    @GetMapping("/categories")
    public CommonResponse<List<ItemCategory>> listCategories() {
        return CommonResponse.ok(itemCategoryService.listAll());
    }

    @PostMapping("/categories")
    public CommonResponse<ItemCategory> createCategory(@Valid @RequestBody ItemCategoryUpsertRequest req) {
        return CommonResponse.ok(itemCategoryService.create(req));
    }

    @PutMapping("/categories/{id}")
    public CommonResponse<ItemCategory> updateCategory(@PathVariable Long id, @Valid @RequestBody ItemCategoryUpsertRequest req) {
        return CommonResponse.ok(itemCategoryService.update(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public CommonResponse<Void> deleteCategory(@PathVariable Long id) {
        itemCategoryService.delete(id);
        return CommonResponse.ok(null);
    }
}
