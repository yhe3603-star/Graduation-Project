package com.dongmedicine.performance;

import com.dongmedicine.integration.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BasePerformanceTest extends BaseIntegrationTest {

    protected static final int WARMUP_RUNS = 3;
    protected static final int MEASUREMENT_RUNS = 10;
    protected static final int CONCURRENT_USERS = 50;

    protected static final long HOME_PAGE_THRESHOLD_MS = 3000;
    protected static final long LIST_THRESHOLD_MS = 500;
    protected static final long SEARCH_THRESHOLD_MS = 500;

    @Autowired
    protected MockMvc mockMvc;

    protected long measureMillis(RequestBuilder request) throws Exception {
        long start = System.nanoTime();
        mockMvc.perform(request).andReturn();
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    protected ConcurrencyResult runConcurrent(int threadCount, Callable<Void> task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);
        List<Long> durations = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    long t0 = System.nanoTime();
                    task.call();
                    durations.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0));
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        long totalStart = System.nanoTime();
        start.countDown();
        done.await(120, TimeUnit.SECONDS);
        long totalDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - totalStart);
        executor.shutdown();

        double avgDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        return new ConcurrencyResult(threadCount, errors.get(), totalDuration, (long) avgDuration);
    }

    protected record ConcurrencyResult(int totalRequests, int errorCount, long totalDurationMs,
                                        long avgDurationMs) {
        @Override
        public String toString() {
            return String.format("总请求=%d, 错误=%d, 总耗时=%dms, 平均=%dms",
                    totalRequests, errorCount, totalDurationMs, avgDurationMs);
        }
    }
}
