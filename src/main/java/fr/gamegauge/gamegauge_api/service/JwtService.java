package fr.gamegauge.gamegauge_api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service utilitaire pour la gestion des JSON Web Tokens (JWT).
 * Gère la génération, la validation et l'extraction d'informations des tokens.
 */
@Service
public class JwtService {

    // La clé secrète pour signer les tokens. Elle sera injectée depuis application.properties.
    // IMPORTANT: Cette clé doit être longue, complexe et gardée secrète en production.
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Extrait le nom d'utilisateur (email) du token JWT.
     *
     * @param token Le JWT.
     * @return Le nom d'utilisateur.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Génère un token JWT pour un utilisateur.
     *
     * @param userDetails Les détails de l'utilisateur à inclure dans le token.
     * @return Le token JWT généré.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Le token expire dans 24 heures
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valide si un token est correct et n'a pas expiré.
     *
     * @param token Le token JWT à valider.
     * @param userDetails Les détails de l'utilisateur pour la comparaison.
     * @return true si le token est valide, false sinon.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}