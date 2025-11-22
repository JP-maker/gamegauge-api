package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.recaptcha.RecaptchaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Classe de tests unitaires pour le service {@link RecaptchaService}.
 * Teste la validation du token reCAPTCHA en simulant les appels à l'API de Google.
 */
@ExtendWith(MockitoExtension.class)
class RecaptchaServiceTest {

    @Mock // Mock de RestTemplate pour simuler les appels HTTP externes
    private RestTemplate restTemplate;

    @MockitoBean // Injecte le mock RestTemplate dans le RecaptchaService
    private RecaptchaService recaptchaService;

    private String testSecretKey = "test-secret-key";
    private String recaptchaVerifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * Configure le service avant chaque test.
     * Utilise ReflectionTestUtils pour injecter la valeur @Value.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recaptchaService, "recaptchaSecretKey", testSecretKey);
        // Note: L'URL est hardcodée dans le service, pas besoin de l'injecter ici.
    }

    /**
     * Teste la validation d'un token reCAPTCHA valide avec un bon score.
     */
    @Test
    @DisplayName("Devrait retourner true pour un token reCAPTCHA valide et un score élevé")
    void validateToken_shouldReturnTrue_whenTokenIsValidAndScoreIsHigh() {
        // GIVEN
        String token = "valid-token";
        RecaptchaResponse mockResponse = new RecaptchaResponse();
        mockResponse.setSuccess(true);
        mockResponse.setScore(0.9); // Score élevé

        // Simuler la réponse de l'API de Google
        when(restTemplate.postForObject(eq(recaptchaVerifyUrl), any(), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // WHEN
        boolean isValid = recaptchaService.validateToken(token);

        // THEN
        assertThat(isValid).isTrue();
    }

    /**
     * Teste la validation d'un token reCAPTCHA invalide (Google API retourne échec).
     */
    @Test
    @DisplayName("Devrait retourner false pour un token reCAPTCHA invalide")
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        // GIVEN
        String token = "invalid-token";
        RecaptchaResponse mockResponse = new RecaptchaResponse();
        mockResponse.setSuccess(false); // Google API indique un échec

        // Simuler la réponse de l'API de Google
        when(restTemplate.postForObject(eq(recaptchaVerifyUrl), any(), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // WHEN
        boolean isValid = recaptchaService.validateToken(token);

        // THEN
        assertThat(isValid).isFalse();
    }

    /**
     * Teste la validation d'un token reCAPTCHA valide mais avec un score trop bas.
     */
    @Test
    @DisplayName("Devrait retourner false quand le score reCAPTCHA est trop bas")
    void validateToken_shouldReturnFalse_whenScoreIsTooLow() {
        // GIVEN
        String token = "low-score-token";
        RecaptchaResponse mockResponse = new RecaptchaResponse();
        mockResponse.setSuccess(true);
        mockResponse.setScore(0.3); // Score bas (inférieur à 0.5)

        // Simuler la réponse de l'API de Google
        when(restTemplate.postForObject(eq(recaptchaVerifyUrl), any(), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // WHEN
        boolean isValid = recaptchaService.validateToken(token);

        // THEN
        assertThat(isValid).isFalse();
    }
}