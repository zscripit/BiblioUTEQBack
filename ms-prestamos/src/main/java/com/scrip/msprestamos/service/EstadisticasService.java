package com.scrip.msprestamos.service;

import com.scrip.msprestamos.dto.LibroMasPrestadoResponse;
import com.scrip.msprestamos.dto.PrestamosPeriodoResponse;
import com.scrip.msprestamos.dto.UsuarioMasSancionadoResponse;
import com.scrip.msprestamos.entity.Libro;
import com.scrip.msprestamos.entity.Prestamo;
import com.scrip.msprestamos.entity.Sancion;
import com.scrip.msprestamos.repository.LibroRepository;
import com.scrip.msprestamos.repository.PrestamoRepository;
import com.scrip.msprestamos.repository.SancionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private static final int LIMITE_DEFAULT = 10;
    private static final int LIMITE_MAXIMO = 100;

    private final PrestamoRepository prestamoRepository;
    private final SancionRepository sancionRepository;
    private final LibroRepository libroRepository;

    public PrestamosPeriodoResponse contarPrestamosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRangoFechas(fechaInicio, fechaFin);

        ZoneId zonaHoraria = ZoneId.systemDefault();
        OffsetDateTime inicio = fechaInicio.atStartOfDay(zonaHoraria).toOffsetDateTime();
        OffsetDateTime finExclusiva = fechaFin.plusDays(1).atStartOfDay(zonaHoraria).toOffsetDateTime();

        Integer total = prestamoRepository
                .findByFechaPrestamoGreaterThanEqualAndFechaPrestamoLessThan(inicio, finExclusiva)
                .size();

        return new PrestamosPeriodoResponse(fechaInicio, fechaFin, total);
    }

    public List<LibroMasPrestadoResponse> obtenerLibrosMasPrestados(Integer limite) {
        int limiteNormalizado = normalizarLimite(limite);

        Map<UUID, Integer> prestamosPorLibro = prestamoRepository.findAll().stream()
                .collect(Collectors.groupingBy(Prestamo::getLibroId, Collectors.summingInt(prestamo -> 1)));

        Map<UUID, Libro> librosPorId = libroRepository.findAllById(prestamosPorLibro.keySet()).stream()
                .collect(Collectors.toMap(Libro::getId, Function.identity()));

        return prestamosPorLibro.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limiteNormalizado)
                .map(entry -> crearLibroMasPrestadoResponse(entry, librosPorId))
                .toList();
    }

    public List<UsuarioMasSancionadoResponse> obtenerUsuariosMasSancionados(Integer limite) {
        int limiteNormalizado = normalizarLimite(limite);

        return sancionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Sancion::getUsuarioId, Collectors.summingInt(sancion -> 1)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limiteNormalizado)
                .map(entry -> new UsuarioMasSancionadoResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private LibroMasPrestadoResponse crearLibroMasPrestadoResponse(
            Map.Entry<UUID, Integer> entry,
            Map<UUID, Libro> librosPorId
    ) {
        Libro libro = librosPorId.get(entry.getKey());

        return new LibroMasPrestadoResponse(
                entry.getKey(),
                libro != null ? libro.getTitulo() : null,
                libro != null ? libro.getIsbn() : null,
                entry.getValue()
        );
    }

    private void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas inicial y final son obligatorias");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser mayor que la fecha final");
        }
    }

    private int normalizarLimite(Integer limite) {
        if (limite == null) {
            return LIMITE_DEFAULT;
        }

        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("El limite debe estar entre 1 y 100");
        }

        return limite;
    }
}
