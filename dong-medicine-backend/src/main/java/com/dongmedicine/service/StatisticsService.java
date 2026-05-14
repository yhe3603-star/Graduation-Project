package com.dongmedicine.service;

import java.util.Map;

/**
 * 数据统计服务接口
 * 提供平台数据概览与统计功能
 */
public interface StatisticsService {

    /**
     * 获取平台数据概览
     *
     * @return 概览数据Map
     */
    Map<String, Object> getOverview();

    /**
     * 获取图表统计数据
     *
     * @return 图表数据Map
     */
    Map<String, Object> getChartData();

    /**
     * 获取各实体数量
     *
     * @return 数量Map
     */
    Map<String, Long> getCounts();

    /**
     * 获取访问趋势（最近7天）
     *
     * @return 趋势数据Map，包含dates和counts
     */
    Map<String, Object> getVisitTrend();
}
