package com.scrip.msprestamos.repository;

import com.scrip.msprestamos.dto.LibroMasPrestadoResponse;
import com.scrip.msprestamos.entity.Prestamo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PrestamoRepository extends JpaRepository<Prestamo, UUID> {

    long countByFechaPrestamoGreaterThanEqualAndFechaPrestamoLessThan(
            OffsetDateTime fechaInicio,
            OffsetDateTime fechaFinExclusiva
    );

    @Query("""
            select new com.scrip.msprestamos.dto.LibroMasPrestadoResponse(
                p.libroId,
                l.titulo,
                l.isbn,
                count(p)
            )
            from Prestamo p
            join Libro l on l.id = p.libroId
            group by p.libroId, l.titulo, l.isbn
            order by count(p) desc
            """)
    List<LibroMasPrestadoResponse> findLibrosMasPrestados(Pageable pageable);
}
