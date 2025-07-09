package com.emobile.springtodo.core.mapper;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.core.entity.db.User;
import com.emobile.springtodo.core.entity.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto userToDto(User user);

    User requestToUser(CreateUserRequest request);
}
