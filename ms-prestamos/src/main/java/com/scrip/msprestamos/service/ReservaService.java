package com.scrip.msprestamos.service;

import com.scrip.msprestamos.client.NotificacionClient;
import com.scrip.msprestamos.dto.ReservaRequest;
import com.scrip.msprestamos.entity.EstadoReserva;
import com.scrip.msprestamos.entity.Libro;
import com.scrip.msprestamos.entity.Reserva;
import com.scrip.msprestamos.repository.LibroRepository;
import com.scrip.msprestamos.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private static final int DIAS_EXPIRACION_RESERVA = 3;

    private final ReservaRepository reservaRepository;
    private final LibroRepository libroRepository;
    private final NotificacionClient notificacionClient;

    @Transactional
    public Reserva crearReserva(ReservaRequest request) {
        Libro libro = libroRepository.findById(request.getLibroId())
                .orElseThrow(() -> new IllegalArgumentException("El libro especificado no existe en el catálogo."));

        if (libro.getActivo() != null && !libro.getActivo()) {
            throw new IllegalArgumentException("El libro especificado no está activo.");
        }

        int stockDisponible = (libro.getStock() != null ? libro.getStock() : 0)
                - (libro.getStockReservado() != null ? libro.getStockReservado() : 0);
        if (stockDisponible <= 0) {
            throw new IllegalArgumentException("No hay ejemplares disponibles de '" + libro.getTitulo() + "' para reservar.");
        }

        libro.setStockReservado((libro.getStockReservado() != null ? libro.getStockReservado() : 0) + 1);
        libroRepository.save(libro);

        OffsetDateTime fechaReserva = OffsetDateTime.now();
        Reserva reserva = Reserva.builder()
                .usuarioId(request.getUsuarioId())
                .libroId(request.getLibroId())
                .estado(EstadoReserva.ACTIVA)
                .fechaReserva(fechaReserva)
                .fechaExpiracion(fechaReserva.plusDays(DIAS_EXPIRACION_RESERVA))
                .build();

        reserva = reservaRepository.save(reserva);

        String mensaje = String.format(
                "Tu reserva del libro '%s' está activa. Recógelo antes del %s.",
                libro.getTitulo(),
                reserva.getFechaExpiracion().toLocalDate()
        );
        notificar(reserva, "RESERVA_CREADA", mensaje);

        return reserva;
    }

    public List<Reserva> obtenerReservasActivasDeUsuario(UUID usuarioId) {
        return reservaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoReserva.ACTIVA);
    }

    public List<Reserva> obtenerReservasDeUsuario(UUID usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    public List<Reserva> obtenerTodasReservas() {
        return reservaRepository.findAll();
    }

    @Transactional
    public Reserva cancelarReserva(UUID id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La reserva especificada no existe."));

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new IllegalArgumentException("La reserva ya no está activa.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        liberarStockReservado(reserva.getLibroId());
        reserva = reservaRepository.save(reserva);

        notificar(reserva, "RESERVA_CANCELADA", "Tu reserva ha sido cancelada.");

        return reserva;
    }

    private void liberarStockReservado(UUID libroId) {
        libroRepository.findById(libroId).ifPresent(libro -> {
            int actual = libro.getStockReservado() != null ? libro.getStockReservado() : 0;
            libro.setStockReservado(Math.max(0, actual - 1));
            libroRepository.save(libro);
        });
    }

    private void notificar(Reserva reserva, String tipo, String mensaje) {
        Map<String, Object> notificationRequest = Map.of(
                "usuarioId", reserva.getUsuarioId().toString(),
                "tipo", tipo,
                "referenciaId", reserva.getId().toString(),
                "mensaje", mensaje
        );

        try {
            notificacionClient.enviarNotificacion(notificationRequest);
        } catch (Exception e) {
            System.err.println("No se pudo enviar la notificación de " + tipo + ": " + e.getMessage());
        }
    }
}
