package likelion.mlb.backendProject.domain.chat.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
public class ChatRoom {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;
}
