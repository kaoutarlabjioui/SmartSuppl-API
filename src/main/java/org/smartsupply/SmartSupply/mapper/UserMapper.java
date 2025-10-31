package org.smartsupply.SmartSupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.smartsupply.SmartSupply.dto.request.RegisterRequestDto;
import org.smartsupply.SmartSupply.dto.response.UserResponseDto;
import org.smartsupply.SmartSupply.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "password", ignore = true) // Le password sera encodé séparément
    User toEntity(RegisterRequestDto registerRequestDto);


    UserResponseDto toResponseDto(User user);
}