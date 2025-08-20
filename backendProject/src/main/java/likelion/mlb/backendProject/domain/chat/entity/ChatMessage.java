package likelion.mlb.backendProject.domain.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "chat_message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType; // USER | SYSTEM | ALERT

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "user_id") // 시스템/봇 메시지는 NULL
    private UUID userId;

    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum MessageType { USER, SYSTEM, ALERT }
}