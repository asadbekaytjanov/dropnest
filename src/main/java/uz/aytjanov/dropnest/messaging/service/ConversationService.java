package uz.aytjanov.dropnest.messaging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.aytjanov.dropnest.messaging.entity.Conversation;
import uz.aytjanov.dropnest.messaging.repository.ConversationRepository;
import uz.aytjanov.dropnest.messaging.repository.MessageRepository;
import uz.aytjanov.dropnest.repository.UsersRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final MessageRepository messageRepository;
    private final UsersRepository usersRepository;
    private final ConversationRepository conversationRepository;

    public List<Conversation> getConversations(Long userId) {

    }
}