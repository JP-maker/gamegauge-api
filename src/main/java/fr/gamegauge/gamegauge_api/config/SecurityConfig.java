package fr.gamegauge.gamegauge_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration de la sécurité pour l'application GameGauge.
 * C'est ici que nous définirons les règles de sécurité, le hachage des mots de passe, etc.
 */
@Configuration // Indique à Spring que cette classe contient des configurations de beans.
public class SecurityConfig {

    /**
     * Définit le bean pour l'encodeur de mot de passe.
     * Nous utilisons BCrypt, qui est un algorithme de hachage fort et standard.
     * Ce bean sera ensuite injectable partout où nous aurons besoin d'encoder ou de vérifier un mot de passe.
     *
     * @return une instance de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Nous ajouterons d'autres configurations de sécurité ici plus tard (filtres, autorisations...).
}