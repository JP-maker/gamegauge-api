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
 * Représente un tableau de scores.
 * Chaque tableau est lié à un jeu et appartient à un utilisateur.
 */
@Entity
@Table(name = "boards")
@Getter
@Setter
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * L'utilisateur qui possède ce tableau de scores.
     * C'est une relation Plusieurs-à-Un (plusieurs Boards peuvent appartenir à un User).
     * FetchType.LAZY signifie que l'utilisateur ne sera chargé de la BDD que si on y accède explicitement.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false) // Définit la colonne de la clé étrangère.
    private User owner;

    @Column(name = "target_score")
    private Integer targetScore;

    @Enumerated(EnumType.STRING) // Stocke l'enum en tant que chaîne ("HIGHEST_WINS") dans la BDD
    @Column(name = "score_condition")
    private ScoreCondition scoreCondition;

    @Column(name = "number_of_rounds")
    private Integer numberOfRounds;

    @Column(name = "display_order")
    private Integer displayOrder;


    /**
     * La liste des participants associés à ce tableau de scores.
     * C'est une relation Un-à-Plusieurs (un Board a plusieurs Participants).
     * mappedBy = "board" indique que l'entité Participant gère la relation (via son champ "board").
     * cascade = CascadeType.ALL signifie que les opérations (save, delete...) sur un Board
     *   seront propagées à ses Participants.
     * orphanRemoval = true signifie que si un Participant est retiré de cette liste, il sera
     *   supprimé de la base de données.
     */
    @OneToMany(
            mappedBy = "board",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Participant> participants = new ArrayList<>();

    @CreationTimestamp // Géré automatiquement par Hibernate.
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp // Géré automatiquement par Hibernate.
    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- Méthodes utilitaires (bonne pratique) ---
    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setBoard(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setBoard(null);
    }
}