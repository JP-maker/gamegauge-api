package fr.gamegauge.gamegauge_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour la requête de mise à jour d'une entrée de score.
 */
@Getter
@Setter
public class ScoreEntryUpdateRequest {
    @NotNull(message = "La valeur du score ne peut pas être nulle.")
    private Integer scoreValue;

    @NotNull(message = "Le numéro du tour ne peut pas être nul.")
    private Integer roundNumber;
}