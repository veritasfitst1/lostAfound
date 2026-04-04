package com.example.server.service;

import com.example.server.dto.ReportVO;
import com.example.server.entity.Item;
import com.example.server.entity.Report;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.ItemRepository;
import com.example.server.repository.ReportRepository;
import com.example.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    private static final int STATUS_PENDING = 0;  //待审核
    private static final int STATUS_APPROVED = 1;  //通过
    private static final int STATUS_REJECTED = 2;   //驳回

    //创建举报信息
    @Transactional(readOnly = false)
    public ReportVO create(Long reporterId, Long reportedUserId, Long reportedItemId, String reason) {
        User reporter = userService.findById(reporterId);
        if (reportedUserId == null && reportedItemId == null) {
            throw new BusinessException(400, "必须举报用户或物品");
        }
        if (reportedUserId != null) {
            userService.findById(reportedUserId);
        }
        if (reportedItemId != null) {
            itemRepository.findById(reportedItemId).orElseThrow(() -> new BusinessException(404, "物品不存在"));
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUserId != null ? userService.findById(reportedUserId) : null)
                .reportedItem(reportedItemId != null ? itemRepository.findById(reportedItemId).orElse(null) : null)
                .reason(reason)
                .status(STATUS_PENDING)
                .build();
        report = reportRepository.save(report);
        return toReportVO(report);
    }

    public List<ReportVO> listPending() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(STATUS_PENDING).stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());
    }

    //查询举报列表
    public List<ReportVO> listAll() {
        return reportRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toReportVO)
                .collect(Collectors.toList());
    }

    //通过举报：待处理时先处理物品(置为已过期)再处理被举报用户(封禁状态切换)；重复通过不再执行副作用
    @Transactional(readOnly = false)
    public ReportVO approve(Long reportId, Long adminId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "举报不存在"));
        boolean wasPending = report.getStatus() == STATUS_PENDING;
        if (wasPending) {
            applyApproveSideEffects(report);
        }
        report.setStatus(STATUS_APPROVED);
        report.setAdminNote(adminNote);
        report = reportRepository.save(report);
        return toReportVO(report);
    }

    /** 先物品后用户；不切换管理员账号状态 */
    private void applyApproveSideEffects(Report report) {
        if (report.getReportedItem() != null) {
            Long itemId = report.getReportedItem().getId();
            itemRepository.findById(itemId).ifPresent(item -> {
                item.setStatus(3);
                itemRepository.save(item);
            });
        }
        if (report.getReportedUser() != null) {
            User u = userRepository.findById(report.getReportedUser().getId()).orElse(null);
            if (u != null && !"ADMIN".equals(u.getRole())) {
                u.setStatus(u.getStatus() != null && u.getStatus() == 0 ? 1 : 0);
                userRepository.save(u);
            }
        }
    }

    //拒绝举报
    @Transactional(readOnly = false)
    public ReportVO reject(Long reportId, Long adminId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "举报不存在"));
        report.setStatus(STATUS_REJECTED);
        report.setAdminNote(adminNote);
        report = reportRepository.save(report);
        return toReportVO(report);
    }

    /** 撤销举报：将已通过/已驳回的举报重置为待审核，并回退通过时的副作用 */
    @Transactional(readOnly = false)
    public ReportVO revoke(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "举报不存在"));
        if (report.getStatus() == STATUS_PENDING) {
            throw new BusinessException(400, "该举报已是待审核状态");
        }
        if (report.getStatus() == STATUS_APPROVED) {
            revokeApproveSideEffects(report);
        }
        report.setStatus(STATUS_PENDING);
        report.setAdminNote(null);
        report = reportRepository.save(report);
        return toReportVO(report);
    }

    /** 回退通过时的副作用：物品恢复寻找中，用户解封 */
    private void revokeApproveSideEffects(Report report) {
        if (report.getReportedItem() != null) {
            itemRepository.findById(report.getReportedItem().getId()).ifPresent(item -> {
                if (item.getStatus() == 3) {
                    item.setStatus(0);
                    itemRepository.save(item);
                }
            });
        }
        if (report.getReportedUser() != null) {
            User u = userRepository.findById(report.getReportedUser().getId()).orElse(null);
            if (u != null && !"ADMIN".equals(u.getRole()) && u.getStatus() != null && u.getStatus() == 1) {
                u.setStatus(0);
                userRepository.save(u);
            }
        }
    }

    //统计待处理举报数量
    public long countPending() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(STATUS_PENDING).size();
    }

    //返回给前端用的举报信息
    private ReportVO toReportVO(Report r) {
        return ReportVO.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .reporterNickname(r.getReporter().getNickname())
                .reportedUserId(r.getReportedUser() != null ? r.getReportedUser().getId() : null)
                .reportedUserNickname(r.getReportedUser() != null ? r.getReportedUser().getNickname() : null)
                .reportedItemId(r.getReportedItem() != null ? r.getReportedItem().getId() : null)
                .reportedItemTitle(r.getReportedItem() != null ? r.getReportedItem().getTitle() : null)
                .reason(r.getReason())
                .status(r.getStatus())
                .adminNote(r.getAdminNote())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
