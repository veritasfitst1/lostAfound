package com.example.server.controller;

import com.example.server.dto.CommonResponse;
import com.example.server.dto.ReportCreateRequest;
import com.example.server.dto.ReportVO;
import com.example.server.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public CommonResponse<ReportVO> create(@RequestAttribute Long userId, @Valid @RequestBody ReportCreateRequest req) {
        return CommonResponse.ok(reportService.create(userId, req.getReportedUserId(), req.getReportedItemId(), req.getReason()));
    }
}
