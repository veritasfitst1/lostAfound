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

    //管理员端仪表盘界面
    @GetMapping("/stats")
    public CommonResponse<AdminStatsVO> stats() {
        return CommonResponse.ok(adminService.getStats());
    }

    //管理员端用户管理界面
    @GetMapping("/users")
    public CommonResponse<PageResponse<UserVO>> users(
            @RequestParam(required = false) String keyword,  //搜索关键词
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.ok(adminService.listUsers(keyword, page, size));
    }

    //管理员端用户管理封禁/解封
    @PutMapping("/users/{id}/ban")
    public CommonResponse<UserVO> toggleBan(@PathVariable Long id) {
        return CommonResponse.ok(adminService.toggleBan(id));
    }

    //管理员端物品管理信息
    @GetMapping("/items")
    public CommonResponse<PageResponse<ItemVO>> items(
            @RequestParam(required = false) Integer type,   //招领or失物
            @RequestParam(required = false) Integer status,  //寻找中 找回 过期 撤销
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CommonResponse.ok(adminService.listItems(type, status, page, size));
    }

    //管理员端物品管理编辑信息
    @PutMapping("/items/{id}")
    public CommonResponse<ItemVO> updateItem(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {  //前端传一个json
        return CommonResponse.ok(adminService.updateItem(id,
                body.get("title"), body.get("description"), body.get("location"), body.get("contact")));
    }

    //管理员端物品管理  过期&恢复
    @PostMapping("/items/{id}/status")
    public CommonResponse<ItemVO> updateItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        return CommonResponse.ok(adminService.updateItemStatus(id, status));
    }

    //管理员端物品管理  删除 删除物品
    @DeleteMapping("/items/{id}")
    public CommonResponse<Void> deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
        return CommonResponse.ok(null);
    }

    //过期处理  批量标记过期
    @PostMapping("/items/expire")
    public CommonResponse<List<ItemVO>> expireItems(@RequestParam(defaultValue = "30") int days) {
        return CommonResponse.ok(adminService.expireOldItems(days));
    }

    //管理员端举报审核界面  待审核 or 全部记录
    @GetMapping("/reports")
    public CommonResponse<List<ReportVO>> reports(
            @RequestParam(value = "pendingOnly", defaultValue = "false") boolean pendingOnly) {   //URL 查询参数 参数名 pendingOnly  前端不传时默认值为 false
            return CommonResponse.ok(adminService.listReports(pendingOnly));
    }

    //管理员端举报审核界面 通过举报
    @PutMapping("/reports/{id}/approve")
    public CommonResponse<ReportVO> approveReport(@PathVariable Long id, @RequestParam(required = false) String note) {   //审核原因 可选
        return CommonResponse.ok(adminService.approveReport(id, note));
    }

    //管理员端举报审核界面 驳回举报
    @PutMapping("/reports/{id}/reject")
    public CommonResponse<ReportVO> rejectReport(@PathVariable Long id, @RequestParam(required = false) String note) {
        return CommonResponse.ok(adminService.rejectReport(id, note));
    }

    //管理员端举报审核界面 撤销之前的操作
    @PutMapping("/reports/{id}/revoke")
    public CommonResponse<ReportVO> revokeReport(@PathVariable Long id) {
        return CommonResponse.ok(adminService.revokeReport(id));
    }

    //管理员端物品分类界面 全部信息
    @GetMapping("/categories")
    public CommonResponse<List<ItemCategory>> listCategories() {
        return CommonResponse.ok(itemCategoryService.listAll());
    }

    //管理员端物品分类界面 创建分类
    @PostMapping("/categories")
    public CommonResponse<ItemCategory> createCategory(@Valid @RequestBody ItemCategoryUpsertRequest req) {
        return CommonResponse.ok(itemCategoryService.create(req));
    }

    //管理员端物品分类界面 编辑分类
    @PutMapping("/categories/{id}")
    public CommonResponse<ItemCategory> updateCategory(@PathVariable Long id, @Valid @RequestBody ItemCategoryUpsertRequest req) {
        return CommonResponse.ok(itemCategoryService.update(id, req));
    }

    //管理员端物品分类界面 删除分类
    @DeleteMapping("/categories/{id}")
    public CommonResponse<Void> deleteCategory(@PathVariable Long id) {
        itemCategoryService.delete(id);
        return CommonResponse.ok(null);
    }
}
