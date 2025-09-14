package fr.gamegauge.gamegauge_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un participant (un joueur) dans un tableau de scores spécifique.
 */
@Entity
@Table(name = "participants")
@Getter
@Setter
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Le tableau de scores auquel ce participant est rattaché.
     * C'est une relation Plusieurs-à-Un (plusieurs Participants pour un Board).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // Pour l'instant, un participant est juste un nom.
    // Plus tard, on pourrait lier un participant à un User s'il est inscrit.

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * La liste des entrées de score pour ce participant.
     * Relation Un-à-Plusieurs.
     */
    @OneToMany(
            mappedBy = "participant",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ScoreEntry> scoreEntries = new ArrayList<>();

    // --- Méthodes utilitaires ---
    public void addScoreEntry(ScoreEntry scoreEntry) {
        scoreEntries.add(scoreEntry);
        scoreEntry.setParticipant(this);
    }
}