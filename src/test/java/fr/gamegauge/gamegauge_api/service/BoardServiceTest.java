package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.BoardCreateRequest;
import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.exception.ResourceNotFoundException;
import fr.gamegauge.gamegauge_api.mapper.BoardMapper;
import fr.gamegauge.gamegauge_api.model.Board;
import fr.gamegauge.gamegauge_api.model.Participant;
import fr.gamegauge.gamegauge_api.model.ScoreEntry;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.BoardRepository;
import fr.gamegauge.gamegauge_api.repository.ParticipantRepository;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe BoardService.
 */
@ExtendWith(MockitoExtension.class) // Active l'utilisation de Mockito dans cette classe de test
class BoardServiceTest {

    // On crée des "mocks" (simulations) pour toutes les dépendances du BoardService
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardMapper boardMapper;
    // On ignore les autres dépendances pour l'instant car elles ne sont pas utilisées dans la méthode testée

    // On demande à Mockito d'injecter les mocks ci-dessus dans cette instance du BoardService
    @InjectMocks
    private BoardService boardService;

    // Objets de test que nous réutiliserons
    private User testUser;
    private Board testBoard;

    @BeforeEach // Cette méthode sera exécutée avant chaque test
    void setUp() {
        // Initialiser nos objets de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        testBoard = new Board();
        testBoard.setId(10L);
        testBoard.setName("Test Board");
        testBoard.setOwner(testUser);
    }

    @Test
    void getBoardById_shouldReturnBoard_whenFoundAndOwned() {
        // --- GIVEN (Étant donné) ---
        // On définit le comportement de nos mocks
        // 1. Quand userRepository.findByEmail est appelé avec "test@example.com", il doit retourner notre utilisateur de test
        String userEmail = "test@example.com";
        Long boardId = 10L;

        // 1. Préparer le mock pour quand le service appellera userRepository
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // 2. Préparer le mock pour quand le service appellera boardRepository
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // 3. Préparer le mock pour quand le service appellera le mapper
        // On crée une fausse réponse que le mapper est censé retourner
        BoardResponse mockResponse = new BoardResponse(boardId, "Test Board", null, null,null, null, null, null, null);
        when(boardMapper.toBoardResponse(any(Board.class))).thenReturn(mockResponse);

        // --- WHEN ---
        // On appelle la VRAIE méthode du service que l'on veut tester
        BoardResponse actualResponse = boardService.getBoardById(boardId, userEmail);

        // --- THEN ---
        // On vérifie le résultat retourné par le service
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(boardId);
        assertThat(actualResponse.getName()).isEqualTo("Test Board");
    }

    @Test
    void getBoardById_shouldReturnBoardResponse_whenFoundAndOwned() {
        // --- GIVEN ---
        String userEmail = "test@example.com";
        Long boardId = 10L;

        // Définir le comportement des mocks
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // Simuler le mapper
        BoardResponse mockResponse = new BoardResponse(boardId, "Test Board", null,null, null, null, null, null, null);
        when(boardMapper.toBoardResponse(any(Board.class))).thenReturn(mockResponse);

        // --- WHEN ---
        BoardResponse actualResponse = boardService.getBoardById(boardId, userEmail);

        // --- THEN ---
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(boardId);
        assertThat(actualResponse.getName()).isEqualTo("Test Board");
    }

    @Test
    void getBoardById_shouldThrowResourceNotFoundException_whenBoardDoesNotExist() {
        // --- GIVEN ---
        String userEmail = "test@example.com";
        Long nonExistentBoardId = 99L;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        // Simuler le cas où le board n'est pas trouvé pour cet utilisateur
        when(boardRepository.findByIdAndOwner(nonExistentBoardId, testUser)).thenReturn(Optional.empty());

        // --- WHEN & THEN ---
        // On vérifie qu'appeler la méthode lève bien l'exception attendue
        assertThrows(ResourceNotFoundException.class, () -> {
            boardService.getBoardById(nonExistentBoardId, userEmail);
        });
    }

