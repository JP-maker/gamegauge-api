package fr.gamegauge.gamegauge_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gamegauge.gamegauge_api.config.SecurityConfig;
import fr.gamegauge.gamegauge_api.dto.response.BoardResponse;
import fr.gamegauge.gamegauge_api.service.BoardService;
import fr.gamegauge.gamegauge_api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser; // Pour simuler un user connecté
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getBoards_shouldReturn403_whenNotAuthenticated() throws Exception {
        // Test de sécurité : si on n'est pas authentifié, on doit avoir une erreur 403 (Forbidden)
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com") // Simule un utilisateur connecté avec ce nom
    void getUserBoards_shouldReturnBoardList_whenAuthenticated() throws Exception {
        // GIVEN
        BoardResponse boardResponse = new BoardResponse(1L, "Test Board", 100, null, null, null, null, null, null);
        List<BoardResponse> boardList = Collections.singletonList(boardResponse);

        // Simuler le service
        when(boardService.getBoardsForUser(anyString())).thenReturn(boardList);

        // WHEN & THEN
        mockMvc.perform(get("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // La liste JSON doit avoir 1 élément
                .andExpect(jsonPath("$[0].name", is("Test Board"))); // Le nom du 1er élément doit être "Test Board"
    }

    // Vous pouvez continuer avec des tests pour les autres endpoints (GET by ID, POST, PUT, DELETE...)
    // en suivant le même modèle avec @WithMockUser.
}