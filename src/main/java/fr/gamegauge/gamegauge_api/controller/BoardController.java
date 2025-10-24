package fr.gamegauge.gamegauge_api.controller;

import fr.gamegauge.gamegauge_api.dto.request.*;
import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.dto.response.ParticipantResponse;
import fr.gamegauge.gamegauge_api.dto.response.ScoreEntryResponse;
import fr.gamegauge.gamegauge_api.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des tableaux de scores (Boards).
 * Tous les endpoints de ce contrôleur sont protégés et nécessitent une authentification.
 */
@RestController
@RequestMapping("/api/boards") // Chemin de base pour toutes les routes liées aux boards.
@RequiredArgsConstructor
@Tag(name = "Boards", description = "Endpoints pour la gestion des tableaux de scores") // Tag pour grouper les endpoints
@SecurityRequirement(name = "bearerAuth") // Indique que tous les endpoints de ce contrôleur nécessitent une authentification
public class BoardController {

    private static final Logger logger = LogManager.getLogger(BoardController.class);
    private final BoardService boardService;

    /**
     * Endpoint pour récupérer tous les tableaux de scores de l'utilisateur authentifié.
     * Mappé sur GET /api/boards
     *
     * @param authentication L'objet d'authentification de l'utilisateur connecté.
     * @return Une liste des tableaux de scores de l'utilisateur.
     */
    @Operation(summary = "Lister les tableaux de l'utilisateur", description = "Récupère la liste de tous les tableaux appartenant à l'utilisateur authentifié.")
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getUserBoards(Authentication authentication) {
        String userEmail = authentication.getName();
        logger.info("Requête GET /api/boards reçue de l'utilisateur {}", userEmail);

        List<BoardResponse> boards = boardService.getBoardsForUser(userEmail);

        return ResponseEntity.ok(boards);
    }

