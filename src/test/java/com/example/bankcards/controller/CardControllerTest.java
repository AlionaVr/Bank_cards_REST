package com.example.bankcards.controller;

import com.example.bankcards.SecurityTestConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.MyJwtFilter;
import com.example.bankcards.service.CardService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(SecurityTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private MyJwtFilter myJwtFilter;

    @Test
    @DisplayName("Should return 200 OK when card is created successfully")
    @WithMockUser(roles = "ADMIN")
    void createCard_returnsCreatedCard() throws Exception {
        UUID userId = UUID.randomUUID();
        CardDto dto = CardDto.builder()
                .id(UUID.randomUUID())
                .cardHolderName("John Doe")
                .build();

        when(cardService.createCard(eq(userId), any())).thenReturn(dto);

        String body = """
                {
                  "cardHolderName": "John Doe",
                  "initialBalance": 1000.00
                }
                """;

        mockMvc.perform(post("/api/cards")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"));
    }

    @Test
    @DisplayName("Should return 200 OK when card is deleted successfully")
    @WithMockUser(roles = "ADMIN")
    void deleteCard_returnsOk() throws Exception {
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(delete("/api/cards")
                        .param("cardId", cardId.toString()))
                .andExpect(status().isOk());

        verify(cardService).deleteCard(cardId);
    }

    @Test
    @DisplayName("Should return 200 OK when card is activated successfully")
    @WithMockUser(roles = "ADMIN")
    void activateCard_returnsDto() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardDto dto = CardDto.builder().id(cardId).status(CardStatus.ACTIVE).build();

        when(cardService.activateCard(cardId)).thenReturn(dto);

        mockMvc.perform(put("/api/cards/activate/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 200 OK when card is blocked successfully")
    @WithMockUser(roles = "ADMIN")
    void blockCard_returnsDto() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardDto dto = CardDto.builder().id(cardId).status(CardStatus.BLOCKED).build();

        when(cardService.blockCard(cardId)).thenReturn(dto);

        mockMvc.perform(put("/api/cards/block/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @DisplayName("Should return 200 OK when card is unblocked successfully")
    @WithMockUser(roles = "USER")
    void requestBlockCard_returnsDto() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardDto dto = CardDto.builder().id(cardId).status(CardStatus.BLOCKED).build();

        when(cardService.requestBlockCard(cardId)).thenReturn(dto);

        mockMvc.perform(put("/api/cards/request-block/{cardId}", cardId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 OK when user cards are returned")
    @WithMockUser(roles = "USER")
    void getUserCards_returnsPage() throws Exception {
        UUID userId = UUID.randomUUID();
        CardDto dto = CardDto.builder()
                .id(UUID.randomUUID())
                .cardHolderName("User Card")
                .build();

        Page<CardDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(cardService.getUserCards(eq(userId), eq(CardStatus.ACTIVE), eq("John"), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/cards/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE")
                        .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardHolderName").value("User Card"));
    }

    @Test
    @DisplayName("Should return 200 OK when all cards are returned")
    @WithMockUser(roles = "ADMIN")
    void getAllCards_returnsList() throws Exception {
        CardDto dto = CardDto.builder()
                .id(UUID.randomUUID())
                .cardHolderName("Admin Card")
                .build();

        when(cardService.getAllCards()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardHolderName").value("Admin Card"));
    }

    @Test
    @DisplayName("Should return 200 OK when card balance is returned")
    @WithMockUser(roles = "USER")
    void getBalance_returnsBigDecimal() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.getBalance(cardId)).thenReturn(new BigDecimal("123.45"));

        mockMvc.perform(get("/api/cards/balance/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));
    }
}
