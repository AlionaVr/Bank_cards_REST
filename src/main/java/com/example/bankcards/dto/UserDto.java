package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(description = "User DTO")
public class UserDto {
    private UUID id;
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public UserDto(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
    }
}
