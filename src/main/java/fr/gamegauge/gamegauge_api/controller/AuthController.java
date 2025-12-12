package fr.gamegauge.gamegauge_api.controller;

// 1. Importer les classes de Log4j2
import fr.gamegauge.gamegauge_api.dto.request.LoginRequest;
import fr.gamegauge.gamegauge_api.dto.response.JwtAuthenticationResponse;
import fr.gamegauge.gamegauge_api.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @Operation(summary = "Créer un nouveau compte utilisateur")
    @ApiResponse(responseCode = "200", description = "Utilisateur enregistré avec succès")
    @ApiResponse(responseCode = "409", description = "Le nom d'utilisateur ou l'email est déjà utilisé")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // 3. Ajouter un log à l'entrée de la méthode
        // IMPORTANT : Ne jamais logger d'informations sensibles comme le mot de passe.
        logger.info("Requête reçue sur /api/auth/register pour l'utilisateur : {}", registerRequest.getUsername());

        authService.registerUser(registerRequest);

        // 4. Ajouter un log avant de retourner la réponse
        logger.info("Inscription réussie pour l'utilisateur : {}. Réponse 200 OK envoyée.", registerRequest.getUsername());
        return ResponseEntity.ok(new MessageResponse("Utilisateur enregistré avec succès !"));
    }

    /**
     * Endpoint pour initier la procédure de réinitialisation du mot de passe.
     * Mappé sur la méthode POST à l'URL /api/auth/forgot-password.
     *
     * @param body Le corps de la requête contenant l'email de l'utilisateur.
     * @return une {@link ResponseEntity} avec un message de succès.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        return ResponseEntity.ok("Si l'email existe, un lien a été envoyé.");
    }

    /**
     * Endpoint pour réinitialiser le mot de passe avec un token.
     * Mappé sur la méthode POST à l'URL /api/auth/reset-password.
     *
     * @param body Le corps de la requête contenant le token et le nouveau mot de passe.
     * @return une {@link ResponseEntity} avec un message de succès.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok("Mot de passe modifié avec succès.");
    }
}