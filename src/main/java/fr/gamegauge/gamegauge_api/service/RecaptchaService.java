package fr.gamegauge.gamegauge_api.service;

import fr.gamegauge.gamegauge_api.dto.recaptcha.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {
    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final double RECAPTCHA_SCORE_THRESHOLD = 0.5; // Seuil de score

    @Value("${recaptcha.secret-key}")
    private String recaptchaSecretKey;

    public boolean validateToken(String recaptchaToken) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecretKey);
        requestMap.add("response", recaptchaToken);

        RecaptchaResponse response = restTemplate.postForObject(GOOGLE_RECAPTCHA_VERIFY_URL, requestMap, RecaptchaResponse.class);

        if (response == null || !response.isSuccess()) {
            return false;
        }

        // On vérifie que le score est supérieur à notre seuil
        return response.getScore() >= RECAPTCHA_SCORE_THRESHOLD;
    }
}