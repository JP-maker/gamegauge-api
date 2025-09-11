package fr.gamegauge.gamegauge_api.controller;

// 1. Importer les classes de Log4j2
import fr.gamegauge.gamegauge_api.dto.request.LoginRequest;
import fr.gamegauge.gamegauge_api.dto.response.JwtAuthenticationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST gérant les points d'entrée (endpoints) pour l'authentification.
 * Inclut l'inscription, la connexion, etc.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 2. Initialiser le logger pour cette classe
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint pour l'authentification d'un utilisateur existant.
     * Mappé sur la méthode POST à l'URL /api/auth/login.
     *
     * @param loginRequest Le corps de la requête contenant l'email et le mot de passe.
     * @return une {@link ResponseEntity} avec le token JWT en cas de succès.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Requête reçue sur /api/auth/login pour l'utilisateur : {}", loginRequest.getEmail());
        JwtAuthenticationResponse response = authService.login(loginRequest);
        logger.info("Connexion réussie pour {}. Réponse 200 OK avec token envoyée.", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour l'inscription d'un nouvel utilisateur.
     * Mappé sur la méthode POST à l'URL /api/auth/register.
     *
     * @param registerRequest Le corps de la requête contenant les informations d'inscription.
     * @return une {@link ResponseEntity} avec un message de succès.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // 3. Ajouter un log à l'entrée de la méthode
        // IMPORTANT : Ne jamais logger d'informations sensibles comme le mot de passe.
        logger.info("Requête reçue sur /api/auth/register pour l'utilisateur : {}", registerRequest.getUsername());

        authService.registerUser(registerRequest);

        // 4. Ajouter un log avant de retourner la réponse
        logger.info("Inscription réussie pour l'utilisateur : {}. Réponse 200 OK envoyée.", registerRequest.getUsername());
        return ResponseEntity.ok("Utilisateur enregistré avec succès !");
    }
}