package backendjava.controller;

import backendjava.dto.BudgetLimitRequest;
import backendjava.dto.BudgetLimitResponse;
import backendjava.service.BudgetLimitService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BudgetLimitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BudgetLimitService budgetLimitService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BudgetLimitController(budgetLimitService)).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getLimits_ShouldReturnList() throws Exception {
        when(budgetLimitService.getLimits(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/budget-limits")
                        .param("month", "6")
                        .param("year", "2026"))
                .andExpect(status().isOk());
    }

    @Test
    void setLimit_ShouldReturnCreated() throws Exception {
        BudgetLimitResponse response = new BudgetLimitResponse(
                1L, 1L, "Еда", new BigDecimal("10000"), 6, 2026);

        when(budgetLimitService.setLimit(anyLong(), any(BudgetLimitRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/budget-limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "categoryId": 1,
                                    "limitAmount": 10000,
                                    "month": 6,
                                    "year": 2026
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limitAmount").value(10000));
    }

    @Test
    void deleteLimit_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/budget-limits/1"))
                .andExpect(status().isOk());
    }
}
