package backendjava.controller;

import backendjava.dto.TransactionRequest;
import backendjava.dto.TransactionResponse;
import backendjava.dto.TransactionResponseWithWarning;
import backendjava.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService)).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getTransactions_ShouldReturnList() throws Exception {
        when(transactionService.getTransactions(anyLong(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    void createTransaction_ShouldReturnCreated() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L, 1L, "Еда", new BigDecimal("100"), LocalDate.now(), "Комментарий", "EXPENSE", null);
        TransactionResponseWithWarning responseWithWarning = new TransactionResponseWithWarning(response, null);

        when(transactionService.createTransaction(anyLong(), any(TransactionRequest.class)))
                .thenReturn(responseWithWarning);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "categoryId": 1,
                                    "amount": 100,
                                    "date": "2026-06-01",
                                    "comment": "Комментарий",
                                    "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.amount").value(100));
    }

    @Test
    void updateTransaction_ShouldReturnUpdated() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L, 1L, "Еда", new BigDecimal("200"), LocalDate.now(), "Комментарий", "EXPENSE", null);
        TransactionResponseWithWarning responseWithWarning = new TransactionResponseWithWarning(response, null);

        when(transactionService.updateTransaction(anyLong(), anyLong(), any(TransactionRequest.class)))
                .thenReturn(responseWithWarning);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "categoryId": 1,
                                    "amount": 200,
                                    "date": "2026-06-01",
                                    "comment": "Комментарий",
                                    "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.amount").value(200));
    }

    @Test
    void deleteTransaction_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getBalance_ShouldReturnValue() throws Exception {
        when(transactionService.getCurrentBalance(anyLong())).thenReturn(new BigDecimal("3000"));

        mockMvc.perform(get("/api/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(3000));
    }

    @Test
    void getTotalBalance_ShouldReturnValue() throws Exception {
        when(transactionService.getTotalBalance(anyLong())).thenReturn(new BigDecimal("4000"));

        mockMvc.perform(get("/api/transactions/total-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(4000));
    }
}
