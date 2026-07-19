package com.scrip.msnotificaciones.service;

import com.scrip.msnotificaciones.entity.EstadoEntrega;
import com.scrip.msnotificaciones.entity.Notificacion;
import com.scrip.msnotificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntregaCorreoService {
    private final NotificacionRepository repository;
    private final IdentityClient identityClient;
    private final SmtpEmailClient smtpEmailClient;
    @Value("${notifications.max-attempts}") private int maxAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void entregar(UUID id) {
        Notificacion notificacion = repository.findById(id).orElse(null);
        if (notificacion == null || notificacion.getEstadoEntrega() == EstadoEntrega.ENVIADA || notificacion.getEstadoEntrega() == EstadoEntrega.OMITIDA) return;
        try {
            IdentityClient.UserStatus usuario = identityClient.obtenerUsuario(notificacion.getUsuarioId());
            if (usuario == null || !usuario.activo()) {
                notificacion.setEstadoEntrega(EstadoEntrega.OMITIDA);
                notificacion.setUltimoError("Usuario inactivo o inexistente");
                return;
            }
            String destino = usuario.email();
            if (destino == null || destino.isBlank()) {
                throw new IllegalStateException("El usuario no tiene un correo electrónico configurado");
            }
            String proveedorId = smtpEmailClient.enviar(destino, asunto(notificacion), plantilla(notificacion));
            notificacion.setIntentos(notificacion.getIntentos() + 1);
            notificacion.setCorreoDestino(destino);
            notificacion.setProveedorId(proveedorId);
            notificacion.setEstadoEntrega(EstadoEntrega.ENVIADA);
            notificacion.setFechaEntrega(OffsetDateTime.now());
            notificacion.setUltimoError(null);
        } catch (Exception exception) {
            notificacion.setIntentos(notificacion.getIntentos() + 1);
            notificacion.setEstadoEntrega(EstadoEntrega.ERROR);
            notificacion.setUltimoError(exception.getMessage());
            log.warn("No se pudo entregar la notificación {} (intento {}): {}", id, notificacion.getIntentos(), exception.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${notifications.retry-delay-ms}")
    public void reintentarPendientes() {
        repository.findByEstadoEntregaInAndIntentosLessThan(List.of(EstadoEntrega.PENDIENTE, EstadoEntrega.ERROR), maxAttempts)
                .forEach(notificacion -> entregar(notificacion.getId()));
    }

    private String asunto(Notificacion n) { return "Biblioteca UTEQ - " + n.getTipo().name().replace('_', ' '); }
    private String plantilla(Notificacion n) { return "<h2>Biblioteca UTEQ</h2><p>" + escapar(n.getMensaje()) + "</p>"; }
    private String escapar(String valor) { return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
}
