package com.example.bankcards.controller;

import com.example.bankcards.SecurityTestConfig;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.MyJwtFilter;
import com.example.bankcards.service.TransferService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import(SecurityTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MyJwtFilter myJwtFilter;

    @Test
    @DisplayName("Should return 200 OK when transfer is successful")
    @WithMockUser(roles = "USER")
    void transferMoneyBetweenUserCards_returnsDto() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setId(UUID.randomUUID());
        dto.setAmount(new BigDecimal("100.00"));

        when(transferService.transferBetweenCards(any())).thenReturn(dto);

        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        String body = """
                {
                  "fromCardId": "%s",
                  "toCardId": "%s",
                  "amount": 100.00,
                  "description": "Test transfer"
                }
                """.formatted(fromCardId, toCardId);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @DisplayName("Should return 200 OK when transfer history is returned")
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getTransferHistory_returnsPage() throws Exception {
        TransferDto dto = new TransferDto();
        dto.setId(UUID.randomUUID());
        dto.setAmount(new BigDecimal("50.00"));

        Page<TransferDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(transferService.getTransferHistory(anyInt(), anyInt(), any())).thenReturn(page);

        UUID cardId = UUID.randomUUID();

        mockMvc.perform(get("/api/transfers/history")
                        .param("page", "0")
                        .param("size", "10")
                        .param("cardId", cardId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(50.00));
    }
}
