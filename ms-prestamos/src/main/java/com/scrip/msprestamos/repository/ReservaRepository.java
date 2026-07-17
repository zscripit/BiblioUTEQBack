package com.scrip.msprestamos.repository;

import com.scrip.msprestamos.entity.EstadoReserva;
import com.scrip.msprestamos.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    List<Reserva> findByUsuarioIdAndEstado(UUID usuarioId, EstadoReserva estado);
    List<Reserva> findByUsuarioId(UUID usuarioId);
}
