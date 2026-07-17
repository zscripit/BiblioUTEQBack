package com.scrip.msuserauthregister.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Configuration
public class AuthorizationServerConfig {

    // 1. Aquí solucionamos el error: Registramos un cliente en memoria para pruebas
    @Bean
    public RegisteredClientRepository registeredClientRepository(BCryptPasswordEncoder passwordEncoder) {
        RegisteredClient postmanClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("postman-client")
                // Registramos la contraseña secreta cifrada con BCrypt (la usaremos en Postman)
                .clientSecret(passwordEncoder.encode("PostmanSecret2024!"))
                // Método de autenticación requerido por el estándar
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                // Flujos permitidos de OAuth 2.1 (Intercambio de código y actualización)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                // Redirección obligatoria para capturar el código (Postman maneja esta por defecto)
                .redirectUri("https://oauth.pstmn.io/v1/callback")
                // Permisos o alcances del token
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read")
                .scope("write")
                // Exige PKCE de forma obligatoria (Requisito estricto de OAuth 2.1)
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();

        // Angular es un cliente publico: el navegador no puede guardar secretos.
        RegisteredClient angularClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("bibliouteq-spa")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4200/auth/callback")
                .postLogoutRedirectUri("http://localhost:4200/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(postmanClient, angularClient);
    }

    // 2. Definimos los ajustes del servidor (rutas por defecto)
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            if (context.getPrincipal() == null) {
                return;
            }

            Set<String> roles = context.getPrincipal().getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                    .filter(role -> !role.startsWith("SCOPE_"))
                    .collect(Collectors.toSet());

            Object principal = context.getPrincipal().getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                context.getClaims().claim("roles", roles);
                context.getClaims().claim("user_id", userDetails.getId().toString());
                context.getClaims().claim("email", userDetails.getUsername());
                context.getClaims().claim("name", userDetails.getFullName());
            } else if (context.getAuthorization() != null
                    && context.getAuthorization().getAccessToken() != null) {
                // En refresh_token el principal es el cliente. Conservamos los claims
                // de identidad emitidos en el access token original.
                Map<String, Object> previousClaims = context.getAuthorization().getAccessToken().getClaims();
                if (previousClaims != null) {
                    previousClaims.entrySet().stream()
                            .filter(entry -> Set.of("roles", "user_id", "email", "name").contains(entry.getKey()))
                            .filter(entry -> entry.getValue() != null)
                            .forEach(entry -> context.getClaims().claim(entry.getKey(), entry.getValue()));
                }
            }
        };
    }
}
