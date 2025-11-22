package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.BoardCreateRequest;
import fr.gamegauge.gamegauge_api.dto.request.BoardOrderUpdateRequest;
import fr.gamegauge.gamegauge_api.dto.request.BoardUpdateRequest;
import fr.gamegauge.gamegauge_api.dto.request.ScoreEntryAddRequest;
import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.dto.response.ParticipantResponse;
import fr.gamegauge.gamegauge_api.exception.ResourceNotFoundException;
import fr.gamegauge.gamegauge_api.mapper.BoardMapper;
import fr.gamegauge.gamegauge_api.mapper.ParticipantMapper;
import fr.gamegauge.gamegauge_api.model.Board;
import fr.gamegauge.gamegauge_api.model.Participant;
import fr.gamegauge.gamegauge_api.model.ScoreCondition;
import fr.gamegauge.gamegauge_api.model.ScoreEntry;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.BoardRepository;
import fr.gamegauge.gamegauge_api.repository.ParticipantRepository;
import fr.gamegauge.gamegauge_api.repository.ScoreEntryRepository;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Classe de tests unitaires pour le service {@link BoardService}.
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private UserRepository userRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private ScoreEntryRepository scoreEntryRepository;
    @Mock private BoardMapper boardMapper;
    @Mock private ParticipantMapper participantMapper; // Nécessaire pour certains tests de mappage

    @InjectMocks
    private BoardService boardService;

    private User testUser;
    private Board testBoard;
    private Participant testParticipant;
    private ScoreEntry testScoreEntry;

    /**
     * Initialise les objets de test avant chaque exécution de test.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");

        testBoard = new Board();
        testBoard.setId(10L);
        testBoard.setName("Test Board");
        testBoard.setOwner(testUser);
        testBoard.setTargetScore(100);
        testBoard.setScoreCondition(ScoreCondition.HIGHEST_WINS);
        testBoard.setNumberOfRounds(5);
        testBoard.setCreatedAt(Instant.now());
        testBoard.setUpdatedAt(Instant.now());

        testParticipant = new Participant();
        testParticipant.setId(1L);
        testParticipant.setName("Player One");
        testParticipant.setBoard(testBoard);
        testParticipant.setScoreEntries(new ArrayList<>()); // Doit être mutable

        testScoreEntry = new ScoreEntry();
        testScoreEntry.setId(100L);
        testScoreEntry.setRoundNumber(1);
        testScoreEntry.setScoreValue(50);
        testScoreEntry.setParticipant(testParticipant);

        testBoard.addParticipant(testParticipant);
        testParticipant.addScoreEntry(testScoreEntry);
    }

    /**
     * Teste la récupération d'un tableau de scores par ID et propriété.
     * Scénario: Tableau trouvé et possédé.
     */
    @Test
    @DisplayName("Devrait retourner le BoardResponse quand le tableau est trouvé et possédé")
    void getBoardById_shouldReturnBoardResponse_whenFoundAndOwned() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        BoardResponse mockResponse = new BoardResponse(boardId, testBoard.getName(), testBoard.getTargetScore(), testBoard.getScoreCondition(), testBoard.getNumberOfRounds(), testBoard.getCreatedAt(), testBoard.getUpdatedAt(), testUser.getUsername(), Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));
        when(boardMapper.toBoardResponse(any(Board.class))).thenReturn(mockResponse);

        // WHEN
        BoardResponse actualResponse = boardService.getBoardById(boardId, userEmail);

        // THEN
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(boardId);
        assertThat(actualResponse.getName()).isEqualTo(testBoard.getName());
    }

    /**
     * Teste la récupération d'un tableau inexistant ou non possédé.
     * Scénario: Le tableau n'est pas trouvé.
     */
    @Test
    @DisplayName("Devrait lever ResourceNotFoundException quand le tableau n'existe pas ou n'est pas possédé")
    void getBoardById_shouldThrowResourceNotFoundException_whenBoardDoesNotExist() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long nonExistentBoardId = 99L;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(nonExistentBoardId, testUser)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> boardService.getBoardById(nonExistentBoardId, userEmail));
    }

    /**
     * Teste la création d'un nouveau tableau de scores.
     */
    @Test
    @DisplayName("Devrait créer un nouveau tableau et retourner le BoardResponse")
    void createBoard_shouldSaveAndReturnBoardResponse() {
        // GIVEN
        String userEmail = testUser.getEmail();
        BoardCreateRequest request = new BoardCreateRequest();
        request.setName("New Game");
        request.setTargetScore(200);
        request.setScoreCondition(ScoreCondition.LOWEST_WINS);
        request.setNumberOfRounds(10);

        Board savedBoard = new Board();
        savedBoard.setId(11L);
        savedBoard.setName(request.getName());
        savedBoard.setOwner(testUser);
        savedBoard.setTargetScore(request.getTargetScore());
        savedBoard.setScoreCondition(request.getScoreCondition());
        savedBoard.setNumberOfRounds(request.getNumberOfRounds());

        BoardResponse mockResponse = new BoardResponse(savedBoard.getId(), savedBoard.getName(), savedBoard.getTargetScore(), savedBoard.getScoreCondition(), savedBoard.getNumberOfRounds(), savedBoard.getCreatedAt(), savedBoard.getUpdatedAt(), testUser.getUsername(), Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);
        when(boardMapper.toBoardResponse(any(Board.class))).thenReturn(mockResponse);

        // WHEN
        BoardResponse actualResponse = boardService.createBoard(request, userEmail);

        // THEN
        verify(boardRepository, times(1)).save(any(Board.class));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getName()).isEqualTo(request.getName());
        assertThat(actualResponse.getTargetScore()).isEqualTo(request.getTargetScore());
        assertThat(actualResponse.getScoreCondition()).isEqualTo(request.getScoreCondition());
    }

    /**
     * Teste la mise à jour d'un tableau de scores existant.
     */
    @Test
    @DisplayName("Devrait mettre à jour un tableau et retourner le BoardResponse")
    void updateBoard_shouldUpdateAndReturnBoardResponse() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        BoardUpdateRequest request = new BoardUpdateRequest();
        request.setName("Updated Board Name");
        request.setTargetScore(150);
        request.setScoreCondition(ScoreCondition.LOWEST_WINS);
        request.setNumberOfRounds(7);

        Board updatedBoard = new Board();
        updatedBoard.setId(boardId);
        updatedBoard.setName(request.getName());
        updatedBoard.setOwner(testUser);
        updatedBoard.setTargetScore(request.getTargetScore());
        updatedBoard.setScoreCondition(request.getScoreCondition());
        updatedBoard.setNumberOfRounds(request.getNumberOfRounds());

        BoardResponse mockResponse = new BoardResponse(updatedBoard.getId(), updatedBoard.getName(), updatedBoard.getTargetScore(), updatedBoard.getScoreCondition(), updatedBoard.getNumberOfRounds(), updatedBoard.getCreatedAt(), updatedBoard.getUpdatedAt(), testUser.getUsername(), Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard)); // Retourner l'original pour la modification

        // WHEN
        BoardResponse actualResponse = boardService.updateBoard(boardId, request, userEmail);

        // THEN
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getName()).isEqualTo(request.getName());
        assertThat(actualResponse.getTargetScore()).isEqualTo(request.getTargetScore());
    }

    /**
     * Teste la suppression d'un tableau de scores.
     */
    @Test
    @DisplayName("Devrait supprimer un tableau quand il est trouvé et possédé")
    void deleteBoard_shouldDeleteBoard_whenFoundAndOwned() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // WHEN
        boardService.deleteBoard(boardId, userEmail);

        // THEN
        verify(boardRepository, times(1)).delete(testBoard); // Vérifier que delete est appelé avec le bon objet
    }

    /**
     * Teste l'ajout d'un participant à un tableau.
     */
    @Test
    @DisplayName("Devrait ajouter un participant à un tableau et retourner ParticipantResponse")
    void addParticipantToBoard_shouldAddParticipant() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        fr.gamegauge.gamegauge_api.dto.request.ParticipantAddRequest request = new fr.gamegauge.gamegauge_api.dto.request.ParticipantAddRequest();
        request.setName("New Player");

        Participant newParticipantEntity = new Participant();
        newParticipantEntity.setId(2L);
        newParticipantEntity.setName("New Player");

        ParticipantResponse mockResponse = new ParticipantResponse(2L, "New Player", 0, Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard); // La sauvegarde du board propage la cascade
        when(participantMapper.toParticipantResponse(any(Participant.class))).thenReturn(mockResponse);

        // WHEN
        ParticipantResponse actualResponse = boardService.addParticipantToBoard(boardId, request, userEmail);

        // THEN
        verify(boardRepository, times(1)).save(any(Board.class));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getName()).isEqualTo("New Player");
        assertThat(testBoard.getParticipants()).hasSize(2); // Vérifier que le participant est ajouté au board
    }

    /**
     * Teste la suppression d'un participant d'un tableau.
     */
    @Test
    @DisplayName("Devrait supprimer un participant d'un tableau")
    void removeParticipantFromBoard_shouldRemoveParticipant() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        Long participantId = testParticipant.getId();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // WHEN
        boardService.removeParticipantFromBoard(boardId, participantId, userEmail);

        // THEN
        assertThat(testBoard.getParticipants()).isEmpty(); // Vérifier que le participant a été retiré de la liste du board
    }

    /**
     * Teste la mise à jour d'un participant.
     */
    @Test
    @DisplayName("Devrait mettre à jour un participant et retourner ParticipantResponse")
    void updateParticipantInBoard_shouldUpdateParticipant() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        Long participantId = testParticipant.getId();
        fr.gamegauge.gamegauge_api.dto.request.ParticipantUpdateRequest request = new fr.gamegauge.gamegauge_api.dto.request.ParticipantUpdateRequest();
        request.setName("Updated Player Name");

        Participant updatedParticipant = new Participant();
        updatedParticipant.setId(participantId);
        updatedParticipant.setName(request.getName());
        ParticipantResponse mockResponse = new ParticipantResponse(participantId, request.getName(), 0, Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));
        when(participantRepository.save(any(Participant.class))).thenReturn(updatedParticipant);
        when(participantMapper.toParticipantResponse(any(Participant.class))).thenReturn(mockResponse);

        // WHEN
        ParticipantResponse actualResponse = boardService.updateParticipantInBoard(boardId, participantId, request, userEmail);

        // THEN
        verify(participantRepository, times(1)).save(any(Participant.class));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getName()).isEqualTo(request.getName());
    }

    /**
     * Teste la définition (ajout ou mise à jour) d'un score pour un participant.
     */
    @Test
    @DisplayName("Devrait définir un score pour un participant (nouvelle entrée)")
    void setScoreForParticipant_shouldAddNewScore() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        Long participantId = testParticipant.getId();
        ScoreEntryAddRequest request = new ScoreEntryAddRequest();
        request.setRoundNumber(2);
        request.setScoreValue(75);

        ScoreEntry newScore = new ScoreEntry();
        newScore.setId(101L);
        newScore.setRoundNumber(request.getRoundNumber());
        newScore.setScoreValue(request.getScoreValue());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));
        when(scoreEntryRepository.save(any(ScoreEntry.class))).thenReturn(newScore);

        // WHEN
        boardService.setScoreForParticipant(boardId, participantId, request, userEmail);

        // THEN
        verify(scoreEntryRepository, times(1)).save(any(ScoreEntry.class));
        assertThat(testParticipant.getScoreEntries()).hasSize(2); // La liste devrait avoir 2 scores
    }

    /**
     * Teste la mise à jour d'un score existant pour le même tour.
     */
    @Test
    @DisplayName("Devrait mettre à jour un score existant pour le même tour")
    void setScoreForParticipant_shouldUpdateExistingScore() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        Long participantId = testParticipant.getId();
        ScoreEntryAddRequest request = new ScoreEntryAddRequest();
        request.setRoundNumber(1); // Même tour que le score initial
        request.setScoreValue(120); // Nouvelle valeur

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));
        when(scoreEntryRepository.save(any(ScoreEntry.class))).thenReturn(testScoreEntry); // Simuler la sauvegarde

        // WHEN
        boardService.setScoreForParticipant(boardId, participantId, request, userEmail);

        // THEN
        verify(scoreEntryRepository, times(1)).save(any(ScoreEntry.class));
        assertThat(testScoreEntry.getScoreValue()).isEqualTo(120); // La valeur du score existant doit être mise à jour
        assertThat(testParticipant.getScoreEntries()).hasSize(1); // Pas de nouveau score ajouté
    }

    /**
     * Teste la suppression d'une entrée de score.
     */
    @Test
    @DisplayName("Devrait supprimer une entrée de score d'un participant")
    void deleteScoreFromParticipant_shouldRemoveScoreEntry() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();
        Long participantId = testParticipant.getId();
        Long scoreId = testScoreEntry.getId();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // WHEN
        boardService.deleteScoreFromParticipant(boardId, participantId, scoreId, userEmail);

        // THEN
        assertThat(testParticipant.getScoreEntries()).isEmpty(); // Vérifier que le score est retiré de la liste du participant
    }

    /**
     * Teste la réinitialisation complète d'un tableau (suppression de tous les scores).
     */
    @Test
    @DisplayName("Devrait réinitialiser un tableau en supprimant tous les scores")
    void restartBoard_shouldDeleteAllScoresForBoard() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // WHEN
        boardService.restartBoard(boardId, userEmail);

        // THEN
        // Vérifier que la méthode spécifique du repository est appelée
        verify(scoreEntryRepository, times(1)).deleteAllByParticipantBoardId(boardId);
    }

    /**
     * Teste la duplication d'un tableau de scores.
     * Vérifie qu'un nouveau tableau est créé avec les mêmes règles et participants, mais sans scores.
     */
    @Test
    @DisplayName("Devrait dupliquer un tableau sans les scores")
    void duplicateBoard_shouldCreateNewBoardWithoutScores() {
        // GIVEN
        String userEmail = testUser.getEmail();
        Long boardId = testBoard.getId();

        Board duplicatedBoard = new Board();
        duplicatedBoard.setId(12L);
        duplicatedBoard.setName(testBoard.getName() + " (Copie)");
        duplicatedBoard.setOwner(testUser);
        duplicatedBoard.setTargetScore(testBoard.getTargetScore());
        duplicatedBoard.setScoreCondition(testBoard.getScoreCondition());
        duplicatedBoard.setNumberOfRounds(testBoard.getNumberOfRounds());

        // Pour le mapper
        BoardResponse mockResponse = new BoardResponse(duplicatedBoard.getId(), duplicatedBoard.getName(), duplicatedBoard.getTargetScore(), duplicatedBoard.getScoreCondition(), duplicatedBoard.getNumberOfRounds(), Instant.now(), Instant.now(), testUser.getUsername(), Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(duplicatedBoard);
        when(boardMapper.toBoardResponse(any(Board.class))).thenReturn(mockResponse);

        // WHEN
        BoardResponse actualResponse = boardService.duplicateBoard(boardId, userEmail);

        // THEN
        verify(boardRepository, times(1)).save(any(Board.class));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getName()).contains("(Copie)");
        assertThat(actualResponse.getParticipants()).isEmpty(); // La copie ne doit pas avoir de participants mappés
        // Important: Vérifier que l'entité sauvegardée n'a pas de scores
        ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
        verify(boardRepository).save(boardCaptor.capture());
        assertThat(boardCaptor.getValue().getParticipants()).hasSize(testBoard.getParticipants().size()); // Même nombre de participants
        assertThat(boardCaptor.getValue().getParticipants().get(0).getScoreEntries()).isEmpty(); // Mais leurs scores sont vides
    }
}