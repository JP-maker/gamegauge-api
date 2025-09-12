package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.BoardCreateRequest;
import fr.gamegauge.gamegauge_api.dto.request.BoardUpdateRequest;
import fr.gamegauge.gamegauge_api.dto.request.ParticipantAddRequest;
import fr.gamegauge.gamegauge_api.dto.request.ParticipantUpdateRequest;
import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.dto.response.ParticipantResponse;
import fr.gamegauge.gamegauge_api.exception.ResourceNotFoundException;
import fr.gamegauge.gamegauge_api.exception.UnauthorizedException;
import fr.gamegauge.gamegauge_api.model.Board;
import fr.gamegauge.gamegauge_api.model.Participant;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.BoardRepository;
import fr.gamegauge.gamegauge_api.repository.ParticipantRepository;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + userEmail));

        // 2. Créer la nouvelle entité Board.
        Board board = new Board();
        board.setName(request.getName());
        board.setOwner(owner);

        // 3. Sauvegarder dans la base de données.
        Board savedBoard = boardRepository.save(board);
        logger.info("Tableau '{}' (ID: {}) créé avec succès.", savedBoard.getName(), savedBoard.getId());

        // 4. Mapper l'entité sauvegardée vers un DTO de réponse et le retourner.
        return mapBoardToBoardResponse(savedBoard);
    }

    /**
     * Récupère tous les tableaux de scores d'un utilisateur.
     *
     * @param userEmail L'email de l'utilisateur authentifié.
     * @return Une liste de DTOs représentant les tableaux de l'utilisateur.
     */
    public List<BoardResponse> getBoardsForUser(String userEmail) {
        logger.debug("Récupération des tableaux pour l'utilisateur {}", userEmail);
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + userEmail));

        List<Board> boards = boardRepository.findByOwner(owner);

        // On mappe chaque entité Board en BoardResponse
        return boards.stream()
                .map(this::mapBoardToBoardResponse)
                .collect(Collectors.toList());
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

        return mapBoardToBoardResponse(board);
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
        Board updatedBoard = boardRepository.save(board);
        logger.info("Tableau ID {} mis à jour avec succès.", updatedBoard.getId());

        return mapBoardToBoardResponse(updatedBoard);
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
        return new ParticipantResponse(savedParticipant.getId(), savedParticipant.getName());
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

        return new ParticipantResponse(savedParticipant.getId(), savedParticipant.getName());
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
        // Mapper la liste des entités Participant en une liste de DTOs ParticipantResponse
        List<ParticipantResponse> participantResponses = board.getParticipants().stream()
                .map(participant -> new ParticipantResponse(participant.getId(), participant.getName()))
                .collect(Collectors.toList());

        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                board.getOwner().getUsername(),
                participantResponses // Inclure la liste des participants mappés
        );
    }
}