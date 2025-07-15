package com.emobile.springtodo.api.mapper;

import com.emobile.springtodo.api.output.task.TaskResponse;
import com.emobile.springtodo.api.output.task.ListTaskResponse;
import com.emobile.springtodo.api.output.user.UserResponse;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import com.emobile.springtodo.core.entity.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResponseMapper {

    TaskResponse taskToResponse(TaskDto taskDto);

    @Mapping(source = "id", target = "id")
    UserResponse userToResponse(UserDto userDto);

    default ListTaskResponse listTaskToResponse(List<TaskDto> tasks, long total, int limit, int offset) {
        ListTaskResponse response = new ListTaskResponse();
        response.setTasks(
                tasks.stream()
                        .map(this::taskToResponse)
                        .toList()
        );
        response.setTotal(total);
        response.setLimit(limit);
        response.setOffset(offset);
        return response;
    }
}
