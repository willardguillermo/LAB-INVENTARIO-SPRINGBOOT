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
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
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
                        // /api/**: 401/403 en JSON para que el fetch del frontend pueda mostrar un mensaje.
                        // El resto (plantillas Thymeleaf) sigue el flujo normal: redirige a /login.
                        // Nota: no se usa defaultAuthenticationEntryPointFor/defaultAccessDeniedHandlerFor
                        // porque Spring usa el primer mapeo registrado como "default" cuando ninguno
                        // coincide, y eso pisaba el redirect a /login de formLogin() con la respuesta JSON.
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
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

    private boolean esApi(jakarta.servlet.http.HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        AuthenticationEntryPoint redirigirALogin = new LoginUrlAuthenticationEntryPoint("/login");
        return (request, response, authException) -> {
            if (esApi(request)) {
                escribirError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para acceder a este recurso");
            } else {
                redirigirALogin.commence(request, response, authException);
            }
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandler porDefecto = new AccessDeniedHandlerImpl();
        return (request, response, accessDeniedException) -> {
            if (esApi(request)) {
                escribirError(response, HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta operación");
            } else {
                porDefecto.handle(request, response, accessDeniedException);
            }
        };
    }

    // Cuerpo con el mismo formato que GlobalExceptionHandler (timestamp, status, error, message);
    // se arma a mano porque esta clase se ejecuta antes del dispatcher y no depende de Jackson.
    private void escribirError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status,
                                String mensaje) throws IOException {
        String json = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s"}""".formatted(
                Instant.now(), status.value(), status.getReasonPhrase(), mensaje.replace("\"", "\\\""));

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(json);
    }
}
