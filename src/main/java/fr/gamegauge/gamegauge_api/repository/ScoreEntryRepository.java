package fr.gamegauge.gamegauge_api.repository;

import fr.gamegauge.gamegauge_api.model.ScoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
}