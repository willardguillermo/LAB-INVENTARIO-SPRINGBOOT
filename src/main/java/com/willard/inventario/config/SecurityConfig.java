package com.willard.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import java.io.IOException;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean obligatorio para que Thymeleaf reconozca las etiquetas sec:authorize
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                        // Solo ADMINISTRADOR gestiona usuarios, roles y consulta la auditoría
                        .requestMatchers("/usuarios/**", "/roles/**",
                                "/api/usuarios/**", "/api/roles/**", "/api/auditoria/**")
                        .hasRole("ADMINISTRADOR")
                        // Categorías, unidades de medida y proveedores: lectura ADMINISTRADOR y MEDICO,
                        // escritura solo ADMINISTRADOR
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**", "/api/unidades-medida/**",
                                "/api/proveedores/**")
                        .hasAnyRole("ADMINISTRADOR", "MEDICO")
                        .requestMatchers("/api/categorias/**", "/api/unidades-medida/**", "/api/proveedores/**")
                        .hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(handling -> handling
                        // /api/**: 401/403 en JSON para que el fetch del frontend pueda mostrar un mensaje
                        // en vez de recibir el HTML de la página de login o del error 403.
                        .defaultAuthenticationEntryPointFor(jsonEntryPoint(), apiRequestMatcher())
                        .defaultAccessDeniedHandlerFor(jsonAccessDeniedHandler(), apiRequestMatcher())
                );

        return http.build();
    }

    // Usuario/rol inactivo -> mensaje distinto al de credenciales incorrectas (login.html lo lee en ?error=)
    private AuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler credenciales = new SimpleUrlAuthenticationFailureHandler("/login?error=credenciales");
        SimpleUrlAuthenticationFailureHandler deshabilitado = new SimpleUrlAuthenticationFailureHandler("/login?error=deshabilitado");
        return (request, response, exception) -> {
            if (exception instanceof DisabledException) {
                deshabilitado.onAuthenticationFailure(request, response, exception);
            } else {
                credenciales.onAuthenticationFailure(request, response, exception);
            }
        };
    }

    private org.springframework.security.web.util.matcher.RequestMatcher apiRequestMatcher() {
        return request -> request.getRequestURI().startsWith("/api/");
    }

    private AuthenticationEntryPoint jsonEntryPoint() {
        return (request, response, authException) ->
                escribirError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para acceder a este recurso");
    }

    private AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                escribirError(response, HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta operación");
    }

    // Cuerpo con el mismo formato que GlobalExceptionHandler (timestamp, status, error, message);
    // se arma a mano porque esta clase se ejecuta antes del dispatcher y no depende de Jackson.
    private void escribirError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status,
                                String mensaje) throws IOException {
        String json = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s"}""".formatted(
                Instant.now(), status.value(), status.getReasonPhrase(), mensaje.replace("\"", "\\\""));

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(json);
    }
}
