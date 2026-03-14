package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
    public CommonResponse<List<ReportVO>> reports() {
        return CommonResponse.ok(adminService.listReports());
    }

    @PutMapping("/reports/{id}/approve")
    public CommonResponse<ReportVO> approveReport(@PathVariable Long id, @RequestParam(required = false) String note) {
        return CommonResponse.ok(adminService.approveReport(id, note));
    }

    @PutMapping("/reports/{id}/reject")
    public CommonResponse<ReportVO> rejectReport(@PathVariable Long id, @RequestParam(required = false) String note) {
        return CommonResponse.ok(adminService.rejectReport(id, note));
    }
}
