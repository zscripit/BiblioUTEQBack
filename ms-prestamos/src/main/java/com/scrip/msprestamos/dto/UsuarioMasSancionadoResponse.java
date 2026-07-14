package com.scrip.msprestamos.dto;

import java.util.UUID;

public record UsuarioMasSancionadoResponse(
        UUID usuarioId,
        long totalSanciones
) {
}
