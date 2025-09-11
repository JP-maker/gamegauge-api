package fr.gamegauge.gamegauge_api.config;

import fr.gamegauge.gamegauge_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre de sécurité qui s'exécute une fois par requête pour valider le token JWT.
 * Ce filtre intercepte les requêtes entrantes, vérifie la présence et la validité d'un token JWT
 * dans l'en-tête "Authorization", et configure le contexte de sécurité de Spring en conséquence.
 */
@Component
@RequiredArgsConstructor // Annotation Lombok qui génère un constructeur avec les champs final.
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LogManager.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si l'en-tête est absent ou ne commence pas par "Bearer ", on passe au filtre suivant.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait le token de l'en-tête (en enlevant "Bearer ").
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            logger.warn("Erreur lors de l'extraction du nom d'utilisateur du JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Si on a un email et que l'utilisateur n'est pas déjà authentifié dans le contexte de sécurité.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Si le token est valide pour cet utilisateur.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                logger.debug("Token JWT valide pour l'utilisateur: {}", userEmail);
                // On crée un token d'authentification pour Spring Security.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // On n'a pas besoin des credentials ici.
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // On met à jour le contexte de sécurité. L'utilisateur est maintenant considéré comme authentifié.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                logger.warn("Validation du token JWT échouée pour l'utilisateur: {}", userEmail);
            }
        }

        // On passe la requête au filtre suivant dans la chaîne.
        filterChain.doFilter(request, response);
    }
}