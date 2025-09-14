package fr.gamegauge.gamegauge_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Représente une seule entrée de score pour un participant dans un tour donné.
 */
@Entity
@Table(name = "score_entries")
@Getter
@Setter
public class ScoreEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int scoreValue;

    @Column(nullable = false)
    private int roundNumber;

    /**
     * Le participant à qui ce score appartient.
     * Relation Plusieurs-à-Un (plusieurs ScoreEntries pour un Participant).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}