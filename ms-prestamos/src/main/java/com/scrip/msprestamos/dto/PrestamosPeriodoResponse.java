package com.scrip.msprestamos.dto;

import java.time.LocalDate;

public record PrestamosPeriodoResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        long totalPrestamos
) {
}
