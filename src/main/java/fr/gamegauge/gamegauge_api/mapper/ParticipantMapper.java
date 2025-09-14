package fr.gamegauge.gamegauge_api.mapper;

import fr.gamegauge.gamegauge_api.dto.response.ParticipantResponse;
import fr.gamegauge.gamegauge_api.dto.response.ScoreEntryResponse;
import fr.gamegauge.gamegauge_api.model.Participant;
import fr.gamegauge.gamegauge_api.model.ScoreEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring") // componentModel="spring" rend le mapper injectable comme un Bean
public interface ParticipantMapper {

    // Méthode principale pour mapper un Participant en ParticipantResponse
    @Mapping(source = "scoreEntries", target = "totalScore", qualifiedByName = "calculateTotalScore")
    @Mapping(source = "scoreEntries", target = "scores")
    ParticipantResponse toParticipantResponse(Participant participant);

    // MapStruct sait automatiquement mapper des listes si on lui dit comment mapper un seul élément
    List<ScoreEntryResponse> toScoreEntryResponseList(List<ScoreEntry> scoreEntries);

    // On n'a pas besoin de définir explicitement le mapping pour ScoreEntry car les noms de champs correspondent
    ScoreEntryResponse toScoreEntryResponse(ScoreEntry scoreEntry);

    // Méthode personnalisée pour calculer le score total
    @Named("calculateTotalScore")
    default int calculateTotalScore(List<ScoreEntry> scoreEntries) {
        if (scoreEntries == null) {
            return 0;
        }
        return scoreEntries.stream().mapToInt(ScoreEntry::getScoreValue).sum();
    }
}