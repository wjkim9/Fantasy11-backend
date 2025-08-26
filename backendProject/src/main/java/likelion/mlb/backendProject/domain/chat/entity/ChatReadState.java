package likelion.mlb.backendProject.domain.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "chat_read_state",
    uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReadState {

  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "chat_room_id", nullable = false)
  private UUID chatRoomId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "last_message_id")
  private UUID lastMessageId;

  @Column(name = "last_read_at", nullable = false)
  private Instant lastReadAt;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (lastReadAt == null) {
      lastReadAt = Instant.EPOCH; // 처음엔 1970-01-01
    }
  }

  public void mark(UUID messageId, Instant when) {
    this.lastMessageId = messageId;
    this.lastReadAt = when != null ? when : Instant.now();
  }
}
