package com.anwar.bookmanagement.service;

import com.anwar.bookmanagement.dto.BookPatchRequest;
import com.anwar.bookmanagement.dto.BookRequest;
import com.anwar.bookmanagement.dto.BookResponse;

import java.util.List;

public interface BookService {

    BookResponse createBook(BookRequest request);

    List<BookResponse> getAllBooks();

    BookResponse getBookById(Long id);

    BookResponse updateBook(Long id, BookRequest request);

    BookResponse patchBook(Long id, BookPatchRequest request);

    void deleteBook(Long id);
}