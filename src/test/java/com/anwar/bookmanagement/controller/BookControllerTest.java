package com.anwar.bookmanagement.controller;

import com.anwar.bookmanagement.dto.BookResponse;
import com.anwar.bookmanagement.exception.GlobalExceptionHandler;
import com.anwar.bookmanagement.exception.ResourceNotFoundException;
import com.anwar.bookmanagement.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void shouldCreateBook() throws Exception {
        Map<String, Object> payload = Map.of(
                "title", "Clean Code",
                "author", "Robert C. Martin",
                "isbn", "9780132350884",
                "publishedDate", "2008-08-01"
        );

        BookResponse response = new BookResponse(1L, "Clean Code", "Robert C. Martin",
                "9780132350884", LocalDate.of(2008, 8, 1));

        when(bookService.createBook(any())).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    void shouldReturnValidationErrorWhenTitleAndIsbnMissing() throws Exception {
        Map<String, Object> payload = Map.of(
                "author", "Robert C. Martin",
                "publishedDate", "2008-08-01"
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"))
                .andExpect(jsonPath("$.errors.isbn").value("ISBN is required"));
    }

    @Test
    void shouldGetAllBooks() throws Exception {
        BookResponse first = new BookResponse(1L, "Clean Code", "Robert C. Martin",
                "9780132350884", LocalDate.of(2008, 8, 1));

        when(bookService.getAllBooks()).thenReturn(List.of(first));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void shouldGetBookById() throws Exception {
        BookResponse response = new BookResponse(1L, "Clean Code", "Robert C. Martin",
                "9780132350884", LocalDate.of(2008, 8, 1));

        when(bookService.getBookById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        when(bookService.getBookById(999L))
                .thenThrow(new ResourceNotFoundException("Book with id 999 not found"));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Book with id 999 not found"));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        Map<String, Object> payload = Map.of(
                "title", "Clean Code V2",
                "author", "Robert C. Martin",
                "isbn", "9780132350884",
                "publishedDate", "2015-01-01"
        );

        BookResponse response = new BookResponse(1L, "Clean Code V2", "Robert C. Martin",
                "9780132350884", LocalDate.of(2015, 1, 1));

        when(bookService.updateBook(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code V2"));
    }

    @Test
    void shouldPartiallyUpdateBook() throws Exception {
        Map<String, Object> payload = Map.of("title", "Clean Code - Updated Edition");

        BookResponse response = new BookResponse(1L, "Clean Code - Updated Edition", "Robert C. Martin",
                "9780132350884", LocalDate.of(2008, 8, 1));

        when(bookService.patchBook(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code - Updated Edition"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }
}