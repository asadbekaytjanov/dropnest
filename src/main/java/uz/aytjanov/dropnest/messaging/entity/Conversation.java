package uz.aytjanov.dropnest.messaging.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "conversations")
@Getter
@Setter
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;
    @Column(unique = true)
    private String directKey;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatMessage message;
    private Instant lastMessageAt;
}