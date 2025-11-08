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
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequestDto registerRequestDto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toEntity(UserResponseDto dto);

    UserResponseDto toResponseDto(User user);
}