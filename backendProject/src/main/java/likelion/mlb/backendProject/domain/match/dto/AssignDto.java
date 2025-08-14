package likelion.mlb.backendProject.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AssignDto {
    private UUID draftId;
    private short userNumber;
}
