package com.scrip.msprestamos.controller;

import com.scrip.msprestamos.dto.LibroMasPrestadoResponse;
import com.scrip.msprestamos.dto.PrestamosPeriodoResponse;
import com.scrip.msprestamos.dto.UsuarioMasSancionadoResponse;
import com.scrip.msprestamos.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    @GetMapping("/prestamos-periodo")
    public ResponseEntity<PrestamosPeriodoResponse> obtenerPrestamosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(estadisticasService.contarPrestamosPorPeriodo(fechaInicio, fechaFin));
    }

    @GetMapping("/libros-mas-prestados")
    public ResponseEntity<List<LibroMasPrestadoResponse>> obtenerLibrosMasPrestados(
            @RequestParam(required = false) Integer limite
    ) {
        return ResponseEntity.ok(estadisticasService.obtenerLibrosMasPrestados(limite));
    }

    @GetMapping("/usuarios-mas-sancionados")
    public ResponseEntity<List<UsuarioMasSancionadoResponse>> obtenerUsuariosMasSancionados(
            @RequestParam(required = false) Integer limite
    ) {
        return ResponseEntity.ok(estadisticasService.obtenerUsuariosMasSancionados(limite));
    }
}
