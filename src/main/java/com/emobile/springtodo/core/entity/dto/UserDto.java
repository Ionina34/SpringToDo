package com.emobile.springtodo.core.entity.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class UserDto implements Serializable {
    private Long id;
    private String username;
    private String email;
}
