package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implémentation de l'interface UserDetailsService de Spring Security.
 * Ce service est responsable de charger les détails spécifiques à un utilisateur (par email)
 * depuis la base de données.
 */
@Service // Très important : déclare cette classe comme un bean Spring.
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ApplicationUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Charge un utilisateur par son nom d'utilisateur (qui est l'email dans notre cas).
     *
     * @param email l'email de l'utilisateur à rechercher.
     * @return un objet UserDetails contenant les informations de l'utilisateur.
     * @throws UsernameNotFoundException si aucun utilisateur n'est trouvé avec cet email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // La logique est exactement la même qu'avant.
        fr.gamegauge.gamegauge_api.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        return new User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}