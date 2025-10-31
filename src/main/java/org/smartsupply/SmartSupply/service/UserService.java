package org.smartsupply.SmartSupply.service;

import org.smartsupply.SmartSupply.dto.request.UserUpdateDto;
import org.smartsupply.SmartSupply.dto.response.UserResponseDto;
import org.smartsupply.SmartSupply.dto.response.UserStatsDto;
import org.smartsupply.SmartSupply.model.enums.Role;

import java.util.List;

public interface UserService {


    List<UserResponseDto> getAllUsers();

    List<UserResponseDto> getActiveUsers();


    List<UserResponseDto> getInactiveUsers();


    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmail(String email);

    List<UserResponseDto> getUsersByRole(Role role);


    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);


    UserResponseDto activateUser(Long id);


    UserResponseDto deactivateUser(Long id);


    void deleteUser(Long id);


    List<UserResponseDto> searchUsers(String keyword);


    boolean existsById(Long id);


    UserStatsDto getUserStats();
}