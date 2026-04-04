package com.example.server.controller;

import com.example.server.dto.CommonResponse;
import com.example.server.dto.ConversationVO;
import com.example.server.dto.MessageVO;
import com.example.server.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    //获取该用户的会话列表
    @GetMapping("/conversations")
    public CommonResponse<List<ConversationVO>> conversations(@RequestAttribute Long userId) {
        return CommonResponse.ok(messageService.getConversations(userId));
    }

    //获取该用户与另一个用户的会话
    @GetMapping("/conversation/{otherUserId}")
    public CommonResponse<List<MessageVO>> conversation(@RequestAttribute Long userId, @PathVariable Long otherUserId) {
        return CommonResponse.ok(messageService.getConversation(userId, otherUserId));
    }

    //当前用户作为接收方，未读的消息总数
    @GetMapping("/unread-count")
    public CommonResponse<Long> unreadCount(@RequestAttribute Long userId) {
        return CommonResponse.ok(messageService.getUnreadCount(userId));
    }
}
