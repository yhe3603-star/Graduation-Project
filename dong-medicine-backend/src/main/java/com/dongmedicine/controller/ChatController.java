package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong successRequests = new AtomicLong(0);
    private static final AtomicLong failedRequests = new AtomicLong(0);

    public static void recordRequest(boolean success) {
        totalRequests.incrementAndGet();
        if (success) {
            successRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
    }

    @GetMapping("/stats")
    public R<ChatStats> stats() {
        return R.ok(new ChatStats(
                totalRequests.get(),
                successRequests.get(),
                failedRequests.get()
        ));
    }

    public record ChatStats(long totalRequests, long successRequests, long failedRequests) {}
}
