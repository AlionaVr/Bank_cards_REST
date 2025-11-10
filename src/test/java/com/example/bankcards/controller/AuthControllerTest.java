package com.example.bankcards.controller;

import com.example.bankcards.SecurityTestConfig;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.MyJwtFilter;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MyJwtFilter myJwtFilter;

    @Test
    @DisplayName("Should return 200 OK when login is successful")
    void login_WhenValidRequest_ThenReturnToken() throws Exception {
        LoginResponse response = new LoginResponse("jwt-token-123");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        String json = """
                {
                  "login": "john",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth-token").value("jwt-token-123"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login is invalid")
    void login_WhenRequestInvalid_ThenReturnBadRequest() throws Exception {
        String json = """
                {
                  "login": ""
                }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 OK when register is successful")
    void registerUser_WhenValidRequest_ThenReturnUserDto() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(UUID.randomUUID());
        dto.setLogin("newuser");
        dto.setEmail("new@bank.com");
        dto.setFirstName("New");
        dto.setLastName("User");
        dto.setRole(UserRole.USER);

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(dto);

        String json = """
                {
                  "login": "newuser",
                  "password": "Password123",
                  "email": "new@bank.com",
                  "firstName": "New",
                  "lastName": "User"
                }
                """;

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@bank.com"));

        verify(authService).register(any(UserRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when register is invalid")
    void registerUser_WhenRequestInvalid_ThenReturnBadRequest() throws Exception {
        // invalid email + too short password -> validation should fail
        String json = """
                {
                  "login": "us",
                  "password": "short",
                  "email": "not-an-email",
                  "firstName": "N",
                  "lastName": ""
                }
                """;

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 OK when logout is successful")
    void logout_WhenCalled_ThenReturnOk() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk());
    }
}