package com.example.server.service;

import com.example.server.dto.AdminStatsVO;
import com.example.server.dto.ItemVO;
import com.example.server.dto.PageResponse;
import com.example.server.dto.ReportVO;
import com.example.server.dto.UserVO;
import com.example.server.entity.Item;
import com.example.server.entity.User;
import com.example.server.repository.ItemRepository;
import com.example.server.repository.ReportRepository;
import com.example.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ReportRepository reportRepository;
    private final ItemService itemService;
    private final ReportService reportService;
    private final UserService userService;

    private static final int TYPE_LOST = 0;
    private static final int TYPE_FOUND = 1;
    private static final int STATUS_SEARCHING = 0;
    private static final int STATUS_EXPIRED = 3;

    //管理员端仪表盘统计信息
    public AdminStatsVO getStats() {
        long totalUsers = userRepository.count();
        long totalItems = itemRepository.count();
        long lostCount = itemRepository.countByType(TYPE_LOST);
        long foundCount = itemRepository.countByType(TYPE_FOUND);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayItems = itemRepository.findAll().stream()
                .filter(i -> i.getCreatedAt().isAfter(todayStart))
                .count();
        long pendingReports = reportService.countPending();  //待审核举报
        //分类分布饼图
        List<Map<String, Object>> categoryDist = new ArrayList<>();
        itemRepository.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getCategory().getName(), Collectors.counting()))
                .forEach((name, cnt) -> categoryDist.add(Map.<String, Object>of("name", name, "value", cnt)));
        //最近七天趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {  //共七天
            LocalDate d = LocalDate.now().minusDays(i);   //今天往前i天
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.plusDays(1).atStartOfDay();
            long cnt = itemRepository.findAll().stream()
                    .filter(item -> item.getCreatedAt().isAfter(start) && item.getCreatedAt().isBefore(end))
                    .count();  //统计第i天数据
            trend.add(Map.<String, Object>of("date", d.toString(), "count", cnt));
        }

        return AdminStatsVO.builder()
                .totalUsers(totalUsers)
                .totalItems(totalItems)
                .lostCount(lostCount)
                .foundCount(foundCount)
                .todayItems(todayItems)
                .pendingReports(pendingReports)
                .categoryDistribution(categoryDist)
                .recentTrend(trend)
                .build();
    }

    //所有or筛选的用户信息
    public PageResponse<UserVO> listUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<User> all = userRepository.findAll();   //所有用户
        if (keyword != null && !keyword.isBlank()) {  //关键词筛选
            String k = keyword.toLowerCase();
            all = all.stream()
                    .filter(u -> (u.getNickname() != null && u.getNickname().toLowerCase().contains(k))  //nickname
                            || (u.getUsername() != null && u.getUsername().toLowerCase().contains(k))   //username包含keyword
                            || (u.getOpenid() != null && u.getOpenid().contains(k)))   //openid
                    .collect(Collectors.toList());
        }
        int from = page * size;   //开始页数
        int to = Math.min(from + size, all.size());  //最后结尾
        List<User> sub = from < all.size() ? all.subList(from, to) : List.of();  //截取数据 如果有数据 → 截取这一页 否则 → 返回空列表
        return PageResponse.<UserVO>builder()
                .content(sub.stream().map(userService::toUserVO).collect(Collectors.toList()))
                .total(all.size())
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) all.size() / size))
                .build();
    }

    //封禁解封转换
    @Transactional(readOnly = false)
    public UserVO toggleBan(Long userId) {
        User user = userService.findById(userId);
        user.setStatus(user.getStatus() == 0 ? 1 : 0);  //封禁-》解封 解封-》封禁
        user = userRepository.save(user);
        return userService.toUserVO(user);
    }

    //物品信息 可以通过选项筛选
    public PageResponse<ItemVO> listItems(Integer type, Integer status, int page, int size) {
        return itemService.adminList(null, null, type, status, page, size);
    }

    //删除物品
    @Transactional(readOnly = false)
    public void deleteItem(Long itemId) {
        itemService.delete(itemId, null, true);
    }

    //批量标记过期
    @Transactional(readOnly = false)
    public List<ItemVO> expireOldItems(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);  //计算过期时间  当前时间-天数
        List<Item> items = itemRepository.findAll().stream()   //批量找过期物品  寻找中-》过期
                .filter(i -> i.getStatus() == STATUS_SEARCHING && i.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());
        items.forEach(i -> {
            i.setStatus(STATUS_EXPIRED);
            itemRepository.save(i);
        });
        return items.stream()
                .map(i -> itemService.getById(i.getId()))
                .collect(Collectors.toList());
    }

    //更新物品信息
    @Transactional(readOnly = false)
    public ItemVO updateItem(Long itemId, String title, String description, String location, String contact) {
        return itemService.adminUpdate(itemId, title, description, location, contact);
    }

    //物品状态转换
    @Transactional(readOnly = false)
    public ItemVO updateItemStatus(Long itemId, Integer status) {
        return itemService.adminUpdateStatus(itemId, status);
    }

    //显示待审核 还是 显示 全部记录
    public List<ReportVO> listReports(boolean pendingOnly) {
        return pendingOnly ? reportService.listPending() : reportService.listAll();
    }

    //举报审核 通过举报
    @Transactional(readOnly = false)
    public ReportVO approveReport(Long reportId, String note) {
        return reportService.approve(reportId, null, note);
    }

    //举报审核 驳回举报
    @Transactional(readOnly = false)
    public ReportVO rejectReport(Long reportId, String note) {
        return reportService.reject(reportId, null, note);
    }

    //举报审核 撤销之前的操作
    @Transactional(readOnly = false)
    public ReportVO revokeReport(Long reportId) {
        return reportService.revoke(reportId);
    }
}
