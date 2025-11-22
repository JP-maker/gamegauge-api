package fr.gamegauge.gamegauge_api.controller;

import fr.gamegauge.gamegauge_api.config.SecurityConfig;
import fr.gamegauge.gamegauge_api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "user@example.com")
    void userProfile_shouldReturnWelcomeMessage_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bienvenue sur votre profil, user@example.com !"));
    }

    @Test
    void userProfile_shouldReturn403_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isForbidden());
    }
}