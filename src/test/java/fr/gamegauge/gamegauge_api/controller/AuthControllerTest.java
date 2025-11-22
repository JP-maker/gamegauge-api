package fr.gamegauge.gamegauge_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gamegauge.gamegauge_api.config.SecurityConfig;
import fr.gamegauge.gamegauge_api.dto.request.LoginRequest;
import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.dto.response.JwtAuthenticationResponse;
import fr.gamegauge.gamegauge_api.service.AuthService;
import fr.gamegauge.gamegauge_api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class) // On ne teste que ce contrôleur
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permet de simuler des requêtes HTTP

    @Autowired
    private ObjectMapper objectMapper; // Pour convertir les objets Java en JSON

    @MockitoBean // On crée un mock du service, car on ne veut pas tester sa logique ici
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService; // Nécessaire pour JwtAuthFilter

    @MockitoBean
    private UserDetailsService userDetailsService;

    private AuthController authController;

    @BeforeEach
        // Méthode d'initialisation avant chaque test
    void setUp() {
        // On instancie manuellement le contrôleur en lui passant notre mock.
        authController = new AuthController(authService);
    }

    @Test
    void registerUser_shouldCallAuthService() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");

        // WHEN
        // On appelle directement la méthode Java du contrôleur
        ResponseEntity<?> response = authController.registerUser(request);

        // THEN
        // 1. On vérifie que le service a bien été appelé
        verify(authService).registerUser(any(RegisterRequest.class));

        // 2. On vérifie que le contrôleur retourne bien la bonne réponse HTTP
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        // On pourrait aussi vérifier le corps de la réponse
    }

    @Test
    void registerUser_shouldReturnSuccessMessage() throws Exception {
        // GIVEN
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRecaptchaToken("valid-token");

        // Simuler le service : quand registerUser est appelé, il ne fait rien (void)
        doNothing().when(authService).registerUser(any(RegisterRequest.class));

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                        // AJOUTER CETTE LIGNE pour désactiver le CSRF pour cette requête de test
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilisateur enregistré avec succès !"));
    }

    @Test
    void loginUser_shouldReturnJwtToken() throws Exception {
        // GIVEN
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setRecaptchaToken("valid-token");

        JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse("fake.jwt.token");

        // Simuler le service : quand login est appelé, il retourne notre fausse réponse JWT
        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        // AJOUTER AUSSI ICI
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"));
    }
}