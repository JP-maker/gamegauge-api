package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.LoginRequest;
import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.dto.response.JwtAuthenticationResponse;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// Nouveaux imports
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // Ajout

    // Mettre à jour le constructeur
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     *
     * @param registerRequest Les informations d'inscription (nom d'utilisateur, email, mot de passe).
     */
    public void registerUser(RegisterRequest registerRequest) {
        logger.info("Tentative d'inscription pour l'utilisateur : {}", registerRequest.getUsername());

        try {
            if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                logger.warn("Le nom d'utilisateur {} est déjà pris.", registerRequest.getUsername());
                throw new IllegalStateException("Erreur : Le nom d'utilisateur est déjà pris !");
            }

            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                logger.warn("L'email {} est déjà utilisé.", registerRequest.getEmail());
                throw new IllegalStateException("Erreur : L'email est déjà utilisé !");
            }

            logger.debug("Les vérifications pour l'utilisateur {} sont passées avec succès.", registerRequest.getUsername());

            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setEmailVerified(false);

            userRepository.save(user);

            logger.info("Utilisateur {} enregistré avec succès.", user.getUsername());

        } catch (IllegalStateException e) {
            // On log l'erreur et on la relance pour que le GlobalExceptionHandler la prenne en charge
            logger.error("Erreur lors de l'inscription de l'utilisateur {}: {}", registerRequest.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log pour toute autre erreur inattendue
            logger.error("Erreur inattendue lors de l'inscription pour l'utilisateur {}", registerRequest.getUsername(), e);
            throw new RuntimeException("Une erreur inattendue est survenue lors de l'inscription.");
        }
    }

    /**
     * Authentifie un utilisateur et retourne un token JWT.
     *
     * @param loginRequest Les informations de connexion (email, mot de passe).
     * @return La réponse contenant le token JWT.
     */
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        logger.info("Tentative de connexion pour l'utilisateur : {}", loginRequest.getEmail());

        // 1. Déclencher le mécanisme d'authentification standard de Spring Security
        // S'il y a une erreur (mauvais mot de passe...), une exception sera levée.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        logger.info("Authentification réussie pour : {}", loginRequest.getEmail());

        // 2. Si l'authentification réussit, on récupère l'utilisateur
        var user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé après authentification."));

        // On crée un UserDetails pour le passer au service JWT
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(java.util.Collections.emptyList())
                .build();

        // 3. On génère le token
        String jwtToken = jwtService.generateToken(userDetails);
        logger.debug("Token JWT généré pour l'utilisateur : {}", user.getEmail());

        return new JwtAuthenticationResponse(jwtToken);
    }
}