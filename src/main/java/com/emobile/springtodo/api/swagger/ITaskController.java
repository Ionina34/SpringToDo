package com.emobile.springtodo.api.swagger;

import com.emobile.springtodo.api.input.CreateTaskRequest;
import com.emobile.springtodo.api.output.task.ListTaskResponse;
import com.emobile.springtodo.api.output.task.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Task controller", description = "Контроллер для доступа к задачам и управления ими")
public interface ITaskController {

    @Operation(
            summary = "Получение задачи",

            description = "Получение задачи по id",
            responses = {
                    @ApiResponse(
                            description = "Задача найдена",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 10,
                                                                "userId": 5,
                                                                "title": "Домашний быт",
                                                                "description": "Помыть посуду",
                                                                "status": "TODO",
                                                                "start": "2023-04-12 14:30:00",
                                                                "deadline": "2023-04-12 18:00:00",
                                                                "end": "2023-04-12 17:15:15"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "Задача не найдена",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 404,
                                                                "message": "Task bot found with id: 4",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<TaskResponse> getTask(Long id);

    @Operation(
            summary = "Получение все задач",

            description = "Получение задач по userId",
            responses = {
                    @ApiResponse(
                            description = "Задачи найдена",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            tasks: [
                                                                 {
                                                                     "id": 10,
                                                                     "userId": 5,
                                                                     "title": "Домашний быт",
                                                                     "description": "Помыть посуду",
                                                                     "status": "TODO",
                                                                     "start": "2023-04-12 14:30:00",
                                                                     "deadline": "2023-04-12 18:00:00",
                                                                     "end": "2023-04-12 17:15:15"
                                                                 },
                                                                 {
                                                                     "id": 7,
                                                                     "userId": 5,
                                                                     "title": "Работа",
                                                                     "description": "Заполнить документы",
                                                                     "status": "IN_PROGRESS",
                                                                     "start": "2023-07-12 14:30:00",
                                                                     "deadline": "2023-07-17 18:00:00",
                                                                     "end": "2023-04-15 13:15:00"
                                                                 }
                                                                 ],
                                                            total: 14,
                                                            limit: 2,
                                                            offset: 1
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<ListTaskResponse> getTasks(Long userId);

    @Operation(
            summary = "Сохранение задачи",
            description = "Сохраняет задачу",
            responses = {
                    @ApiResponse(
                            description = "Задача сохранена",
                            responseCode = "201",
                            content = @Content(
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 10,
                                                                "userId": 5,
                                                                "title": "Домашний быт",
                                                                "description": "Помыть посуду",
                                                                "status": "TODO",
                                                                "start": "2023-04-12 14:30:00",
                                                                "deadline": "2023-04-12 18:00:00",
                                                                "end": "2023-04-12 17:15:15"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "Пользователь, переданный в запросе не найден",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 404,
                                                                "message": "User not found with id: 5",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<TaskResponse> createTask(CreateTaskRequest request);

    @Operation(
            summary = "Обновление задачи",

            description = "Старт задачи по id",
            responses = {
                    @ApiResponse(
                            description = "Задача обновлена",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 10,
                                                                "userId": 5,
                                                                "title": "Домашний быт",
                                                                "description": "Помыть посуду",
                                                                "status": "IN_PROGRESS",
                                                                "start": "2023-04-12 14:30:00",
                                                                "deadline": "2023-04-12 18:00:00",
                                                                "end": "2023-04-12 17:15:15"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "Задача не найдена",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 404,
                                                                "message": "Task bot found with id: 4",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "У пользователя нет прав на задачу",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 403,
                                                                "message": "User access rights error",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<TaskResponse> startTask(Long id, Long userId);

    @Operation(
            summary = "Обновление задачи",

            description = "Окончание задачи по id",
            responses = {
                    @ApiResponse(
                            description = "Задача обновлена",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 10,
                                                                "userId": 5,
                                                                "title": "Домашний быт",
                                                                "description": "Помыть посуду",
                                                                "status": "DONE",
                                                                "start": "2023-04-12 14:30:00",
                                                                "deadline": "2023-04-12 18:00:00",
                                                                "end": "2023-04-12 17:15:15"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "Задача не найдена",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 404,
                                                                "message": "Task bot found with id: 4",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "У пользователя нет прав на задачу",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 403,
                                                                "message": "User access rights error",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<TaskResponse> endTask(Long id, Long userId);
}

