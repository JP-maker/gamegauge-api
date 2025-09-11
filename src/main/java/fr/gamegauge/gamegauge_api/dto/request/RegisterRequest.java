package fr.gamegauge.gamegauge_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) pour transporter les données d'inscription d'un nouvel utilisateur.
 * Utilise les annotations de validation pour s'assurer que les données reçues sont conformes.
 */
@Getter
@Setter
public class RegisterRequest {

    /**
     * Le nom d'utilisateur souhaité.
     * Ne doit pas être vide et doit avoir entre 3 et 50 caractères.
     */
    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide.")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères.")
    private String username;

    /**
     * L'adresse email de l'utilisateur.
     * Doit être un format d'email valide et non vide.
     */
    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    private String email;

    /**
     * Le mot de passe choisi par l'utilisateur.
     * Ne doit pas être vide et doit avoir entre 8 et 120 caractères.
     */
    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Size(min = 8, max = 120, message = "Le mot de passe doit contenir entre 8 et 120 caractères.")
    private String password;
}