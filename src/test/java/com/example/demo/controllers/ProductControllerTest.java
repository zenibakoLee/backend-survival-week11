package com.example.demo.controllers;

import com.example.demo.application.product.CreateProductService;
import com.example.demo.application.product.GetProductListService;
import com.example.demo.dtos.ProductListDto;
import com.example.demo.models.Money;
import com.example.demo.utils.ImageStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.util.List;

import static com.example.demo.controllers.helpers.ResultMatchers.contentContains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetProductListService getProductListService;

    @MockBean
    private CreateProductService createProductService;

    @MockBean
    private ImageStorage imageStorage;

    private final String filename = "src/test/resources/files/test.jpg";

    @Test
    @DisplayName("GET /products")
    void list() throws Exception {
        ProductListDto.ProductDto productDto =
                new ProductListDto.ProductDto("test-id", "제품", 100_000L, filename);

        given(getProductListService.getProductListDto()).willReturn(
                new ProductListDto(List.of(productDto)));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(contentContains("제품"));
    }

    @Test
    @DisplayName("POST /products")
    void create() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg",
                new FileInputStream(filename));

        given(imageStorage.save(any())).willReturn(filename);

        mockMvc.perform(multipart("/products")
                        .file(file)
                        .param("name", "멋진 제품")
                        .param("price", String.valueOf(100_000L)))
                .andExpect(status().isCreated());

        verify(createProductService)
                .createProduct("멋진 제품", new Money(100_000L), filename);
        verify(imageStorage).save(any());
    }
}
