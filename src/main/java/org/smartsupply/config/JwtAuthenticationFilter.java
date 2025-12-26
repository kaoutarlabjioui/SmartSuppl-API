package org.smartsupply.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org.smartsupply.model.entity.User;
import org.smartsupply.repository.UserRepository;
import org. smartsupply.service.implementation.JwtService;
import org.springframework.lang.NonNull;
import org.springframework. security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println(" JwtAuthenticationFilter EXECUTÉ pour: " + request.getRequestURI());
        log.info(" JwtAuthenticationFilter - URI: {}", request. getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        log.info(" ÉTAPE 1 - Authorization Header:  {}", authHeader != null ? "Present" : "Missing");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" ÉTAPE 2 - Pas de token Bearer, continuation sans authentification");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            log.info(" ÉTAPE 3 - Début extraction du token");
            final String jwt = authHeader.substring(7);
            log.info(" ÉTAPE 4 - Token extrait (longueur: {}), premiers caractères: {}.. .",
                    jwt.length(),
                    jwt.length() > 30 ? jwt.substring(0, 30) : jwt);

            log.info(" ÉTAPE 5 - Extraction de l'email");
            final String userEmail = jwtService.extractEmail(jwt);
            log.info(" ÉTAPE 6 - Email extrait: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info(" ÉTAPE 7 - Email présent, vérification du token");

                boolean isValid = jwtService.isTokenValid(jwt);
                log.info(" ÉTAPE 8 - Token valide: {}", isValid);

                boolean isExpired = jwtService.isTokenExpired(jwt);
                log.info(" ÉTAPE 9 - Token expiré: {}", isExpired);

                if (isValid && ! isExpired) {
                    log.info(" ÉTAPE 10 - Token OK, recherche de l'utilisateur");

                    User user = userRepository. findByEmail(userEmail).orElse(null);

                    if (user != null) {
                        log.info(" ÉTAPE 11 - Utilisateur trouvé:  {} - Role: {} - Actif: {}",
                                user.getEmail(), user.getRole(), user.getIsActive());
                    } else {
                        log. warn("ÉTAPE 11 - Utilisateur NON trouvé pour l'email: {}", userEmail);
                    }

                    if (user != null && user.getIsActive()) {
                        log.info(" ÉTAPE 12 - Utilisateur actif, création de l'autorité");

                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                        log.info(" ÉTAPE 13 - Autorité créée: {}", authority. getAuthority());

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(authority)
                        );
                        log.info(" ÉTAPE 14 - Token d'authentification créé");

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        log.info(" ÉTAPE 15 - Détails ajoutés");

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info(" ÉTAPE 16 - AUTHENTIFICATION STOCKÉE DANS LE SECURITY CONTEXT !");
                        log.info(" Utilisateur authentifié: {} avec rôle: {}", userEmail, authority.getAuthority());
                    } else if (user != null && !user.getIsActive()) {
                        log.warn(" ÉTAPE 12 - Utilisateur {} est INACTIF", userEmail);
                    }
                } else {
                    log. warn(" ÉTAPE 10 - Token invalide (valide: {}, expiré: {})", isValid, isExpired);
                }
            } else if (userEmail != null) {
                log.info(" ÉTAPE 7 - Authentification déjà présente dans le contexte");
            } else {
                log.warn(" ÉTAPE 6 - Email NULL extrait du token");
            }
        } catch (Exception e) {
            log.error(" ERREUR dans le filtre JWT:  {}", e.getMessage(), e);
        }

        log.info(" ÉTAPE FINALE - Appel de filterChain.doFilter");
        filterChain.doFilter(request, response);
        log.info(" FIN du JwtAuthenticationFilter");
    }
}