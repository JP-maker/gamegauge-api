package fr.gamegauge.gamegauge_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreEntryAddRequest {
    @NotNull(message = "La valeur du score ne peut pas être nulle.")
    private Integer scoreValue;

    @NotNull(message = "Le numéro du tour ne peut pas être nul.")
    private Integer roundNumber;
}