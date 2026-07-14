package com.scrip.msprestamos.dto;

import java.util.UUID;

public record LibroMasPrestadoResponse(
        UUID libroId,
        String titulo,
        String isbn,
        long totalPrestamos
) {
}
