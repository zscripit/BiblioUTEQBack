package com.scrip.msprestamos.service;

import com.scrip.msprestamos.dto.LibroMasPrestadoResponse;
import com.scrip.msprestamos.dto.PrestamosPeriodoResponse;
import com.scrip.msprestamos.dto.UsuarioMasSancionadoResponse;
import com.scrip.msprestamos.repository.PrestamoRepository;
import com.scrip.msprestamos.repository.SancionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private static final int LIMITE_DEFAULT = 10;
    private static final int LIMITE_MAXIMO = 100;

    private final PrestamoRepository prestamoRepository;
    private final SancionRepository sancionRepository;

    public PrestamosPeriodoResponse contarPrestamosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        validarRangoFechas(fechaInicio, fechaFin);

        ZoneId zonaHoraria = ZoneId.systemDefault();
        OffsetDateTime inicio = fechaInicio.atStartOfDay(zonaHoraria).toOffsetDateTime();
        OffsetDateTime finExclusiva = fechaFin.plusDays(1).atStartOfDay(zonaHoraria).toOffsetDateTime();

        long total = prestamoRepository
                .countByFechaPrestamoGreaterThanEqualAndFechaPrestamoLessThan(inicio, finExclusiva);

        return new PrestamosPeriodoResponse(fechaInicio, fechaFin, total);
    }

    public List<LibroMasPrestadoResponse> obtenerLibrosMasPrestados(Integer limite) {
        return prestamoRepository.findLibrosMasPrestados(PageRequest.of(0, normalizarLimite(limite)));
    }

    public List<UsuarioMasSancionadoResponse> obtenerUsuariosMasSancionados(Integer limite) {
        return sancionRepository.findUsuariosMasSancionados(PageRequest.of(0, normalizarLimite(limite)));
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
