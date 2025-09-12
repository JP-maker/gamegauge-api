package fr.gamegauge.gamegauge_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantAddRequest {
    @NotBlank(message = "Le nom du participant ne peut pas être vide.")
    @Size(min = 1, max = 50, message = "Le nom doit contenir entre 1 et 50 caractères.")
    private String name;
}