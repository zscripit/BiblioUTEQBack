package com.scrip.msuserauthregister.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(min = 8, max = 100) String password
) {}
