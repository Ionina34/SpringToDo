package com.emobile.springtodo.core.mapper;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.dto.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    @Mapping(source = "createdAt", target = "start")
    @Mapping(source = "endDate", target = "end")
    @Mapping(source = "task.user.id", target = "userId")
    TaskDto taskToDto(Task task);

    @Mapping(source = "request.userId", target = "user.id")
    Task requestToTask(CreateTaskRequest request);

    default List<TaskDto> listTaskToListTaskDto(List<Task> tasks) {
        return tasks.stream()
                .map(this::taskToDto)
                .toList();
    }
}
