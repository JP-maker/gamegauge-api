package fr.gamegauge.gamegauge_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour la requête de création d'un nouveau tableau de scores.
 */
@Getter
@Setter
public class BoardCreateRequest {

    /**
     * Le nom du tableau de scores.
     * Exemple : "Soirée jeux du Samedi", "Tournoi Smash Bros".
     */
    @NotBlank(message = "Le nom du tableau ne peut pas être vide.")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères.")
    private String name;

    private Integer targetScore;
    private Integer numberOfRounds;
}