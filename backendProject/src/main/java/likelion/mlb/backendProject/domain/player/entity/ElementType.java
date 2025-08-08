package likelion.mlb.backendProject.domain.player.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplElementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "element_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ElementType {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    //fpl의 각 포지션별 id
    @Column(nullable = false)
    private Integer fplId;

    //ex) Goalkeepers, Defenders, fpl에서 받아오는 영어 이름
    @Column(nullable = false)
    private String pluralName;

    @Builder.Default
    @Column(nullable = false)
    private String krName = "";

    //베스트 일레븐에서 해당 포지션을 뽑아야 하는 최소 수
    @Column(nullable = false)
    private Integer squadMinPlay;

    //베스트 일레븐에서 해당 포지션을 뽑을 수 있는 최대 수
    @Column(nullable = false)
    private Integer squadMaxPlay;

    //fpl에 해당 포지션으로 등록된 선수의 수
    @Column(nullable = false)
    private Integer elementCount;

    public static List<ElementType> elementTypeBuilder(List<FplElementType> fplElementTypes) {
        List<ElementType> ets = new ArrayList<>();
        for (FplElementType fplElementType : fplElementTypes) {
            ElementType et = ElementType.builder()
                    .fplId(fplElementType.getId())
                    .pluralName(fplElementType.getPluralName())
                    .squadMinPlay(fplElementType.getSquadMinPlay())
                    .squadMaxPlay(fplElementType.getSquadMaxPlay())
                    .elementCount(fplElementType.getElementCount())
                    .build();
            ets.add(et);
        }
        return ets;
    }
}