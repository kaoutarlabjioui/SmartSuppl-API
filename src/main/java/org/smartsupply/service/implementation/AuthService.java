package org.smartsupply.service.implementation;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.smartsupply.dto.request.RegisterRequestDto;
import org.smartsupply.dto.response.AuthResponseDto;
import org.smartsupply.exception.DuplicateResourceException;
import org.smartsupply.mapper.UserMapper;
import org.smartsupply.model.entity.User;
import org.smartsupply.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new DuplicateResourceException("Cet email est déjà utilisé");
        }

        // 1. Créer l'utilisateur dans Keycloak
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(registerRequestDto.getEmail());
        keycloakUser.setEmail(registerRequestDto.getEmail());
        keycloakUser.setFirstName(registerRequestDto.getFirstName());
        keycloakUser.setLastName(registerRequestDto.getLastName());
        keycloakUser.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registerRequestDto.getPassword());
        credential.setTemporary(false);
        keycloakUser.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(keycloakUser);

        if (response.getStatus() != 201) {
            String errorMessage = "Erreur lors de la création de l'utilisateur dans Keycloak: "
                    + response.getStatusInfo();
            try {
                errorMessage += " " + response.readEntity(String.class);
            } catch (Exception e) {
                // ignore
            }
            log.error(errorMessage);
            throw new RuntimeException("Impossible de créer l'utilisateur dans Keycloak");
        }

        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        log.info("Utilisateur créé dans Keycloak avec l'ID: {}", userId);

        // 2. Créer l'utilisateur dans la base locale
        User user = userMapper.toEntity(registerRequestDto);
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword())); // On garde le hash local si besoin

        User savedUser = userRepository.save(user);

        return AuthResponseDto.builder()
                .message("Inscription réussie. Utilisateur synchronisé avec Keycloak.")
                .user(userMapper.toResponseDto(savedUser))
                .build();
    }

    public AuthResponseDto login(org.smartsupply.dto.request.LoginRequestDto loginRequestDto) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
        map.add("client_id", "smartsupply-api");
        map.add("username", loginRequestDto.getEmail());
        map.add("password", loginRequestDto.getPassword());
        map.add("grant_type", "password");

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new org.springframework.http.HttpEntity<>(
                map, headers);

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> response = restTemplate.postForObject(
                    "http://localhost:8180/realms/smartsupply/protocol/openid-connect/token",
                    request,
                    java.util.Map.class);

            String accessToken = (String) response.get("access_token");
            String refreshToken = (String) response.get("refresh_token");

            User user = userRepository.findByEmail(loginRequestDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé localement"));

            return AuthResponseDto.builder()
                    .message("Connexion réussie")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userMapper.toResponseDto(user))
                    .build();
        } catch (Exception e) {
            log.error("Erreur login Keycloak", e);
            throw new RuntimeException("Échec de l'authentification : " + e.getMessage());
        }
    }
}