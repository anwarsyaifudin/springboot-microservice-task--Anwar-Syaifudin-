package com.anwar.bookmanagement.repository;

import com.anwar.bookmanagement.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldPersistAndFindBook() {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(bookRepository.findById(saved.getId())).isPresent();
        assertThat(bookRepository.existsByIsbn("9780132350884")).isTrue();
    }

    @Test
    void shouldEnforceUniqueIsbn() {
        Book first = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        Book second = new Book("Clean Architecture", "Robert C. Martin", "9780132350884", LocalDate.of(2017, 9, 1));

        bookRepository.save(first);

        assertThatThrownBy(() -> bookRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldDetectIsbnOwnedByAnotherBook() {
        Book first = new Book("Clean Code", "Robert C. Martin", "9780132350884", LocalDate.of(2008, 8, 1));
        Book second = new Book("Refactoring", "Martin Fowler", "9780201485677", LocalDate.of(1999, 7, 8));

        bookRepository.saveAll(List.of(first, second));

        assertThat(bookRepository.existsByIsbnAndIdNot("9780132350884", first.getId())).isFalse();
        assertThat(bookRepository.existsByIsbnAndIdNot("9780132350884", second.getId())).isTrue();
    }
}