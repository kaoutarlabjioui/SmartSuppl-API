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


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity. ok(userMapper.toResponseDto(user));
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService. getAllUsers();
        return ResponseEntity.ok(users);
    }



    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getActiveUsers() {
        List<UserResponseDto> users = userService. getActiveUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getInactiveUsers() {
        List<UserResponseDto> users = userService.getInactiveUsers();
        return ResponseEntity. ok(users);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getUsersByRole(@PathVariable Role role) {
        List<UserResponseDto> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }


    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> activateUser(@PathVariable Long id) {
        UserResponseDto user = userService.activateUser(id);
        return ResponseEntity. ok(user);
    }


    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable Long id) {
        UserResponseDto user = userService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String keyword) {
        List<UserResponseDto> users = userService. searchUsers(keyword);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsDto> getUserStats() {
        UserStatsDto stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<UserResponseDto> warehouseAccess(@AuthenticationPrincipal User user) {
        return ResponseEntity. ok(userMapper.toResponseDto(user));
    }


    @GetMapping("/client-area")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<UserResponseDto> clientArea(@AuthenticationPrincipal User user) {
        return ResponseEntity. ok(userMapper.toResponseDto(user));
    }
}