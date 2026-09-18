package com.anwar.bookmanagement.service;

import com.anwar.bookmanagement.dto.BookPatchRequest;
import com.anwar.bookmanagement.dto.BookRequest;
import com.anwar.bookmanagement.dto.BookResponse;
import com.anwar.bookmanagement.entity.Book;
import com.anwar.bookmanagement.exception.DuplicateIsbnException;
import com.anwar.bookmanagement.exception.ResourceNotFoundException;
import com.anwar.bookmanagement.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        ensureIsbnNotTaken(request.getIsbn(), null);
        Book book = new Book(request.getTitle(), request.getAuthor(), request.getIsbn(), request.getPublishedDate());
        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        return toResponse(findBookById(id));
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookById(id);
        ensureIsbnNotTaken(request.getIsbn(), id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublishedDate(request.getPublishedDate());
        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse patchBook(Long id, BookPatchRequest request) {
        Book book = findBookById(id);

        if (request.getTitle() != null) {
            requireText(request.getTitle(), "Title");
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            requireText(request.getAuthor(), "Author");
            book.setAuthor(request.getAuthor());
        }
        if (request.getIsbn() != null) {
            requireText(request.getIsbn(), "ISBN");
            ensureIsbnNotTaken(request.getIsbn(), id);
            book.setIsbn(request.getIsbn());
        }
        if (request.getPublishedDate() != null) {
            book.setPublishedDate(request.getPublishedDate());
        }

        return toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        bookRepository.delete(findBookById(id));
    }

    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " not found"));
    }

    private void ensureIsbnNotTaken(String isbn, Long currentId) {
        String trimmed = isbn.trim();
        boolean exists = currentId == null
                ? bookRepository.existsByIsbn(trimmed)
                : bookRepository.existsByIsbnAndIdNot(trimmed, currentId);
        if (exists) {
            throw new DuplicateIsbnException("Book with ISBN " + trimmed + " already exists");
        }
    }

    private void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPublishedDate());
    }
}