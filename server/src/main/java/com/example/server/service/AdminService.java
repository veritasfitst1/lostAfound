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

    public AdminStatsVO getStats() {
        long totalUsers = userRepository.count();
        long totalItems = itemRepository.count();
        long lostCount = itemRepository.countByType(TYPE_LOST);
        long foundCount = itemRepository.countByType(TYPE_FOUND);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayItems = itemRepository.findAll().stream()
                .filter(i -> i.getCreatedAt().isAfter(todayStart))
                .count();
        long pendingReports = reportService.countPending();

        List<Map<String, Object>> categoryDist = new ArrayList<>();
        itemRepository.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getCategory().getName(), Collectors.counting()))
                .forEach((name, cnt) -> categoryDist.add(Map.<String, Object>of("name", name, "value", cnt)));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.plusDays(1).atStartOfDay();
            long cnt = itemRepository.findAll().stream()
                    .filter(item -> item.getCreatedAt().isAfter(start) && item.getCreatedAt().isBefore(end))
                    .count();
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

    public PageResponse<UserVO> listUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<User> all = userRepository.findAll();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.toLowerCase();
            all = all.stream()
                    .filter(u -> (u.getNickname() != null && u.getNickname().toLowerCase().contains(k))
                            || (u.getUsername() != null && u.getUsername().toLowerCase().contains(k))
                            || (u.getOpenid() != null && u.getOpenid().contains(k)))
                    .collect(Collectors.toList());
        }
        int from = page * size;
        int to = Math.min(from + size, all.size());
        List<User> sub = from < all.size() ? all.subList(from, to) : List.of();
        return PageResponse.<UserVO>builder()
                .content(sub.stream().map(userService::toUserVO).collect(Collectors.toList()))
                .total(all.size())
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) all.size() / size))
                .build();
    }

    @Transactional(readOnly = false)
    public UserVO toggleBan(Long userId) {
        User user = userService.findById(userId);
        user.setStatus(user.getStatus() == 0 ? 1 : 0);
        user = userRepository.save(user);
        return userService.toUserVO(user);
    }

    public PageResponse<ItemVO> listItems(Integer type, Integer status, int page, int size) {
        return itemService.adminList(null, null, type, status, page, size);
    }

    @Transactional(readOnly = false)
    public void deleteItem(Long itemId) {
        itemService.delete(itemId, null, true);
    }

    @Transactional(readOnly = false)
    public List<ItemVO> expireOldItems(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<Item> items = itemRepository.findAll().stream()
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

    @Transactional(readOnly = false)
    public ItemVO updateItem(Long itemId, String title, String description, String location, String contact) {
        return itemService.adminUpdate(itemId, title, description, location, contact);
    }

    @Transactional(readOnly = false)
    public ItemVO updateItemStatus(Long itemId, Integer status) {
        return itemService.adminUpdateStatus(itemId, status);
    }

    public List<ReportVO> listReports(boolean pendingOnly) {
        return pendingOnly ? reportService.listPending() : reportService.listAll();
    }

    @Transactional(readOnly = false)
    public ReportVO approveReport(Long reportId, String note) {
        return reportService.approve(reportId, null, note);
    }

    @Transactional(readOnly = false)
    public ReportVO rejectReport(Long reportId, String note) {
        return reportService.reject(reportId, null, note);
    }

    @Transactional(readOnly = false)
    public ReportVO revokeReport(Long reportId) {
        return reportService.revoke(reportId);
    }
}
