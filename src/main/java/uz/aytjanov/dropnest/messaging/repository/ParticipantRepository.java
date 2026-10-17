package uz.aytjanov.dropnest.messaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.aytjanov.dropnest.messaging.entity.ConversationParticipant;

public interface ParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
}