package org. smartsupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartsupply.exception.DuplicateResourceException;
import org.smartsupply.exception.ForbiddenException;
import org.smartsupply.exception.UnauthorizedException;
import org.smartsupply. mapper.UserMapper;
import org. smartsupply.dto.request.LoginRequestDto;
import org.smartsupply.dto.request.RegisterRequestDto;
import org.smartsupply.dto.response. AuthResponseDto;
import org. smartsupply.model.entity.RefreshToken;
import org.smartsupply.model.entity. User;
import org.smartsupply.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordMigrationService passwordMigrationService;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new DuplicateResourceException("Cet email est déjà utilisé");
        }

        User user = userMapper.toEntity(registerRequestDto);

        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshTokenJwt = jwtService.generateRefreshToken(savedUser);
        refreshTokenService.createRefreshToken(savedUser, refreshTokenJwt);

        return AuthResponseDto. builder()
                .message("Inscription réussie")
                .user(userMapper.toResponseDto(savedUser))
                .accessToken(accessToken)
                .refreshToken(refreshTokenJwt)
                .build();
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Votre compte est désactivé");
        }


        boolean passwordMatches = false;
        boolean needsMigration = false;


        if (passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            passwordMatches = true;
        }
        // Si échec, essayer avec SHA-256 (ancien format)
        else if (passwordMigrationService.isSha256Password(user.getPassword())) {
            String sha256Hash = passwordMigrationService.encodeSha256(loginRequestDto.getPassword());
            if (sha256Hash.equals(user.getPassword())) {
                passwordMatches = true;
                needsMigration = true;
                log.info(" Utilisateur {} utilise encore SHA-256, migration nécessaire", user.getEmail());
            }
        }

        if (!passwordMatches) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        // Migration automatique du mot de passe lors de la connexion
        if (needsMigration) {
            user.setPassword(passwordEncoder.encode(loginRequestDto.getPassword()));
            userRepository.save(user);
            log.info(" Mot de passe migré vers BCrypt pour l'utilisateur {}", user. getEmail());
        }

        String accessToken = jwtService. generateAccessToken(user);
        String refreshTokenJwt = jwtService.generateRefreshToken(user);
        refreshTokenService. createRefreshToken(user, refreshTokenJwt);

        return AuthResponseDto.builder()
                .message("Connexion réussie")
                .user(userMapper.toResponseDto(user))
                .accessToken(accessToken)
                .refreshToken(refreshTokenJwt)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Transactional
    public AuthResponseDto refreshAccessToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalide"));

        if (!refreshTokenService.isValid(storedToken)) {
            throw new UnauthorizedException("Refresh token expiré ou révoqué");
        }
        User user = storedToken.getUser();

        if (!user.getIsActive()) {
            throw new ForbiddenException("Votre compte est désactivé");
        }

        // Générer un nouveau access token
        String newAccessToken = jwtService.generateAccessToken(user);

        // Rotation du refresh token
        String newRefreshTokenJwt = jwtService. generateRefreshToken(user);
        refreshTokenService.createRefreshToken(user, newRefreshTokenJwt);

        return AuthResponseDto.builder()
                .message("Token renouvelé")
                .user(userMapper. toResponseDto(user))
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenJwt)
                .build();
    }
}