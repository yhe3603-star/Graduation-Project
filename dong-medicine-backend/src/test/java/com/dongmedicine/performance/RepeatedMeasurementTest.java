package com.dongmedicine.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@DisplayName("多次测量响应时间")
class RepeatedMeasurementTest extends BasePerformanceTest {

    private MeasurementResult measureEndpoint(String label, String url, String... params) throws Exception {
        var builder = get(url);
        for (int i = 0; i < params.length; i += 2) {
            builder.param(params[i], params[i + 1]);
        }
        org.springframework.test.web.servlet.RequestBuilder request = builder;

        // 预热
        for (int i = 0; i < WARMUP_RUNS; i++) {
            mockMvc.perform(request).andReturn();
        }

        // 测量
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < MEASUREMENT_RUNS; i++) {
            durations.add(measureMillis(request));
        }

        double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = durations.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = durations.stream().mapToLong(Long::longValue).min().orElse(0);
        List<Long> sorted = durations.stream().sorted().toList();
        long p95 = sorted.get((int) (MEASUREMENT_RUNS * 0.95));

        System.out.printf("%s - 平均: %.1fms, P95: %dms, 最小: %dms, 最大: %dms, 所有: %s%n",
                label, avg, p95, min, max, durations);

        return new MeasurementResult(label, avg, p95, min, max, durations);
    }

    private record MeasurementResult(String label, double avg, long p95, long min, long max,
                                     List<Long> all) {
    }

    @Nested
    @DisplayName("植物图鉴列表")
    class PlantListMeasurement {
        @Test
        @DisplayName("植物列表平均响应时间应小于500ms")
        void averageTime() throws Exception {
            MeasurementResult r = measureEndpoint("植物列表",
                    "/api/plants/list", "page", "1", "size", "12");
            assertTrue(r.avg() <= LIST_THRESHOLD_MS,
                    "植物列表平均响应时间 " + r.avg() + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
        }
    }

    @Nested
    @DisplayName("知识库列表")
    class KnowledgeListMeasurement {
        @Test
        @DisplayName("知识库列表平均响应时间应小于500ms")
        void averageTime() throws Exception {
            MeasurementResult r = measureEndpoint("知识库列表",
                    "/api/knowledge/list", "page", "1", "size", "12");
            assertTrue(r.avg() <= LIST_THRESHOLD_MS,
                    "知识库列表平均响应时间 " + r.avg() + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
        }
    }

    @Nested
    @DisplayName("全文搜索")
    class SearchMeasurement {
        @Test
        @DisplayName("全文搜索平均响应时间应小于500ms")
        void averageTime() throws Exception {
            MeasurementResult r = measureEndpoint("全文搜索",
                    "/api/search/suggest", "keyword", "侗");
            assertTrue(r.avg() <= SEARCH_THRESHOLD_MS,
                    "全文搜索平均响应时间 " + r.avg() + "ms 超过 " + SEARCH_THRESHOLD_MS + "ms");
        }
    }

    @Nested
    @DisplayName("首页概览")
    class HomePageMeasurement {
        @Test
        @DisplayName("首页概览接口平均响应时间应小于500ms")
        void averageTime() throws Exception {
            MeasurementResult r = measureEndpoint("首页概览",
                    "/api/stats/overview");
            assertTrue(r.avg() <= LIST_THRESHOLD_MS,
                    "首页概览平均响应时间 " + r.avg() + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
        }
    }

    @Nested
    @DisplayName("传承人列表")
    class InheritorListMeasurement {
        @Test
        @DisplayName("传承人列表平均响应时间应小于500ms")
        void averageTime() throws Exception {
            MeasurementResult r = measureEndpoint("传承人列表",
                    "/api/inheritors/list", "page", "1", "size", "12");
            assertTrue(r.avg() <= LIST_THRESHOLD_MS,
                    "传承人列表平均响应时间 " + r.avg() + "ms 超过 " + LIST_THRESHOLD_MS + "ms");
        }
    }
}
