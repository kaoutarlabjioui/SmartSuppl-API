package org.smartsupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern. slf4j.Slf4j;
import org.smartsupply.model.entity.User;
import org.smartsupply.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util. List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordMigrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Vérifie si un mot de passe est en SHA-256 (ancien format)
     */
    public boolean isSha256Password(String password) {
        // Les mots de passe SHA-256 encodés en Base64 font 44 caractères
        // Les mots de passe BCrypt commencent par $2a$ ou $2b$
        return password != null
                && password.length() == 44
                && ! password.startsWith("$2a$")
                && !password.startsWith("$2b$");
    }

    /**
     * Encode un mot de passe en SHA-256 (pour comparaison temporaire)
     */
    public String encodeSha256(String password) {
        try {
            MessageDigest md = MessageDigest. getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors de l'encodage du mot de passe", e);
        }
    }
}