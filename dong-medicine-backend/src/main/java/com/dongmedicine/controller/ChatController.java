package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.dto.ChatRequest;
import com.dongmedicine.dto.ChatResponse;
import com.dongmedicine.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping
    public R<String> chat(@RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.chat(request);
        if (response.isSuccess()) {
            return R.ok(response.getReply());
        } else {
            return R.error(response.getError());
        }
    }
}
