package com.scrip.msuserauthregister.repository;

import com.scrip.msuserauthregister.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Este método nos servirá más adelante para validar que el correo sea único en el registro
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndActivoTrue(String email);
    List<User> findAllByOrderByNombreCompletoAsc();
}
