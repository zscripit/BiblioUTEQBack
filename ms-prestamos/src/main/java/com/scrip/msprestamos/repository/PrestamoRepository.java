package com.scrip.msprestamos.repository;

import com.scrip.msprestamos.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PrestamoRepository extends JpaRepository<Prestamo, UUID> {

    List<Prestamo> findByFechaPrestamoGreaterThanEqualAndFechaPrestamoLessThan(
            OffsetDateTime fechaInicio,
            OffsetDateTime fechaFinExclusiva
    );
}
