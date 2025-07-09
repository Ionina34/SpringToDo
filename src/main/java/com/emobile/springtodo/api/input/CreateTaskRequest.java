package com.emobile.springtodo.api.input;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@NoArgsConstructor
@Getter
@Setter
public class CreateTaskRequest {

    @NotNull(message = "Идентификатор пользователя должен быть указан")
    @Positive(message = "Идентификатор должен быть положительным")
    private Long userId;

    @NotBlank(message = "Заголовок должен быть указан")
    private String title;

    @NotBlank(message = "Описание должно быть указано")
    private String description;

    @NotNull(message = "Дедлайн должен быть указан")
    @FutureOrPresent(message = "Дедлайн не может быть раньше сегодняшней даты")
    private Date deadline;
}
