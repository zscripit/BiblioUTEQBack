package com.scrip.msuserauthregister.service;

import com.scrip.msuserauthregister.domain.User;
import com.scrip.msuserauthregister.dto.RegisterRequest;
import com.scrip.msuserauthregister.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scrip.msuserauthregister.dto.AdminUserRequest;
import com.scrip.msuserauthregister.dto.UserResponse;
import com.scrip.msuserauthregister.dto.UserStatusResponse;
import com.scrip.msuserauthregister.dto.ProfileUpdateRequest;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void registerUser(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String name = normalizeName(request.getNombreCompleto());
        // 1. Validar unicidad del email
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // 2. Construir la entidad cifrando la contraseña
        ensureUniqueName(name, null);
        User user = User.builder()
                .nombreCompleto(name)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // Aquí se aplica BCrypt
                .rol(request.getRol())
                .build();

        // 3. Guardar en Postgres
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByNombreCompletoAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return toResponse(requireActive(id));
    }

    @Transactional(readOnly = true)
    public UserStatusResponse findStatusById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return new UserStatusResponse(user.getEmail(), user.getRol(), user.isActivo());
    }

    @Transactional
    public UserResponse create(AdminUserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria al crear un usuario");
        }
        ensureUniqueEmail(request.email(), null);
        ensureUniqueName(request.nombreCompleto(), null);
        User user = User.builder()
                .nombreCompleto(normalizeName(request.nombreCompleto()))
                .email(normalizeEmail(request.email()))
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activo(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, AdminUserRequest request) {
        User user = requireActive(id);
        ensureUniqueEmail(request.email(), id);
        ensureUniqueName(request.nombreCompleto(), id);
        user.setNombreCompleto(normalizeName(request.nombreCompleto()));
        user.setEmail(normalizeEmail(request.email()));
        user.setRol(request.rol());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(UUID id, boolean active, String authenticatedEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!active && user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
        }
        user.setActivo(active);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findProfile(UUID id) {
        return toResponse(requireActive(id));
    }

    @Transactional
    public UserResponse updateProfile(UUID id, ProfileUpdateRequest request) {
        User user = requireActive(id);
        String email = normalizeEmail(request.email());
        String name = normalizeName(request.nombreCompleto());
        ensureUniqueEmail(email, id);
        ensureUniqueName(name, id);
        user.setNombreCompleto(name);
        user.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    private User requireActive(UUID id) {
        return userRepository.findById(id)
                .filter(User::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void ensureUniqueEmail(String email, UUID currentId) {
        userRepository.findByEmail(normalizeEmail(email)).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new IllegalArgumentException("El correo electronico ya esta registrado");
            }
        });
    }

    private void ensureUniqueName(String name, UUID currentId) {
        userRepository.findByNombreCompletoIgnoreCase(normalizeName(name)).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new IllegalArgumentException("El nombre completo ya esta registrado");
            }
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getNombreCompleto(), user.getEmail(),
                user.getRol(), user.getFechaRegistro(), user.isActivo());
    }
}
