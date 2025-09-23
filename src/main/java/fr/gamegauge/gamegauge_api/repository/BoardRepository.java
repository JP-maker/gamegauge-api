package fr.gamegauge.gamegauge_api.repository;

import fr.gamegauge.gamegauge_api.model.Board;
import fr.gamegauge.gamegauge_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité {@link Board}.
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * Trouve tous les tableaux de scores appartenant à un utilisateur spécifique.
     *
     * @param owner L'utilisateur propriétaire.
     * @return Une liste des tableaux de scores de cet utilisateur.
     */
    List<Board> findByOwner(User owner);

    /**
     * Trouve un tableau de scores par son ID et son propriétaire.
     * C'est une vérification de sécurité importante pour s'assurer qu'un utilisateur
     * ne peut pas accéder aux tableaux d'un autre.
     *
     * @param id L'ID du tableau.
     * @param owner Le propriétaire du tableau.
     * @return Un Optional contenant le tableau s'il est trouvé.
     */
    Optional<Board> findByIdAndOwner(Long id, User owner);
    List<Board> findByOwnerOrderByDisplayOrderAsc(User owner);
}