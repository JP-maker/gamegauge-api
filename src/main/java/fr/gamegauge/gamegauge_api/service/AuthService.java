package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service gérant la logique métier liée à l'authentification (inscription, connexion).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur pour l'injection de dépendances.
     *
     * @param userRepository Le repository pour accéder aux données des utilisateurs.
     * @param passwordEncoder L'encodeur pour hacher les mots de passe.
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gère l'inscription d'un nouvel utilisateur.
     *
     * @param registerRequest Les données d'inscription fournies par l'utilisateur.
     * @throws IllegalStateException si le nom d'utilisateur ou l'email est déjà pris.
     */
    public void registerUser(RegisterRequest registerRequest) {
        // 1. Vérifier si le nom d'utilisateur ou l'email existe déjà
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalStateException("Erreur : Le nom d'utilisateur est déjà pris !");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("Erreur : L'email est déjà utilisé !");
        }

        // 2. Créer une nouvelle instance de l'utilisateur
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());

        // 3. Hasher le mot de passe avant de le sauvegarder
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // 4. Définir les valeurs par défaut (la vérification d'email sera gérée plus tard)
        user.setEmailVerified(false);
        // user.setVerificationToken(...); // On ajoutera la génération du token ici

        // 5. Sauvegarder le nouvel utilisateur dans la base de données
        userRepository.save(user);
    }
}