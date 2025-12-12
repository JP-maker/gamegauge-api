package fr.gamegauge.gamegauge_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur dans le système GameGauge.
 * Cette classe est une entité JPA mappée à la table "users" dans la base de données.
 * Elle contient les informations d'identification et de statut de l'utilisateur.
 */
@Entity
@Table(name = "users") // "user" est un mot-clé SQL, il est plus sûr de nommer la table "users".
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * L'identifiant unique de l'utilisateur.
     * La valeur est générée automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Le nom d'utilisateur choisi. Il doit être unique dans toute l'application.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * L'adresse email de l'utilisateur.
     * Elle est utilisée pour la connexion, la communication et la vérification du compte.
     * Doit être unique.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Le mot de passe de l'utilisateur, stocké sous forme de hash sécurisé.
     * Ne doit jamais être stocké en texte clair.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Un booléen indiquant si l'utilisateur a vérifié son adresse email.
     * Par défaut à 'false' lors de la création du compte.
     */
    @Column(name = "email_verified")
    private boolean emailVerified = false;

    /**
     * Le jeton (token) unique envoyé à l'email de l'utilisateur pour la vérification.
     * Devient nul une fois le compte vérifié.
     */
    @Column(name = "verification_token")
    private String verificationToken;

    /**
     * Le jeton (token) utilisé pour la réinitialisation du mot de passe.
     * Devient nul une fois le mot de passe réinitialisé ou après expiration.
     */
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    /**
     * La date et l'heure d'expiration du jeton de réinitialisation du mot de passe.
     * Permet de s'assurer que le jeton n'est valide que pour une période limitée.
     */
    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

}