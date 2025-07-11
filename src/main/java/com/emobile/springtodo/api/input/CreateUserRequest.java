package com.emobile.springtodo.api.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Setter
@Getter
public class CreateUserRequest implements Serializable {

    @NotBlank(message = "Username must be specified")
    private String username;

    @NotBlank(message = "Email address must be specified")
    @Email(message = "Email address must be of the form: user_name@Domain")
    private String email;
}
