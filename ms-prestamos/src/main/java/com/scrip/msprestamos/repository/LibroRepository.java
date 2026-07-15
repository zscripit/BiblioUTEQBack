package com.scrip.msprestamos.repository;

import com.scrip.msprestamos.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LibroRepository extends JpaRepository<Libro, UUID> {
}
