package fr.gamegauge.gamegauge_api.dto.request;

import fr.gamegauge.gamegauge_api.model.ScoreCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardUpdateRequest {
    @NotBlank(message = "Le nom du tableau ne peut pas être vide.")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères.")
    private String name;
    private Integer targetScore;
    private ScoreCondition scoreCondition;
    private Integer numberOfRounds;
}