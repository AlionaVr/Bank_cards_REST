package com.example.bankcards.controller;

import com.example.bankcards.SecurityTestConfig;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.UserUpdateRequest;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.MyJwtFilter;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MyJwtFilter myJwtFilter;

    @Test
    @DisplayName("Should return 200 OK when user is created successfully")
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserExists_ThenReturnUserDto() throws Exception {
        UUID id = UUID.randomUUID();
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setLogin("admin");
        dto.setEmail("admin@test.com");
        dto.setRole(UserRole.ADMIN);

        when(userService.getUserById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("Should return 200 OK when all users are returned")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WhenValidRequest_ThenReturnPagedResult() throws Exception {
        UserDto user = new UserDto();
        user.setId(UUID.randomUUID());
        user.setLogin("john");
        user.setEmail("john@test.com");
        Page<UserDto> page = new PageImpl<>(List.of(user), PageRequest.of(0, 1), 1);

        when(userService.getAllUsers(eq(0), eq(1), eq("createdAt"))).thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].login").value("john"))
                .andExpect(jsonPath("$.content[0].email").value("john@test.com"));
    }

    @Test
    @DisplayName("Should return 200 OK when user is deleted successfully")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenValidId_ThenReturnOk() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isOk());

        verify(userService).deleteUser(id);
    }

    @Test
    @DisplayName("Should return 200 OK when user is updated successfully")
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenValidRequest_ThenReturnUpdatedDto() throws Exception {
        UUID id = UUID.randomUUID();
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("updated@test.com");
        req.setFirstName("Updated");
        req.setLastName("User");
        req.setRole(UserRole.ADMIN);

        UserDto updated = new UserDto();
        updated.setId(id);
        updated.setEmail("updated@test.com");
        updated.setFirstName("Updated");
        updated.setLastName("User");
        updated.setRole(UserRole.ADMIN);

        when(userService.updateUser(eq(id), any(UserUpdateRequest.class))).thenReturn(updated);

        String json = """
                {
                  "email": "updated@test.com",
                  "firstName": "Updated",
                  "lastName": "User",
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(patch("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).updateUser(eq(id), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user is not admin")
    @WithMockUser(roles = "USER")
    void getUserById_WhenUserIsNotAdmin_ThenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
