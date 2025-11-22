package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.*;
import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.dto.response.ParticipantResponse;
import fr.gamegauge.gamegauge_api.dto.response.ScoreEntryResponse;
import fr.gamegauge.gamegauge_api.exception.ResourceNotFoundException;
import fr.gamegauge.gamegauge_api.exception.UnauthorizedException;
import fr.gamegauge.gamegauge_api.mapper.BoardMapper;
import fr.gamegauge.gamegauge_api.mapper.ParticipantMapper;
import fr.gamegauge.gamegauge_api.model.Board;
import fr.gamegauge.gamegauge_api.model.Participant;
import fr.gamegauge.gamegauge_api.model.ScoreEntry;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.BoardRepository;
import fr.gamegauge.gamegauge_api.repository.ParticipantRepository;
import fr.gamegauge.gamegauge_api.repository.ScoreEntryRepository;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier pour les tableaux de scores (Boards).
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Logger logger = LogManager.getLogger(BoardService.class);

    private final BoardRepository boardRepository;
    private final UserRepository userRepository; // Pour retrouver l'utilisateur propriétaire.
    private final ParticipantRepository participantRepository;
    private final ScoreEntryRepository scoreEntryRepository;
    private final BoardMapper boardMapper;
    private final ParticipantMapper participantMapper;

    /**
     * Crée un nouveau tableau de scores pour un utilisateur donné.
     *
     * @param request       Les données de création du tableau.
     * @param userEmail L'email de l'utilisateur authentifié qui sera le propriétaire.
     * @return Le tableau de scores créé, formaté en DTO.
     */
    public BoardResponse createBoard(BoardCreateRequest request, String userEmail) {
        logger.info("Tentative de création d'un tableau '{}' par l'utilisateur {}", request.getName(), userEmail);

        // 1. Retrouver l'utilisateur propriétaire.
        User owner = getUserByEmail(userEmail);

        // 2. Créer la nouvelle entité Board.
        Board board = new Board();
        board.setName(request.getName());
        board.setOwner(owner);
        board.setTargetScore(request.getTargetScore());
        board.setScoreCondition(request.getScoreCondition());
        board.setNumberOfRounds(request.getNumberOfRounds());

        // 3. Sauvegarder dans la base de données.
        Board savedBoard = boardRepository.save(board);
        logger.info("Tableau '{}' (ID: {}) créé avec succès.", savedBoard.getName(), savedBoard.getId());

        // 4. Mapper l'entité sauvegardée vers un DTO de réponse et le retourner.
        return boardMapper.toBoardResponse(savedBoard);
    }

    /**
     * Récupère tous les tableaux de scores d'un utilisateur.
     *
     * @param userEmail L'email de l'utilisateur authentifié.
     * @return Une liste de DTOs représentant les tableaux de l'utilisateur.
     */
    public List<BoardResponse> getBoardsForUser(String userEmail) {
        logger.debug("Récupération des tableaux pour l'utilisateur {}", userEmail);
        User owner = getUserByEmail(userEmail);

        List<Board> boards = boardRepository.findByOwnerOrderByDisplayOrderAsc(owner);

        // On mappe chaque entité Board en BoardResponse
        return boardMapper.toBoardResponseList(boards);
    }

    /**
     * Récupère un tableau de scores spécifique par son ID, en vérifiant la propriété.
     *
     * @param boardId   L'ID du tableau à récupérer.
     * @param userEmail L'email de l'utilisateur qui fait la demande.
     * @return Le DTO du tableau de scores.
     * @throws ResourceNotFoundException si le tableau n'existe pas.
     * @throws UnauthorizedException     si l'utilisateur n'est pas le propriétaire.
     */
    public BoardResponse getBoardById(Long boardId, String userEmail) {
        logger.debug("Tentative de récupération du tableau ID {} par l'utilisateur {}", boardId, userEmail);
        User user = getUserByEmail(userEmail);

        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        return boardMapper.toBoardResponse(board);
    }

    /**
     * Met à jour le nom d'un tableau de scores.
     *
     * @param boardId   L'ID du tableau à mettre à jour.
     * @param request   Les nouvelles données (ex: nouveau nom).
     * @param userEmail L'email de l'utilisateur qui fait la demande.
     * @return Le DTO du tableau mis à jour.
     */
    public BoardResponse updateBoard(Long boardId, BoardUpdateRequest request, String userEmail) {
        logger.info("Tentative de mise à jour du tableau ID {} par l'utilisateur {}", boardId, userEmail);
        User user = getUserByEmail(userEmail);

        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        board.setName(request.getName());
        board.setTargetScore(request.getTargetScore());
        board.setScoreCondition(request.getScoreCondition());
        board.setNumberOfRounds(request.getNumberOfRounds());

        //Board updatedBoard = boardRepository.save(board);
        logger.info("Tableau ID {} mis à jour avec succès.", board.getId());

        return mapBoardToBoardResponse(board);
    }

    /**
     * Supprime un tableau de scores.
     *
     * @param boardId   L'ID du tableau à supprimer.
     * @param userEmail L'email de l'utilisateur qui fait la demande.
     */
    public void deleteBoard(Long boardId, String userEmail) {
        logger.info("Tentative de suppression du tableau ID {} par l'utilisateur {}", boardId, userEmail);
        User user = getUserByEmail(userEmail);

        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        boardRepository.delete(board);
        logger.info("Tableau ID {} supprimé avec succès.", boardId);
    }

    /**
     * Ajoute un nouveau participant à un tableau de scores existant.
     *
     * @param boardId   L'ID du tableau auquel ajouter le participant.
     * @param request   Les données du participant à ajouter.
     * @param userEmail L'email de l'utilisateur qui effectue l'action (doit être le propriétaire).
     * @return Le DTO du participant nouvellement créé.
     */
    @Transactional // Important pour s'assurer que les modifications sur le board sont bien persistées.
    public ParticipantResponse addParticipantToBoard(Long boardId, ParticipantAddRequest request, String userEmail) {
        logger.info("Tentative d'ajout du participant '{}' au tableau ID {} par l'utilisateur {}",
                request.getName(), boardId, userEmail);

        User user = getUserByEmail(userEmail);
        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        // Créer le nouveau participant
        Participant participant = new Participant();
        participant.setName(request.getName());

        // Ajouter le participant au tableau en utilisant notre méthode utilitaire
        board.addParticipant(participant);

        // Pas besoin de sauvegarder le participant séparément, la sauvegarde du board s'en chargera grâce à la cascade.
        boardRepository.save(board);
        logger.info("Participant '{}' ajouté avec succès au tableau ID {}", request.getName(), boardId);

        // On retourne le dernier participant ajouté
        Participant savedParticipant = board.getParticipants().get(board.getParticipants().size() - 1);
        return participantMapper.toParticipantResponse(savedParticipant);
    }

    /**
     * Supprime un participant d'un tableau de scores.
     *
     * @param boardId       L'ID du tableau dont le participant doit être retiré.
     * @param participantId L'ID du participant à supprimer.
     * @param userEmail     L'email de l'utilisateur qui effectue l'action.
     */
    @Transactional
    public void removeParticipantFromBoard(Long boardId, Long participantId, String userEmail) {
        logger.info("Tentative de suppression du participant ID {} du tableau ID {} par l'utilisateur {}",
                participantId, boardId, userEmail);

        User user = getUserByEmail(userEmail);
        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        // Trouver le participant à supprimer DANS LA LISTE du tableau.
        Participant participantToRemove = board.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant non trouvé dans ce tableau. ID: " + participantId));

        // Utiliser notre méthode d'aide pour retirer le participant.
        // Grâce à orphanRemoval=true dans l'entité Board, Hibernate va
        // automatiquement générer la requête DELETE pour ce participant.
        board.removeParticipant(participantToRemove);

        // La transaction s'assurera que les changements sont sauvegardés à la fin de la méthode.
        // Pas besoin d'appeler boardRepository.save(board) explicitement ici.
        logger.info("Participant ID {} supprimé avec succès du tableau ID {}", participantId, boardId);
    }

    /**
     * Met à jour le nom d'un participant dans un tableau de scores.
     *
     * @param boardId       L'ID du tableau.
     * @param participantId L'ID du participant à mettre à jour.
     * @param request       Les données de mise à jour.
     * @param userEmail     L'email de l'utilisateur qui effectue l'action.
     * @return Le DTO du participant mis à jour.
     */
    public ParticipantResponse updateParticipantInBoard(Long boardId, Long participantId, ParticipantUpdateRequest request, String userEmail) {
        logger.info("Tentative de mise à jour du participant ID {} dans le tableau ID {} par l'utilisateur {}",
                participantId, boardId, userEmail);

        User user = getUserByEmail(userEmail);
        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        // Trouver le participant à mettre à jour dans la liste du tableau.
        Participant participantToUpdate = board.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant non trouvé dans ce tableau. ID: " + participantId));

        // Mettre à jour le nom
        participantToUpdate.setName(request.getName());

        // Sauvegarder l'entité participant mise à jour.
        Participant savedParticipant = participantRepository.save(participantToUpdate);

        logger.info("Participant ID {} mis à jour avec succès. Nouveau nom : {}",
                savedParticipant.getId(), savedParticipant.getName());

        return participantMapper.toParticipantResponse(savedParticipant);
    }

    /**
     * Ajoute une entrée de score pour un participant dans un tableau de scores.
     *
     * @param boardId       L'ID du tableau.
     * @param participantId L'ID du participant.
     * @param request       Les données du score à ajouter.
     * @param userEmail     L'email de l'utilisateur qui effectue l'action.
     * @return Le DTO de l'entrée de score nouvellement créée.
     */
    public ScoreEntryResponse addScoreToParticipant(Long boardId, Long participantId, ScoreEntryAddRequest request, String userEmail) {
        logger.info("Tentative d'ajout d'un score (valeur: {}, tour: {}) pour le participant ID {} dans le tableau ID {}",
                request.getScoreValue(), request.getRoundNumber(), participantId, boardId);

        User user = getUserByEmail(userEmail);
        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        Participant participant = board.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant non trouvé dans ce tableau. ID: " + participantId));

        ScoreEntry scoreEntry = new ScoreEntry();
        scoreEntry.setScoreValue(request.getScoreValue());
        scoreEntry.setRoundNumber(request.getRoundNumber());

        // Utiliser la méthode d'aide pour lier le score au participant
        participant.addScoreEntry(scoreEntry);

        // Sauvegarder explicitement la nouvelle entrée de score
        ScoreEntry savedScoreEntry = scoreEntryRepository.save(scoreEntry);
        logger.info("Score (ID: {}) ajouté avec succès.", savedScoreEntry.getId());

        return new ScoreEntryResponse(savedScoreEntry.getId(), savedScoreEntry.getScoreValue(), savedScoreEntry.getRoundNumber());
    }

    /**
     * Met à jour une entrée de score spécifique.
     *
     * @param boardId       L'ID du tableau.
     * @param participantId L'ID du participant.
     * @param request       Les nouvelles données du score.
     * @param userEmail     L'email de l'utilisateur qui effectue l'action.
     * @return Le DTO du score mis à jour.
     */
    public ScoreEntryResponse setScoreForParticipant(Long boardId, Long participantId, ScoreEntryAddRequest request, String userEmail) {

        logger.info("Définition du score (valeur: {}, tour: {}) pour le participant ID {} dans le tableau ID {}",
                request.getScoreValue(), request.getRoundNumber(), participantId, boardId);

        User user = getUserByEmail(userEmail);
        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("..."));

        Participant participant = board.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("..."));

        // NOUVELLE LOGIQUE : Chercher si un score existe déjà pour ce tour
        Optional<ScoreEntry> existingScoreOpt = participant.getScoreEntries().stream()
                .filter(s -> s.getRoundNumber() == request.getRoundNumber()) // <-- On se base sur le tour
                .findFirst();

        ScoreEntry scoreToSave;
        if (existingScoreOpt.isPresent()) {
            // Si le score existe, on le met à jour
            logger.debug("Mise à jour du score existant pour le tour {}", request.getRoundNumber());
            scoreToSave = existingScoreOpt.get();
            scoreToSave.setScoreValue(request.getScoreValue());
        } else {
            // Sinon, on en crée un nouveau
            logger.debug("Création d'un nouveau score pour le tour {}", request.getRoundNumber());
            scoreToSave = new ScoreEntry();
            scoreToSave.setScoreValue(request.getScoreValue());
            scoreToSave.setRoundNumber(request.getRoundNumber());
            participant.addScoreEntry(scoreToSave);
        }

        ScoreEntry savedScoreEntry = scoreEntryRepository.save(scoreToSave);
        logger.info("Score (ID: {}) défini avec succès.", savedScoreEntry.getId());

        return new ScoreEntryResponse(savedScoreEntry.getId(), savedScoreEntry.getScoreValue(), savedScoreEntry.getRoundNumber());
    }

    /**
     * Supprime une entrée de score spécifique d'un participant.
     *
     * @param boardId       L'ID du tableau.
     * @param participantId L'ID du participant.
     * @param scoreId       L'ID de l'entrée de score à supprimer.
     * @param userEmail     L'email de l'utilisateur qui effectue l'action.
     */
    @Transactional // 1. AJOUTER L'ANNOTATION TRANSACTIONAL
    public void deleteScoreFromParticipant(Long boardId, Long participantId, Long scoreId, String userEmail) {
        logger.info("Tentative de suppression du score ID {} du participant ID {} dans le tableau ID {}",
                scoreId, participantId, boardId);

        User user = getUserByEmail(userEmail);
        Board board = boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        Participant participant = board.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant non trouvé dans ce tableau. ID: " + participantId));

        ScoreEntry scoreToDelete = participant.getScoreEntries().stream()
                .filter(s -> s.getId().equals(scoreId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Entrée de score non trouvée pour ce participant. ID: " + scoreId));

        // 2. MODIFIER LA LOGIQUE DE SUPPRESSION
        // Au lieu de supprimer via le repository, on retire l'élément de la liste du parent.
        // Grâce à orphanRemoval=true, Hibernate générera la requête DELETE pour nous.
        participant.getScoreEntries().remove(scoreToDelete);

        logger.info("Score ID {} supprimé avec succès.", scoreId);

        // Pas besoin d'appeler de .save() ou .delete(). La transaction s'occupe de tout à la fin de la méthode.
    }

    /**
     * Réinitialise un tableau de scores en supprimant toutes les entrées de score.
     *
     * @param boardId   L'ID du tableau à réinitialiser.
     * @param userEmail L'email de l'utilisateur qui doit être le propriétaire.
     */
    @Transactional
    public void restartBoard(Long boardId, String userEmail) {
        logger.info("Tentative de redémarrage du tableau ID {} par l'utilisateur {}", boardId, userEmail);
        User user = getUserByEmail(userEmail);
        // On vérifie toujours que l'utilisateur est bien le propriétaire avant de supprimer.
        boardRepository.findByIdAndOwner(boardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau non trouvé ou accès non autorisé. ID: " + boardId));

        // LA CORRECTION : Une seule ligne pour tout supprimer.
        scoreEntryRepository.deleteAllByParticipantBoardId(boardId);

        logger.info("Tous les scores du tableau ID {} ont été réinitialisés.", boardId);
    }

    @Transactional
    public void updateBoardsOrder(BoardOrderUpdateRequest request, String userEmail) {
        User owner = getUserByEmail(userEmail);
        List<Board> boards = boardRepository.findByOwnerOrderByDisplayOrderAsc(owner);

        // Créer une map pour un accès rapide aux tableaux par ID
        Map<Long, Board> boardMap = boards.stream()
                .collect(Collectors.toMap(Board::getId, Function.identity()));

        // Mettre à jour l'ordre
        int order = 0;
        for (Long boardId : request.getBoardIds()) {
            Board board = boardMap.get(boardId);
            if (board != null && board.getOwner().getId().equals(owner.getId())) { // Sécurité
                board.setDisplayOrder(order++);
                boardRepository.save(board);
            }
        }
    }

    // --- MÉTHODES UTILITAIRES PRIVÉES ---

    /**
     * Méthode utilitaire pour récupérer un utilisateur par email.
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));
    }


    /**
     * Méthode utilitaire pour mapper une entité Board en DTO BoardResponse.
     *
     * @param board L'entité à mapper.
     * @return Le DTO correspondant.
     */
    private BoardResponse mapBoardToBoardResponse(Board board) {
        List<ParticipantResponse> participantResponses = board.getParticipants().stream()
                .map(participant -> {
                    // Pour chaque participant, on mappe ses entrées de score en DTOs
                    List<ScoreEntryResponse> scoreResponses = participant.getScoreEntries().stream()
                            .map(score -> new ScoreEntryResponse(score.getId(), score.getScoreValue(), score.getRoundNumber()))
                            .collect(Collectors.toList());

                    // On calcule le score total
                    int totalScore = participant.getScoreEntries().stream()
                            .mapToInt(ScoreEntry::getScoreValue)
                            .sum();

                    return new ParticipantResponse(participant.getId(), participant.getName(), totalScore, scoreResponses);
                })
                // Trier les participants par score total décroissant
                .sorted((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()))
                .collect(Collectors.toList());

        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getTargetScore(),
                board.getScoreCondition(),
                board.getNumberOfRounds(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                board.getOwner().getUsername(),
                participantResponses
        );
    }

    @Transactional
    public BoardResponse importBoard(BoardImportRequest request, String userEmail) {
        logger.info("Importation d'un tableau local '{}' pour l'utilisateur {}", request.getName(), userEmail);
        User owner = getUserByEmail(userEmail);

        // 1. Créer l'entité Board de base
        Board board = new Board();
        board.setName(request.getName());
        board.setOwner(owner);
        board.setTargetScore(request.getTargetScore());
        board.setScoreCondition(request.getScoreCondition());
        board.setNumberOfRounds(request.getNumberOfRounds());

        // 2. Créer les participants et leurs scores
        if (request.getParticipants() != null) {
            for (BoardImportRequest.ParticipantImportDto pDto : request.getParticipants()) {
                Participant participant = new Participant();
                participant.setName(pDto.getName());

                if (pDto.getScores() != null) {
                    for (BoardImportRequest.ScoreEntryImportDto sDto : pDto.getScores()) {
                        ScoreEntry scoreEntry = new ScoreEntry();
                        scoreEntry.setRoundNumber(sDto.getRoundNumber());
                        scoreEntry.setScoreValue(sDto.getScoreValue());
                        participant.addScoreEntry(scoreEntry);
                    }
                }
                board.addParticipant(participant);
            }
        }

        // 3. Sauvegarder le tout (la cascade s'occupera des participants et scores)
        Board savedBoard = boardRepository.save(board);
        logger.info("Tableau local importé avec succès. Nouvel ID : {}", savedBoard.getId());

        return boardMapper.toBoardResponse(savedBoard);
    }

    /**
     * Duplique un tableau de scores existant, en copiant ses participants mais pas leurs scores.
     *
     * @param boardId   L'ID du tableau à dupliquer.
     * @param userEmail L'email de l'utilisateur qui effectue la duplication.
     * @return Le DTO du nouveau tableau dupliqué.
     */
    @Transactional
    public BoardResponse duplicateBoard(Long boardId, String userEmail) {
        logger.info("Tentative de duplication du tableau ID {} par l'utilisateur {}", boardId, userEmail);
        User owner = getUserByEmail(userEmail);

        // 1. Trouver le tableau original et vérifier la propriété
        Board originalBoard = boardRepository.findByIdAndOwner(boardId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Tableau original non trouvé ou accès non autorisé. ID: " + boardId));

        // 2. Créer la nouvelle entité Board (la copie)
        Board duplicatedBoard = new Board();
        duplicatedBoard.setName(originalBoard.getName() + " (Copie)"); // Ajouter "(Copie)" pour le distinguer
        duplicatedBoard.setOwner(owner);

        // Copier les règles de la partie
        duplicatedBoard.setTargetScore(originalBoard.getTargetScore());
        duplicatedBoard.setScoreCondition(originalBoard.getScoreCondition());
        duplicatedBoard.setNumberOfRounds(originalBoard.getNumberOfRounds());

        // 3. Copier les participants (mais PAS les scores)
        for (Participant originalParticipant : originalBoard.getParticipants()) {
            Participant newParticipant = new Participant();
            newParticipant.setName(originalParticipant.getName());
            // On n'ajoute PAS les scoreEntries, la liste reste vide.
            duplicatedBoard.addParticipant(newParticipant);
        }

        // 4. Sauvegarder la nouvelle entité (la cascade s'occupe de sauvegarder les nouveaux participants)
        Board savedBoard = boardRepository.save(duplicatedBoard);
        logger.info("Tableau dupliqué avec succès. Nouvel ID : {}", savedBoard.getId());

        return boardMapper.toBoardResponse(savedBoard);
    }
}