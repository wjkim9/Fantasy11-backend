package likelion.mlb.backendProject.domain.player.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElementTypeDto {
    private UUID id;
    private Integer fplId;
    private String pluralName;
    private String krName;
    private Integer squadMinPlay;
    private Integer squadMaxPlay;
    private Integer elementCount;
}
