package fr.gamegauge.gamegauge_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

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

    // Nous ajouterons les autres champs (target_score, etc.) plus tard pour rester simple pour le moment.

    @CreationTimestamp // Géré automatiquement par Hibernate.
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp // Géré automatiquement par Hibernate.
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Note : La relation avec les Participants et les ScoreEntries sera ajoutée dans les prochaines étapes.
}