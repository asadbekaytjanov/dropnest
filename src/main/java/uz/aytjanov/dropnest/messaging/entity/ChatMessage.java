package uz.aytjanov.dropnest.messaging.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.aytjanov.dropnest.entity.UserEntity;
import java.time.Instant;

@Entity
@Table
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id")
    private UserEntity  recipient;

    @Column(nullable = false, length = 4000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    private String clientMessageId;

    @Column(nullable = false, updatable = false)
    private Instant sentAt = Instant.now();

    private Instant deletedAt;
}