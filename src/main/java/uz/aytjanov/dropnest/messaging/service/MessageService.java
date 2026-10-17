package uz.aytjanov.dropnest.messaging.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import uz.aytjanov.dropnest.entity.UserEntity;
import uz.aytjanov.dropnest.messaging.entity.ChatMessage;
import uz.aytjanov.dropnest.messaging.entity.Conversation;
import uz.aytjanov.dropnest.messaging.exception.UserNotFoundException;
import uz.aytjanov.dropnest.messaging.repository.ConversationRepository;
import uz.aytjanov.dropnest.messaging.repository.MessageRepository;
import uz.aytjanov.dropnest.repository.UsersRepository;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UsersRepository usersRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
    public ChatMessage sendMessage(Long senderId, Long recipientId, String content) {

        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException(senderId));
        UserEntity recipient = usersRepository.findById(recipientId)
                .orElseThrow(() -> new UserNotFoundException(recipientId));

        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("You cannot message yourself");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("ChatMessage cannot be empty");
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setBody(content.trim());
        message.setSentAt(Instant.now());

        return messageRepository.save(message);
    }
    public List<ChatMessage> getMessages(Long currentId, Long otherId) {
        UserEntity current  = usersRepository.findById(currentId)
                .orElseThrow(() -> new UserNotFoundException(currentId));
        UserEntity other  = usersRepository.findById(otherId)
                .orElseThrow(() -> new UserNotFoundException(otherId));
        if (current.getId().equals(other.getId())) {
            throw new IllegalArgumentException("You cannot message yourself");
        }

        Conversation conversation  = conversationRepository.findByConversationParticipantId(currentId);
        if ((conversation == null)) {
            conversationRepository.findByConversationParticipantId(otherId);
        }
        return messageRepository.findAll()
                .stream()
                .toList();
    }
}
