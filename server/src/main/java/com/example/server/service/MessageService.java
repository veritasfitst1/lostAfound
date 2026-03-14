package com.example.server.service;

import com.example.server.dto.ConversationVO;
import com.example.server.dto.MessageVO;
import com.example.server.entity.Message;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.MessageRepository;
import com.example.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private static final int MSG_TYPE_TEXT = 0;
    private static final int MSG_TYPE_IMAGE = 1;

    public List<ConversationVO> getConversations(Long userId) {
        List<Message> all = messageRepository.findByUserId(userId);
        Map<Long, List<Message>> byOther = new HashMap<>();
        for (Message m : all) {
            Long other = m.getSender().getId().equals(userId) ? m.getReceiver().getId() : m.getSender().getId();
            byOther.computeIfAbsent(other, k -> new ArrayList<>()).add(m);
        }

        List<ConversationVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Message>> e : byOther.entrySet()) {
            Long otherId = e.getKey();
            List<Message> msgs = e.getValue();
            msgs.sort(Comparator.comparing(Message::getCreatedAt).reversed());
            Message last = msgs.get(0);
            User other = userService.findById(otherId);
            long unread = msgs.stream()
                    .filter(m -> m.getReceiver().getId().equals(userId) && m.getIsRead() == 0)
                    .count();
            result.add(ConversationVO.builder()
                    .otherUserId(other.getId())
                    .otherUserNickname(other.getNickname())
                    .otherUserAvatarUrl(other.getAvatarUrl())
                    .lastMessage(last.getContent())
                    .lastMessageTime(last.getCreatedAt())
                    .unreadCount((int) unread)
                    .build());
        }
        result.sort(Comparator.comparing(ConversationVO::getLastMessageTime).reversed());
        return result;
    }

    @Transactional
    public List<MessageVO> getConversation(Long userId, Long otherUserId) {
        List<Message> msgs = messageRepository.findConversation(userId, otherUserId);
        msgs.stream()
                .filter(m -> m.getReceiver().getId().equals(userId) && m.getIsRead() == 0)
                .forEach(m -> {
                    m.setIsRead(1);
                    messageRepository.save(m);
                });
        return msgs.stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Transactional
    public MessageVO send(Long senderId, Long receiverId, String content, int msgType) {
        User sender = userService.findById(senderId);
        User receiver = userService.findById(receiverId);
        if (receiver.getStatus() == 1) {
            throw new BusinessException(400, "对方账号已被封禁");
        }

        Message msg = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .msgType(msgType)
                .isRead(0)
                .build();
        msg = messageRepository.save(msg);
        return toMessageVO(msg);
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIsRead(userId, 0);
    }

    private MessageVO toMessageVO(Message m) {
        return MessageVO.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderNickname(m.getSender().getNickname())
                .senderAvatarUrl(m.getSender().getAvatarUrl())
                .receiverId(m.getReceiver().getId())
                .receiverNickname(m.getReceiver().getNickname())
                .content(m.getContent())
                .msgType(m.getMsgType())
                .isRead(m.getIsRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
