package com.emobile.springtodo.api.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class CreateUserRequest {

    @NotBlank(message = "Имя пользователя должно быть указано")
    private String username;

    @NotBlank(message = "Электронный адрес должен быть указан")
    @Email(message = "Электронный адрес должен быть вида: Имя_Пользователя@Домен")
    private String email;
}
