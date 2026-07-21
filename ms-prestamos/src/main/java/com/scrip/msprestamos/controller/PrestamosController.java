package com.scrip.msprestamos.controller;

import com.scrip.msprestamos.dto.PrestamoRequest;
import com.scrip.msprestamos.entity.Prestamo;
import com.scrip.msprestamos.service.PrestamoService;
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
@RequestMapping("/api/v1/prestamos")
@RequiredArgsConstructor
public class PrestamosController {

    private final PrestamoService prestamoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INSTRUCTOR')")
    public ResponseEntity<?> registrarPrestamo(@Valid @RequestBody PrestamoRequest request) {
        try {
            Prestamo prestamo = prestamoService.registrarPrestamo(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(prestamo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado al registrar el préstamo: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INSTRUCTOR')")
    public ResponseEntity<List<Prestamo>> listarPrestamos() {
        return ResponseEntity.ok(prestamoService.listarPrestamos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Prestamo>> listarPrestamosDeUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(prestamoService.listarPrestamosDeUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPrestamoPorId(@PathVariable("id") UUID id) {
        Prestamo prestamo = prestamoService.obtenerPrestamoPorId(id);
        if (prestamo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(prestamo);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> marcarPrestamoComoDevuelto(@PathVariable("id") UUID id) {
        try {
            prestamoService.marcarComoDevuelto(id);
            return ResponseEntity.ok(Map.of("message", "El préstamo ha sido marcado como devuelto con éxito"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado al marcar como devuelto: " + e.getMessage()));
        }
    }
}
