package backendjava.controller;

import backendjava.dto.CategoryRequest;
import backendjava.dto.CategoryResponse;
import backendjava.service.CategoryService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService)).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getCategories_ShouldReturnList() throws Exception {
        when(categoryService.getAllCategories(anyLong()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void createCategory_ShouldReturnCreated() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Еда");
        req.setType("EXPENSE");
        req.setColor("#FF0000");

        when(categoryService.createCategory(anyLong(), any(CategoryRequest.class)))
                .thenReturn(new CategoryResponse(1L, "Еда", "EXPENSE", "#FF0000", null));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Еда",
                                    "type": "EXPENSE",
                                    "color": "#FF0000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Еда"));
    }

    @Test
    void updateCategory_ShouldReturnUpdated() throws Exception {
        when(categoryService.updateCategory(anyLong(), anyLong(), any(CategoryRequest.class)))
                .thenReturn(new CategoryResponse(1L, "Еда (обновлено)", "EXPENSE", "#FF00FF", null));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Еда (обновлено)",
                                    "type": "EXPENSE",
                                    "color": "#FF00FF"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Еда (обновлено)"));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }
}
