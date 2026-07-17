package com.scrip.msuserauthregister.config;

import com.scrip.msuserauthregister.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapeamos tu ENUM de roles al formato que entiende Spring ("ROLE_ESTUDIANTE")
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // El hash de BCrypt
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Usaremos el email como identificador de login
    }

    public UUID getId() {
        return user.getId();
    }

    public String getFullName() {
        return user.getNombreCompleto();
    }

    @Override
    public boolean isEnabled() {
        return user.isActivo();
    }
}
