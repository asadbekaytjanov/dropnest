package uz.aytjanov.dropnest.messaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.aytjanov.dropnest.messaging.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Conversation findByConversationParticipantId(Long participantId);
    Conversation findByConversationMessageId(Long messageId);
}