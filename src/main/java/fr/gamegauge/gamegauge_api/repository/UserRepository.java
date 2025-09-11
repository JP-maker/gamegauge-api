package fr.gamegauge.gamegauge_api.repository;

import fr.gamegauge.gamegauge_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour l'entité {@link User}.
 * Fournit une interface pour les opérations de base de données (CRUD) sur les utilisateurs.
 * Étend {@link JpaRepository} pour bénéficier des implémentations par défaut de Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son adresse email.
     * Spring Data JPA génère automatiquement l'implémentation de cette méthode
     * en se basant sur son nom.
     *
     * @param email L'adresse email de l'utilisateur à rechercher.
     * @return un {@link Optional} contenant l'utilisateur trouvé, ou vide si aucun utilisateur ne correspond.
     */
    Optional<User> findByEmail(String email);

    /**
     * Recherche un utilisateur par son nom d'utilisateur (username).
     *
     * @param username Le nom d'utilisateur à rechercher.
     * @return un {@link Optional} contenant l'utilisateur trouvé, ou vide si aucun utilisateur ne correspond.
     */
    Optional<User> findByUsername(String username);
}