package likelion.mlb.backendProject.domain.user.dto;

import likelion.mlb.backendProject.domain.user.entity.SeasonUserScore;
import likelion.mlb.backendProject.domain.user.entity.User;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class SeasonUserScoreDto {

    private UUID userId;
    private String email;
    private String name;
    private Integer score;
    private Integer points;

    public static List<SeasonUserScoreDto> toDto(List<SeasonUserScore> seasonUserScores) {
        List<SeasonUserScoreDto> dtos = new ArrayList<>();

        for (SeasonUserScore seasonUserScore : seasonUserScores) {
            User user = seasonUserScore.getUser();
            SeasonUserScoreDto dto = new SeasonUserScoreDto();
            dto.userId = user.getId();
            dto.email = user.getEmail();
            dto.name = user.getName();
            dto.score = seasonUserScore.getScore();
            dto.points = seasonUserScore.getPoints();

            dtos.add(dto);
        }
        return dtos;
    }
}
