package fr.gamegauge.gamegauge_api.dto.recaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecaptchaResponse {
    private boolean success;
    private double score;
    private String action;
    @JsonProperty("challenge_ts")
    private String challengeTs;
    private String hostname;
}