package fr.gamegauge.gamegauge_api.controller;

import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST gérant les points d'entrée (endpoints) pour l'authentification.
 * Inclut l'inscription, la connexion, etc.
 */
@RestController // Combine @Controller et @ResponseBody, indiquant que les retours des méthodes sont directement sérialisés en JSON.
@RequestMapping("/api/auth") // Définit un préfixe de chemin pour toutes les méthodes de ce contrôleur.
public class AuthController {

    private final AuthService authService;

    /**
     * Constructeur pour l'injection de dépendances du service d'authentification.
     *
     * @param authService Le service qui contient la logique métier de l'authentification.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint pour l'inscription d'un nouvel utilisateur.
     * Mappé sur la méthode POST à l'URL /api/auth/register.
     *
     * @param registerRequest Le corps de la requête contenant les informations d'inscription.
     *                        L'annotation @Valid déclenche la validation définie dans le DTO.
     * @return une {@link ResponseEntity} avec un message de succès.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok("Utilisateur enregistré avec succès !");
    }
}