package com.scrip.msprestamos.repository;

import com.scrip.msprestamos.dto.UsuarioMasSancionadoResponse;
import com.scrip.msprestamos.entity.Sancion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SancionRepository extends JpaRepository<Sancion, UUID> {

    @Query("""
            select new com.scrip.msprestamos.dto.UsuarioMasSancionadoResponse(
                s.usuarioId,
                count(s)
            )
            from Sancion s
            group by s.usuarioId
            order by count(s) desc
            """)
    List<UsuarioMasSancionadoResponse> findUsuariosMasSancionados(Pageable pageable);
}
