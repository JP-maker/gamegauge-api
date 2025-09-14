package fr.gamegauge.gamegauge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScoreEntryResponse {
    private Long id;
    private int scoreValue;
    private int roundNumber;
}