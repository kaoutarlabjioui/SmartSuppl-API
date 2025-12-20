package org.smartsupply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org. smartsupply.dto.request.UserUpdateDto;
import org.smartsupply. dto.response.UserStatsDto;
import org.smartsupply.mapper.UserMapper;
import org.smartsupply.dto.response.UserResponseDto;
import org.smartsupply.model.entity.User;
import org.smartsupply.model.enums.Role;
import org.smartsupply.service.UserService;
import org. springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    /**
     * Récupérer l'utilisateur connecté
     * Accessible à tous les utilisateurs authentifiés
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity. ok(userMapper.toResponseDto(user));
    }

    /**
     * Lister tous les utilisateurs
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService. getAllUsers();
        return ResponseEntity.ok(users);
    }


    /**
     * Lister les utilisateurs actifs
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getActiveUsers() {
        List<UserResponseDto> users = userService. getActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Lister les utilisateurs inactifs
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getInactiveUsers() {
        List<UserResponseDto> users = userService.getInactiveUsers();
        return ResponseEntity. ok(users);
    }

    /**
     * Récupérer un utilisateur par ID
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Récupérer les utilisateurs par rôle
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getUsersByRole(@PathVariable Role role) {
        List<UserResponseDto> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Mettre à jour un utilisateur
     * Accessible uniquement aux ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Activer un utilisateur
     * Accessible uniquement aux ADMIN
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> activateUser(@PathVariable Long id) {
        UserResponseDto user = userService.activateUser(id);
        return ResponseEntity. ok(user);
    }

    /**
     * Désactiver un utilisateur
     * Accessible uniquement aux ADMIN
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable Long id) {
        UserResponseDto user = userService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Supprimer un utilisateur
     * Accessible uniquement aux ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechercher des utilisateurs
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String keyword) {
        List<UserResponseDto> users = userService. searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    /**
     * Statistiques des utilisateurs
     * Accessible uniquement aux ADMIN
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsDto> getUserStats() {
        UserStatsDto stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Accès warehouse
     * Accessible aux ADMIN et WAREHOUSE_MANAGER
     */
    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<UserResponseDto> warehouseAccess(@AuthenticationPrincipal User user) {
        return ResponseEntity. ok(userMapper.toResponseDto(user));
    }

    /**
     * Espace client
     * Accessible uniquement aux CLIENT
     */
    @GetMapping("/client-area")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<UserResponseDto> clientArea(@AuthenticationPrincipal User user) {
        return ResponseEntity. ok(userMapper.toResponseDto(user));
    }
}