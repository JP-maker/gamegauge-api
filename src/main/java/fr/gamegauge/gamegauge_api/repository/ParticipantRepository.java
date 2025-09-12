package fr.gamegauge.gamegauge_api.repository;

import fr.gamegauge.gamegauge_api.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}