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
    //管理员端仪表盘信息
    private long totalUsers;
    private long totalItems;
    private long lostCount;
    private long foundCount;
    private long todayItems;
    private long pendingReports;  //待处理举报
    private List<Map<String, Object>> categoryDistribution;   //分类分布
    private List<Map<String, Object>> recentTrend;   //最近趋势
}
