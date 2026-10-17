package uz.aytjanov.dropnest.messaging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.aytjanov.dropnest.messaging.entity.ChatMessage;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
}
