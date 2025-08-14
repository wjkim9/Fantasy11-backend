package likelion.mlb.backendProject.domain.match.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftStartMessage {
    private String type;       // "DRAFT_START"
    private UUID draftId;
    private short userNumber;  // 1~4
}
