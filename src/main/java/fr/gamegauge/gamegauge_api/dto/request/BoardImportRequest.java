package fr.gamegauge.gamegauge_api.dto.request;

import fr.gamegauge.gamegauge_api.model.ScoreCondition;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BoardImportRequest {
    private String name;
    private Integer targetScore;
    private ScoreCondition scoreCondition;
    private Integer numberOfRounds;
    private List<ParticipantImportDto> participants;

    @Getter
    @Setter
    public static class ParticipantImportDto {
        private String name;
        private List<ScoreEntryImportDto> scores;
    }

    @Getter
    @Setter
    public static class ScoreEntryImportDto {
        private int scoreValue;
        private int roundNumber;
    }
}