    /**
     * Endpoint pour récupérer un seul tableau par son ID.
     * Mappé sur GET /api/boards/{boardId}
     *
     * @param boardId        L'ID du tableau passé dans l'URL.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Le DTO du tableau.
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoardById(
            @PathVariable Long boardId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête GET /api/boards/{} reçue de l'utilisateur {}", boardId, userEmail);
        BoardResponse board = boardService.getBoardById(boardId, userEmail);
        return ResponseEntity.ok(board);
    }

    /**
     * Endpoint pour créer un nouveau tableau de scores.
     * Mappé sur POST /api/boards
     *
     * @param request        Les données de création du tableau.
     * @param authentication L'objet d'authentification injecté par Spring Security,
     *                       contenant les informations de l'utilisateur connecté.
     * @return Le tableau de scores nouvellement créé avec un statut 201 Created.
     */
    @Operation(summary = "Créer un nouveau tableau de scores", description = "Crée un nouveau tableau pour l'utilisateur authentifié.")
    @ApiResponse(responseCode = "201", description = "Tableau créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données de la requête invalides")
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody BoardCreateRequest request, Authentication authentication) {
        String userEmail = authentication.getName(); // L'email est le "name" de notre principal.
        logger.info("Requête POST /api/boards reçue de l'utilisateur {}", userEmail);

        BoardResponse createdBoard = boardService.createBoard(request, userEmail);

        return new ResponseEntity<>(createdBoard, HttpStatus.CREATED);
    }

    /**
     * Endpoint pour mettre à jour un tableau.
     * Mappé sur PUT /api/boards/{boardId}
     *
     * @param boardId        L'ID du tableau à mettre à jour.
     * @param request        Les nouvelles données.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Le DTO du tableau mis à jour.
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody BoardUpdateRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête PUT /api/boards/{} reçue de l'utilisateur {}", boardId, userEmail);
        BoardResponse updatedBoard = boardService.updateBoard(boardId, request, userEmail);
        return ResponseEntity.ok(updatedBoard);
    }

    /**
     * Endpoint pour supprimer un tableau.
     * Mappé sur DELETE /api/boards/{boardId}
     *
     * @param boardId        L'ID du tableau à supprimer.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Une réponse vide avec un statut 204 No Content.
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Long boardId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête DELETE /api/boards/{} reçue de l'utilisateur {}", boardId, userEmail);
        boardService.deleteBoard(boardId, userEmail);
        return ResponseEntity.noContent().build(); // 204 No Content est la réponse standard pour un DELETE réussi.
    }

    /**
     * Endpoint pour ajouter un participant à un tableau de scores spécifique.
     * Mappé sur POST /api/boards/{boardId}/participants
     *
     * @param boardId        L'ID du tableau cible.
     * @param request        Les données du nouveau participant.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Le DTO du participant créé avec un statut 201 Created.
     */
    @PostMapping("/{boardId}/participants")
    public ResponseEntity<ParticipantResponse> addParticipant(
            @PathVariable Long boardId,
            @Valid @RequestBody ParticipantAddRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête POST /api/boards/{}/participants reçue de l'utilisateur {}", boardId, userEmail);

        ParticipantResponse newParticipant = boardService.addParticipantToBoard(boardId, request, userEmail);

        return new ResponseEntity<>(newParticipant, HttpStatus.CREATED);
    }

    /**
     * Endpoint pour supprimer un participant d'un tableau de scores.
     * Mappé sur DELETE /api/boards/{boardId}/participants/{participantId}
     *
     * @param boardId        L'ID du tableau cible.
     * @param participantId  L'ID du participant à supprimer.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Une réponse vide avec un statut 204 No Content.
     */
    @DeleteMapping("/{boardId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long boardId,
            @PathVariable Long participantId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête DELETE /api/boards/{}/participants/{} reçue de l'utilisateur {}",
                boardId, participantId, userEmail);

        boardService.removeParticipantFromBoard(boardId, participantId, userEmail);

        return ResponseEntity.noContent().build(); // Statut 204
    }

    /**
     * Endpoint pour mettre à jour un participant d'un tableau de scores.
     * Mappé sur PUT /api/boards/{boardId}/participants/{participantId}
     *
     * @param boardId        L'ID du tableau cible.
     * @param participantId  L'ID du participant à mettre à jour.
     * @param request        Les nouvelles données du participant.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Le DTO du participant mis à jour.
     */
    @PutMapping("/{boardId}/participants/{participantId}")
    public ResponseEntity<ParticipantResponse> updateParticipant(
            @PathVariable Long boardId,
            @PathVariable Long participantId,
            @Valid @RequestBody ParticipantUpdateRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête PUT /api/boards/{}/participants/{} reçue de l'utilisateur {}",
                boardId, participantId, userEmail);

        ParticipantResponse updatedParticipant = boardService.updateParticipantInBoard(boardId, participantId, request, userEmail);

        return ResponseEntity.ok(updatedParticipant);
    }

    /**
     * Endpoint pour ajouter une entrée de score à un participant.
     * Mappé sur POST /api/boards/{boardId}/participants/{participantId}/scores
     *
     * @param boardId        L'ID du tableau.
     * @param participantId  L'ID du participant.
     * @param request        Les données du score (valeur et tour).
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Le DTO du score créé avec un statut 201 Created.
     */
    @PutMapping("/{boardId}/participants/{participantId}/scores")
    public ResponseEntity<ScoreEntryResponse> setScore(
            @PathVariable Long boardId,
            @PathVariable Long participantId,
            @Valid @RequestBody ScoreEntryAddRequest request,
            Authentication authentication) {
        logger.info("Requête PUT /api/boards/{}/participants/{}/scores reçue de l'utilisateur {}",
                boardId, participantId, authentication.getName());
        String userEmail = authentication.getName();
        // appeler la nouvelle méthode du service
        ScoreEntryResponse newScore = boardService.setScoreForParticipant(boardId, participantId, request, userEmail);

        return ResponseEntity.ok(newScore);
    }

    /**
     * Endpoint pour supprimer une entrée de score d'un participant.
     * Mappé sur DELETE /api/boards/{boardId}/participants/{participantId}/scores/{scoreId}
     *
     * @param boardId        L'ID du tableau.
     * @param participantId  L'ID du participant.
     * @param scoreId        L'ID du score à supprimer.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Une réponse vide avec un statut 204 No Content.
     */
    @DeleteMapping("/{boardId}/participants/{participantId}/scores/{scoreId}")
    public ResponseEntity<Void> deleteScore(
            @PathVariable Long boardId,
            @PathVariable Long participantId,
            @PathVariable Long scoreId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        logger.info("Requête DELETE /api/boards/{}/participants/{}/scores/{} reçue de l'utilisateur {}",
                boardId, participantId, scoreId, userEmail);

        boardService.deleteScoreFromParticipant(boardId, participantId, scoreId, userEmail);

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint pour mettre à jour l'ordre d'affichage des tableaux de scores.
     * Mappé sur PUT /api/boards/order
     *
     * @param request        Les nouvelles positions des tableaux.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Une réponse vide avec un statut 200 OK.
     */
    @PutMapping("/order")
    public ResponseEntity<Void> updateBoardsOrder(@RequestBody BoardOrderUpdateRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        boardService.updateBoardsOrder(request, userEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint pour importer un tableau de scores à partir d'un JSON.
     * Mappé sur POST /api/boards/import
     *
     * @param request        Les données d'importation du tableau.
     * @param authentication Les infos de l'utilisateur connecté.
     * @return Le DTO du tableau importé avec un statut 201 Created.
     */
    @PostMapping("/import")
    public ResponseEntity<BoardResponse> importBoard(@RequestBody BoardImportRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        BoardResponse importedBoard = boardService.importBoard(request, userEmail);
        return new ResponseEntity<>(importedBoard, HttpStatus.CREATED);
    }

    /**
     * Endpoint pour redémarrer un tableau de scores (remise à zéro des scores).
     * Mappé sur POST /api/boards/{boardId}/restart
     * @param boardId
     * @param authentication
     * @return Une réponse vide avec un statut 200 OK.
     */
    @PostMapping("/{boardId}/restart")
    public ResponseEntity<Void> restartBoard(@PathVariable Long boardId, Authentication authentication) {
        String userEmail = authentication.getName();
        boardService.restartBoard(boardId, userEmail);
        return ResponseEntity.ok().build();
    }
}