package com.example.server.websocket;

import com.example.server.entity.Message;
import com.example.server.entity.User;
import com.example.server.repository.MessageRepository;
import com.example.server.service.UserService;
import com.example.server.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null || !jwtUtil.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Long userId = jwtUtil.getUserId(token);
        SESSIONS.put(userId, session);
        log.info("WebSocket connected: userId={}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null || !jwtUtil.validateToken(token)) return;
        Long senderId = jwtUtil.getUserId(token);

        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            Long receiverId = node.path("receiverId").asLong();
            String content = node.path("content").asText();
            int msgType = node.path("msgType").asInt(0);

            User sender = userService.findById(senderId);
            User receiver = userService.findById(receiverId);
            Message msg = Message.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .content(content)
                    .msgType(msgType)
                    .isRead(0)
                    .build();
            msg = messageRepository.save(msg);

            var resp = Map.of(
                    "type", "message",
                    "id", msg.getId(),
                    "senderId", senderId,
                    "receiverId", receiverId,
                    "content", content,
                    "msgType", msgType,
                    "createdAt", msg.getCreatedAt().toString()
            );
            String json = objectMapper.writeValueAsString(resp);

            session.sendMessage(new TextMessage(json));
            WebSocketSession target = SESSIONS.get(receiverId);
            if (target != null && target.isOpen()) {
                target.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.warn("Handle message error", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.entrySet().removeIf(e -> e.getValue().equals(session));
    }

    private String getTokenFromSession(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.contains("token=")) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) return param.substring(6);
            }
        }
        return null;
    }
}
