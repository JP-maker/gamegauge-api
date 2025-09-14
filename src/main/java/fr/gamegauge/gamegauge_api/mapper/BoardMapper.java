package fr.gamegauge.gamegauge_api.mapper;

import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.dto.response.ParticipantResponse;
import fr.gamegauge.gamegauge_api.model.Board;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ParticipantMapper.class}) // uses = {..} permet à ce mapper d'utiliser d'autres mappers
public interface BoardMapper {

    // 1. SIMPLIFIER LE MAPPING PRINCIPAL
    // MapStruct va maintenant utiliser ParticipantMapper (via `uses`) pour convertir la liste de participants.
    @Mapping(source = "owner.username", target = "ownerUsername")
    @Mapping(source = "participants", target = "participants") // Mapping direct
    BoardResponse toBoardResponse(Board board);

    List<BoardResponse> toBoardResponseList(List<Board> boards);

    /**
     * Méthode exécutée APRÈS le mapping de base.
     * Nous l'utilisons pour appliquer une logique personnalisée, comme le tri.
     *
     * @param boardResponse  Le DTO cible, déjà mappé.
     */
    @AfterMapping
    default void sortParticipantsByScore(@MappingTarget BoardResponse boardResponse) {
        if (boardResponse != null && boardResponse.getParticipants() != null) {
            boardResponse.getParticipants().sort(
                    Comparator.comparingInt(ParticipantResponse::getTotalScore).reversed()
            );
        }
    }
}