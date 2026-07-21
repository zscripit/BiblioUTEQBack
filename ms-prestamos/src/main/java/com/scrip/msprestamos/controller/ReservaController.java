package com.scrip.msprestamos.controller;

import com.scrip.msprestamos.dto.ReservaRequest;
import com.scrip.msprestamos.entity.Reserva;
import com.scrip.msprestamos.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<?> crearReserva(@Valid @RequestBody ReservaRequest request) {
        try {
            Reserva reserva = reservaService.crearReserva(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado al registrar la reserva: " + e.getMessage()));
        }
    }

    @GetMapping("/usuario/{usuarioId}/activas")
    public ResponseEntity<List<Reserva>> obtenerReservasActivasDeUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(reservaService.obtenerReservasActivasDeUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Reserva>> obtenerReservasDeUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(reservaService.obtenerReservasDeUsuario(usuarioId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INSTRUCTOR')")
    public ResponseEntity<List<Reserva>> obtenerTodasReservas() {
        return ResponseEntity.ok(reservaService.obtenerTodasReservas());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable UUID id) {
        try {
            Reserva reserva = reservaService.cancelarReserva(id);
            return ResponseEntity.ok(reserva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado al cancelar la reserva: " + e.getMessage()));
        }
    }
}
