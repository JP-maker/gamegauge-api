package fr.gamegauge.gamegauge_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    private String password;
}