    @Test
    void createBoard_shouldSaveAndReturnBoardResponse() {
        // --- GIVEN ---
        String userEmail = "test@example.com";
        BoardCreateRequest request = new BoardCreateRequest();
        request.setName("Nouveau Tournoi");
        request.setTargetScore(500);

        // 1. Préparer l'utilisateur
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // 2. Simuler la sauvegarde du repository (il doit retourner l'objet 'sauvegardé')
        // On simule l'entité qui est retournée après la sauvegarde (avec l'ID généré)
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        // 3. Simuler le mapping vers le DTO de réponse
        BoardResponse mockResponse = new BoardResponse(
                testBoard.getId(), "Nouveau Tournoi", 500, null, null,null, null, "testuser", null);
        when(boardMapper.toBoardResponse(any(Board.class))).thenReturn(mockResponse);

        // --- WHEN ---
        BoardResponse actualResponse = boardService.createBoard(request, userEmail);

        // --- THEN ---
        // Vérifier que le repository a bien été appelé pour sauvegarder
        verify(boardRepository).save(any(Board.class));

        // Vérifier la réponse
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getName()).isEqualTo("Nouveau Tournoi");
        assertThat(actualResponse.getTargetScore()).isEqualTo(500);
        assertThat(actualResponse.getOwnerUsername()).isEqualTo("testuser");
    }

    @Test
    void createBoard_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // --- GIVEN ---
        String nonExistentUserEmail = "inconnu@example.com";
        BoardCreateRequest request = new BoardCreateRequest();
        request.setName("Tournoi Inconnu");

        // Simuler le cas où l'utilisateur n'est pas trouvé
        when(userRepository.findByEmail(nonExistentUserEmail)).thenReturn(Optional.empty());

        // --- WHEN & THEN ---
        // Vérifier que l'exception correcte est levée
        assertThrows(UsernameNotFoundException.class, () -> {
            boardService.createBoard(request, nonExistentUserEmail);
        });

        // Vérifier que la méthode de sauvegarde n'a JAMAIS été appelée (sécurité)
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    void deleteScoreFromParticipant_shouldDeleteScoreSuccessfully() {
        // --- GIVEN ---
        String userEmail = "test@example.com";
        Long boardId = 10L;
        Long participantId = 1L;
        Long scoreId = 100L;
        ParticipantRepository participantRepository = mock(ParticipantRepository.class);

        // 1. Préparer l'utilisateur et les mocks de repository
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(boardRepository.findByIdAndOwner(boardId, testUser)).thenReturn(Optional.of(testBoard));

        // 2. Créer l'entrée de score et le participant simulés
        ScoreEntry scoreToDelete = new ScoreEntry();
        scoreToDelete.setId(scoreId);
        scoreToDelete.setScoreValue(100);

        Participant participant = new Participant();
        participant.setId(participantId);
        // Utiliser une VRAIE liste pour simuler l'action .remove()
        List<ScoreEntry> scores = new ArrayList<>();
        scores.add(scoreToDelete);
        participant.setScoreEntries(scores);

        // 3. Simuler que le Board contient ce participant
        // Ici, on simule que la liste de participants du board contient notre participant
        testBoard.setParticipants(List.of(participant));

        // --- WHEN ---
        // On exécute la suppression
        boardService.deleteScoreFromParticipant(boardId, participantId, scoreId, userEmail);

        // --- THEN ---
        // L'assertion la plus importante est que l'élément est retiré de la liste,
        // ce qui déclenchera orphanRemoval=true.
        // On vérifie que la liste de scores du participant NE contient PLUS le score supprimé.
        assertThat(participant.getScoreEntries()).isEmpty();

        // Sécurité: vérifier que le scoreRepository.delete() n'a JAMAIS été appelé
        // car nous utilisons orphanRemoval=true (c'est Hibernate qui s'en charge).
        verify(participantRepository, never()).delete(any(Participant.class)); // Cette vérification est pour les participants
        // Nous pouvons ignorer ScoreEntryRepository.delete() car nous ne l'avons pas injecté ici si nous n'en avions plus besoin,
        // mais nous devons au moins vérifier l'état du modèle.

        // Finalement, on peut vérifier que la sauvegarde du participant n'a pas été appelée explicitement (car @Transactional gère ça)
        // Mais la vérification la plus robuste reste la liste vide.
    }
}