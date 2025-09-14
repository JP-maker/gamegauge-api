package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.request.LoginRequest;
import fr.gamegauge.gamegauge_api.dto.request.RegisterRequest;
import fr.gamegauge.gamegauge_api.dto.response.JwtAuthenticationResponse;
import fr.gamegauge.gamegauge_api.model.User;
import fr.gamegauge.gamegauge_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService; // Ajout
    @Mock
    private AuthenticationManager authenticationManager; // Ajout

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword"); // Important pour le test de login
    }

    @Test
    void registerUser_shouldSaveUser_whenUsernameAndEmailAreAvailable() {
        // --- GIVEN ---
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        // Simuler que le username et l'email ne sont PAS trouvés
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Simuler le hachage du mot de passe
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        // --- WHEN ---
        authService.registerUser(request);

        // --- THEN ---
        // 1. Capturer l'objet User qui a été passé à la méthode save()
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        // 2. Récupérer l'utilisateur capturé
        User savedUser = userArgumentCaptor.getValue();

        // 3. Vérifier que les informations de l'utilisateur sauvegardé sont correctes
        assertThat(savedUser.getUsername()).isEqualTo("newUser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword"); // Très important : vérifier que le mdp est bien haché
        assertThat(savedUser.isEmailVerified()).isFalse();
    }

    @Test
    void registerUser_shouldThrowIllegalStateException_whenUsernameIsTaken() {
        // --- GIVEN ---
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        // Simuler que le nom d'utilisateur EST trouvé
        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        // --- WHEN & THEN ---
        // Vérifier que l'exception est levée
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.registerUser(request);
        });

        // Vérifier le message de l'exception
        assertThat(exception.getMessage()).isEqualTo("Erreur : Le nom d'utilisateur est déjà pris !");

        // Vérifier que la sauvegarde n'a jamais eu lieu
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowIllegalStateException_whenEmailIsTaken() {
        // --- GIVEN ---
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        // Simuler que le username est disponible mais que l'email EST trouvé
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        // --- WHEN & THEN ---
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.registerUser(request);
        });

        assertThat(exception.getMessage()).isEqualTo("Erreur : L'email est déjà utilisé !");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnJwtResponse_whenCredentialsAreValid() {
        // --- GIVEN ---
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // 1. Simuler que l'AuthenticationManager ne lève PAS d'exception
        // C'est implicite, on n'a pas besoin d'un when().thenReturn() pour une méthode void.
        // Si on ne configure rien, il ne fera rien (ce qui équivaut à un succès).

        // 2. Simuler la récupération de l'utilisateur après l'authentification réussie
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // 3. Simuler la génération du token
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("fake.jwt.token");

        // --- WHEN ---
        JwtAuthenticationResponse response = authService.login(request);

        // --- THEN ---
        // 1. Vérifier que l'AuthenticationManager a bien été appelé avec les bons identifiants
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        UsernamePasswordAuthenticationToken authenticationToken = captor.getValue();

        assertThat(authenticationToken.getName()).isEqualTo("test@example.com");
        assertThat(authenticationToken.getCredentials()).isEqualTo("password123");

        // 2. Vérifier que la réponse contient le bon token
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // --- GIVEN ---
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        // 1. Simuler que l'AuthenticationManager lève une exception quand il est appelé
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Identifiants invalides"));

        // --- WHEN & THEN ---
        // Vérifier que l'exception est bien levée (et non interceptée par notre service)
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        // Sécurité : vérifier que le service ne continue PAS pour générer un token
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }
}