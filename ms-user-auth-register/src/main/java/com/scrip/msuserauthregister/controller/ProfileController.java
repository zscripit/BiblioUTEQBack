package com.scrip.msuserauthregister.controller;

import com.scrip.msuserauthregister.dto.ProfileUpdateRequest;
import com.scrip.msuserauthregister.dto.UserResponse;
import com.scrip.msuserauthregister.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;

    @GetMapping
    public UserResponse getProfile(JwtAuthenticationToken authentication) {
        return userService.findProfile(userId(authentication));
    }

    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody ProfileUpdateRequest request,
                                      JwtAuthenticationToken authentication) {
        return userService.updateProfile(userId(authentication), request);
    }

    private UUID userId(JwtAuthenticationToken authentication) {
        String claim = authentication.getToken().getClaimAsString("user_id");
        if (claim == null) throw new IllegalArgumentException("El token no contiene el identificador del usuario");
        return UUID.fromString(claim);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
