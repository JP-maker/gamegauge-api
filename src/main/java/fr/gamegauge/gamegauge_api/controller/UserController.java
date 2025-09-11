package fr.gamegauge.gamegauge_api.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur pour les ressources protégées liées à l'utilisateur.
 * Tous les endpoints de ce contrôleur nécessitent une authentification.
 */
@RestController
@RequestMapping("/api/users") // Nouveau chemin de base pour ce contrôleur
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @GetMapping("/profile")
    public ResponseEntity<String> userProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        logger.info("Accès au profil par l'utilisateur authentifié : {}", currentPrincipalName);
        return ResponseEntity.ok("Bienvenue sur votre profil, " + currentPrincipalName + " !");
    }
}