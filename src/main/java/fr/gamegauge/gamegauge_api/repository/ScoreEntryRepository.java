package fr.gamegauge.gamegauge_api.repository;

import fr.gamegauge.gamegauge_api.model.ScoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    /**
     * Supprime en masse toutes les entrées de score associées à un tableau de scores spécifique.
     * Le nom de la méthode est interprété par Spring Data JPA pour créer une requête JPQL.
     * "ByParticipantBoardId" navigue à travers les entités : ScoreEntry -> Participant -> Board -> Id
     *
     * @param boardId L'ID du tableau dont les scores doivent être supprimés.
     */
    void deleteAllByParticipantBoardId(Long boardId);
}