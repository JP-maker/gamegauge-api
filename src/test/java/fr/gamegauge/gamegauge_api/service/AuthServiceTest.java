package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.LoginRequest;
import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.dto.response.JwtAuthenticationResponse;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Classe de tests unitaires pour le service {@link AuthService}.
 * Utilise Mockito pour simuler les dépendances et isoler la logique du service.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RecaptchaService recaptchaService; // Mock du service reCAPTCHA

    @InjectMocks
    private AuthService authService;

    private User testUser; // Utilisateur de test réutilisable

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
        testUser.setEmailVerified(true); // Supposons vérifié pour certains tests
        testUser.setVerificationToken(null);
    }

    /**
     * Teste le scénario d'une inscription réussie.
     * Vérifie que l'utilisateur est sauvegardé avec un mot de passe haché et des valeurs par défaut.
     */
    @Test
    @DisplayName("Devrait sauvegarder l'utilisateur quand le nom d'utilisateur et l'email sont disponibles")
    void registerUser_shouldSaveUser_whenUsernameAndEmailAreAvailable() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("valid-recaptcha-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(true); // reCAPTCHA valide
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser); // Simuler la sauvegarde

        // WHEN
        authService.registerUser(request);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture()); // Vérifier que save a été appelé

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("newUser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
        assertThat(savedUser.isEmailVerified()).isFalse(); // Email non vérifié par défaut
    }

    /**
     * Teste l'échec de l'inscription si le nom d'utilisateur est déjà pris.
     * Vérifie que l'exception {@link IllegalStateException} est levée et qu'aucune sauvegarde n'a lieu.
     */
    @Test
    @DisplayName("Devrait lever IllegalStateException quand le nom d'utilisateur est déjà pris")
    void registerUser_shouldThrowIllegalStateException_whenUsernameIsTaken() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("valid-recaptcha-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(true);
        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User())); // Simuler un utilisateur existant

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(User.class)); // Vérifier qu'aucune sauvegarde n'a été faite
    }

    /**
     * Teste l'échec de l'inscription si l'email est déjà utilisé.
     * Vérifie que l'exception {@link IllegalStateException} est levée et qu'aucune sauvegarde n'a lieu.
     */
    @Test
    @DisplayName("Devrait lever IllegalStateException quand l'email est déjà utilisé")
    void registerUser_shouldThrowIllegalStateException_whenEmailIsTaken() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("valid-recaptcha-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(true);
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Teste l'échec de l'inscription si le token reCAPTCHA est invalide.
     */
    @Test
    @DisplayName("Devrait lever IllegalStateException quand le token reCAPTCHA est invalide lors de l'inscription")
    void registerUser_shouldThrowIllegalStateException_whenRecaptchaTokenIsInvalid() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("invalid-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(false); // reCAPTCHA invalide

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Teste le scénario d'une connexion réussie.
     * Vérifie que {@link AuthenticationManager} et {@link JwtService} sont appelés et qu'un token est retourné.
     */
    @Test
    @DisplayName("Devrait retourner JwtAuthenticationResponse quand les identifiants sont valides")
    void login_shouldReturnJwtResponse_whenCredentialsAreValid() {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("valid-recaptcha-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(true);
        // Simuler le succès de l'authentification (ne lève pas d'exception)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("fake.jwt.token");

        // WHEN
        JwtAuthenticationResponse response = authService.login(request);

        // THEN
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(UserDetails.class));
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
    }

    /**
     * Teste l'échec de la connexion si les identifiants sont invalides.
     * Vérifie que {@link BadCredentialsException} est levée et qu'aucun token n'est généré.
     */
    @Test
    @DisplayName("Devrait lever BadCredentialsException quand les identifiants sont invalides")
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");
        request.setRecaptchaToken("valid-recaptcha-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(true);
        // Simuler l'échec de l'authentification
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Identifiants invalides"));

        // WHEN & THEN
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(UserDetails.class)); // Pas de token généré
    }

    /**
     * Teste l'échec de la connexion si le token reCAPTCHA est invalide.
     */
    @Test
    @DisplayName("Devrait lever BadCredentialsException quand le token reCAPTCHA est invalide lors de la connexion")
    void login_shouldThrowBadCredentialsException_whenRecaptchaTokenIsInvalid() {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("invalid-token");

        // Comportement des mocks
        when(recaptchaService.validateToken(anyString())).thenReturn(false); // reCAPTCHA invalide

        // WHEN & THEN
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}