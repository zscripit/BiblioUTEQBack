package com.scrip.mscatalogo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "libros", schema = "catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(length = 255)
    private String autor;

    @Column(length = 100)
    private String categoria;

    @Column(name = "portada_url", length = 1000)
    private String portadaUrl;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 0;

    @Builder.Default
    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    @Builder.Default
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT now()")
    private OffsetDateTime fechaActualizacion = OffsetDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = OffsetDateTime.now();
    }
}
