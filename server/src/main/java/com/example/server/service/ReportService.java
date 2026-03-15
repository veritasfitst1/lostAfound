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

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    @Transactional
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

    public List<ReportVO> listAll() {
        return reportRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toReportVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportVO approve(Long reportId, Long adminId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "举报不存在"));
        report.setStatus(STATUS_APPROVED);
        report.setAdminNote(adminNote);
        report = reportRepository.save(report);
        return toReportVO(report);
    }

    @Transactional
    public ReportVO reject(Long reportId, Long adminId, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "举报不存在"));
        report.setStatus(STATUS_REJECTED);
        report.setAdminNote(adminNote);
        report = reportRepository.save(report);
        return toReportVO(report);
    }

    public long countPending() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(STATUS_PENDING).size();
    }

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
