package com.scrip.mscatalogo.service;

import com.scrip.mscatalogo.dto.BookDto;
import com.scrip.mscatalogo.dto.BookRequest;
import com.scrip.mscatalogo.entity.Libro;
import com.scrip.mscatalogo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookDto> listar(String busqueda, String categoria) {
        List<Libro> libros = (busqueda == null || busqueda.isBlank())
                ? bookRepository.findByActivoTrue()
                : bookRepository.buscar(busqueda);

        return libros.stream()
                .filter(libro -> categoria == null || categoria.isBlank()
                        || categoria.equalsIgnoreCase(libro.getCategoria()))
                .map(this::toDto)
                .toList();
    }

    public BookDto obtener(UUID id) {
        return toDto(buscarPorId(id));
    }

    public List<String> listarCategorias() {
        return bookRepository.buscarCategorias();
    }

    public BookDto crear(BookRequest request) {
        bookRepository.findByIsbn(request.isbn()).ifPresent(libro -> {
            throw new IllegalArgumentException("Ya existe un libro con ese ISBN");
        });

        Libro libro = Libro.builder()
                .titulo(request.titulo())
                .isbn(request.isbn())
                .autor(request.autor())
                .categoria(request.categoria())
                .stock(request.stock())
                .portadaUrl(request.portadaUrl())
                .build();

        return toDto(bookRepository.save(libro));
    }

    public BookDto actualizar(UUID id, BookRequest request) {
        Libro libro = buscarPorId(id);
        libro.setTitulo(request.titulo());
        libro.setIsbn(request.isbn());
        libro.setAutor(request.autor());
        libro.setCategoria(request.categoria());
        libro.setStock(request.stock());
        libro.setPortadaUrl(request.portadaUrl());
        return toDto(bookRepository.save(libro));
    }

    public void eliminar(UUID id) {
        Libro libro = buscarPorId(id);
        libro.setActivo(false);
        bookRepository.save(libro);
    }

    public BookDto reservarStock(UUID id, int cantidad) {
        Libro libro = buscarPorId(id);
        int disponible = libro.getStock() - libro.getStockReservado();
        if (disponible < cantidad) {
            throw new IllegalArgumentException("No hay stock disponible suficiente");
        }
        libro.setStockReservado(libro.getStockReservado() + cantidad);
        return toDto(bookRepository.save(libro));
    }

    public BookDto liberarStock(UUID id, int cantidad) {
        Libro libro = buscarPorId(id);
        libro.setStockReservado(Math.max(0, libro.getStockReservado() - cantidad));
        return toDto(bookRepository.save(libro));
    }

    private Libro buscarPorId(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado"));
    }

    private BookDto toDto(Libro libro) {
        return new BookDto(
                libro.getId(),
                libro.getTitulo(),
                libro.getIsbn(),
                libro.getAutor(),
                libro.getCategoria(),
                libro.getStock(),
                libro.getStockReservado(),
                libro.getActivo(),
                libro.getPortadaUrl()
        );
    }
}
