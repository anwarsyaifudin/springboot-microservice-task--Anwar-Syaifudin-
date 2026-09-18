package com.anwar.bookmanagement.service;

import com.anwar.bookmanagement.dto.BookPatchRequest;
import com.anwar.bookmanagement.dto.BookRequest;
import com.anwar.bookmanagement.dto.BookResponse;
import com.anwar.bookmanagement.entity.Book;
import com.anwar.bookmanagement.exception.DuplicateIsbnException;
import com.anwar.bookmanagement.exception.ResourceNotFoundException;
import com.anwar.bookmanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookRequest request;

    @BeforeEach
    void setUp() {
        request = new BookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("9780132350884");
        request.setPublishedDate(LocalDate.of(2008, 8, 1));
    }

    @Test
    void shouldCreateBook() {
        Book saved = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        saved.setId(1L);

        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        BookResponse response = bookService.createBook(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(response.getIsbn()).isEqualTo("9780132350884");
        assertThat(response.getPublishedDate()).isEqualTo(LocalDate.of(2008, 8, 1));
    }

    @Test
    void shouldThrowDuplicateIsbnWhenCreatingDuplicateIsbn() {
        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateIsbnException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldGetAllBooks() {
        Book first = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        first.setId(1L);
        Book second = new Book("Refactoring", "Martin Fowler", "9780201485677", LocalDate.of(1999, 7, 8));
        second.setId(2L);

        when(bookRepository.findAll()).thenReturn(List.of(first, second));

        List<BookResponse> responses = bookService.getAllBooks();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("Clean Code");
        assertThat(responses.get(1).getTitle()).isEqualTo("Refactoring");
    }

    @Test
    void shouldGetBookById() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldUpdateBook() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        BookRequest updated = new BookRequest();
        updated.setTitle("Clean Code V2");
        updated.setAuthor("Robert C. Martin");
        updated.setIsbn("9780132350884");
        updated.setPublishedDate(LocalDate.of(2015, 1, 1));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("9780132350884", 1L)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.updateBook(1L, updated);

        assertThat(response.getTitle()).isEqualTo("Clean Code V2");
        assertThat(response.getPublishedDate()).isEqualTo(LocalDate.of(2015, 1, 1));
        verify(bookRepository).save(book);
    }

    @Test
    void shouldThrowDuplicateIsbnWhenUpdatingToExistingIsbn() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        BookRequest updated = new BookRequest();
        updated.setTitle("Clean Code");
        updated.setAuthor("Robert C. Martin");
        updated.setIsbn("9999999999999");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("9999999999999", 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, updated))
                .isInstanceOf(DuplicateIsbnException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldPatchBookWithOnlyProvidedFields() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        BookPatchRequest patch = new BookPatchRequest();
        patch.setTitle("Clean Code - Updated Edition");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.patchBook(1L, patch);

        assertThat(response.getTitle()).isEqualTo("Clean Code - Updated Edition");
        assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(response.getIsbn()).isEqualTo("9780132350884");
        assertThat(response.getPublishedDate()).isEqualTo(LocalDate.of(2008, 8, 1));
    }

    @Test
    void shouldPatchBookWhenEmptyBody() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        BookPatchRequest emptyPatch = new BookPatchRequest();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.patchBook(1L, emptyPatch);

        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
    }

    @Test
    void shouldRejectBlankFieldInPatch() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        BookPatchRequest patch = new BookPatchRequest();
        patch.setTitle("   ");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.patchBook(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void shouldDeleteBook() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        book.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }
}