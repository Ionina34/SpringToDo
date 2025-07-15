package com.emobile.springtodo.api.input;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
public class CreateTaskRequest implements Serializable {

    @NotNull(message = "User ID must be specified")
    @Positive(message = "ID must be positive")
    private Long userId;

    @NotBlank(message = "Title must be specified")
    private String title;

    @NotBlank(message = "Description must be specified")
    private String description;

    @NotNull(message = "Deadline must be specified")
    @FutureOrPresent(message = "Deadline cannot be earlier than today")
    private Timestamp deadline;
}
