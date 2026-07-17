package com.scrip.mscatalogo.controller;

import com.scrip.mscatalogo.dto.BookDto;
import com.scrip.mscatalogo.dto.BookRequest;
import com.scrip.mscatalogo.dto.StockRequest;
import com.scrip.mscatalogo.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogController {

    private final BookService bookService;

    public CatalogController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookDto> listar(@RequestParam(required = false) String busqueda,
                                 @RequestParam(required = false) String categoria) {
        return bookService.listar(busqueda, categoria);
    }

    @GetMapping("/categorias")
    public List<String> listarCategorias() {
        return bookService.listarCategorias();
    }

    @GetMapping("/{id}")
    public BookDto obtener(@PathVariable UUID id) {
        return bookService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto crear(@Valid @RequestBody BookRequest request) {
        return bookService.crear(request);
    }

    @PutMapping("/{id}")
    public BookDto actualizar(@PathVariable UUID id, @Valid @RequestBody BookRequest request) {
        return bookService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        bookService.eliminar(id);
    }

    @PatchMapping("/{id}/reservar")
    public BookDto reservar(@PathVariable UUID id, @Valid @RequestBody StockRequest request) {
        return bookService.reservarStock(id, request.cantidad());
    }

    @PatchMapping("/{id}/liberar")
    public BookDto liberar(@PathVariable UUID id, @Valid @RequestBody StockRequest request) {
        return bookService.liberarStock(id, request.cantidad());
    }
}
