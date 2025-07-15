package com.emobile.springtodo.api.swagger;

import com.emobile.springtodo.api.input.CreateUserRequest;
import com.emobile.springtodo.api.output.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User controller", description = "Контроллер для доступа к пользователям")
public interface IUserController {

    @Operation(
            summary = "Получения пользователя",

            description = "Получение пользователя по id",
            responses = {
                    @ApiResponse(
                            description = "Пользователь найден",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 7,
                                                                "username": "Sergey",
                                                                "email": "email@mail.ru"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "Пользователь не найден",
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 404,
                                                                "message": "User bot found with id: 4",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<UserResponse> getUser(Long id);

    @Operation(
            summary = "Сохранение пользователя",
            description = "Сохраняет задачу",
            responses = {
                    @ApiResponse(
                            description = "Пользователь сохранен",
                            responseCode = "201",
                            content = @Content(
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 7,
                                                                "username": "Sergey",
                                                                "email": "email@mail.ru"
                                                            }
                                                            """
                                            )
                                    }
                            )),
                    @ApiResponse(
                            description = "Пользователь уже существует",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "status": 404,
                                                                "message": "User already exists with email or username",
                                                                "timestamp": "2023-04-12 14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            ))
            }
    )
    ResponseEntity<UserResponse> createUser(CreateUserRequest request);

    @Operation(
            summary = "Удаление пользователя",
            description = "Удаление пользователя по id",
            responses = {
                    @ApiResponse(
                            description = "Пользователь удален",
                            responseCode = "200",
                            content = @Content(
                                    examples = {
                                            @ExampleObject(
                                                    value = "SUCCESS"
                                            )
                                    }

                            ))
            }
    )
    ResponseEntity<Void> deleteUser(Long id);
}
