package fr.gamegauge.gamegauge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * DTO pour renvoyer les informations d'un tableau de scores.
 */
@Getter
@Setter
@AllArgsConstructor
public class BoardResponse {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private String ownerUsername;
    private List<ParticipantResponse> participants;
}