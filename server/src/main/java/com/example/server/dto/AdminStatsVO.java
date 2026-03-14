package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsVO {

    private long totalUsers;
    private long totalItems;
    private long lostCount;
    private long foundCount;
    private long todayItems;
    private long pendingReports;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> recentTrend;
}
