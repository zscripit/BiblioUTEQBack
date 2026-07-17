package com.scrip.mscatalogo.dto;

import java.util.UUID;

public record BookDto(
        UUID id,
        String titulo,
        String isbn,
        String autor,
        String categoria,
        Integer stock,
        Integer stockReservado,
        Boolean activo,
        String portadaUrl
) {
}
