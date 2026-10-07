package com.willard.inventario.service;

import com.willard.inventario.aop.UsuarioActualProvider;
import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.UsuarioRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de UsuarioService: los repositorios/colaboradores son mocks, no se necesita MySQL.
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolService rolService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UsuarioActualProvider usuarioActualProvider;

    @InjectMocks
    private UsuarioService usuarioService;

    private static Rol rol(long id, String nombre, boolean activo) {
        Rol r = new Rol();
        r.setId(id);
        r.setNombre(nombre);
        r.setEstado(activo);
        return r;
    }

    private static Usuario usuarioExistente(long id, String username, String passwordHash, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Usuario " + id);
        u.setUsername(username);
        u.setPassword(passwordHash);
        u.setRol(rol);
        u.setEstado(true);
        return u;
    }

    @Nested
    class Modificar {

        @Test
        void editarSinPasswordConservaElHashOriginal() {
            Rol rolActual = rol(1, "MEDICO", true);
            Usuario existente = usuarioExistente(5, "jperez", "$2a$10$hashOriginalGuardado", rolActual);
            when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario datosFormulario = new Usuario();
            datosFormulario.setNombre("Juan Pérez");
            datosFormulario.setUsername("jperez");
            datosFormulario.setPassword(""); // el formulario de edición lo deja vacío a propósito

            Usuario actualizado = usuarioService.modificar(5L, datosFormulario);

            assertThat(actualizado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(actualizado.getPassword()).isEqualTo("$2a$10$hashOriginalGuardado");
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void editarConPasswordNuevaLaEncripta() {
            Rol rolActual = rol(1, "MEDICO", true);
            Usuario existente = usuarioExistente(5, "jperez", "$2a$10$hashOriginalGuardado", rolActual);
            when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
            when(passwordEncoder.encode("nuevaClave123")).thenReturn("$2a$10$hashNuevo");

            Usuario datosFormulario = new Usuario();
            datosFormulario.setNombre("Juan Pérez");
            datosFormulario.setUsername("jperez");
            datosFormulario.setPassword("nuevaClave123");

            Usuario actualizado = usuarioService.modificar(5L, datosFormulario);

            assertThat(actualizado.getPassword()).isEqualTo("$2a$10$hashNuevo");
        }
    }

    @Nested
    class CambiarEstado {

        @Test
        void noPermiteDesactivarseASiMismo() {
            Rol rolActual = rol(1, "ADMINISTRADOR", true);
            Usuario yoMismo = usuarioExistente(1, "admin", "hash", rolActual);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(yoMismo));
            when(usuarioActualProvider.obtenerUsuario()).thenReturn("admin");

            assertThatThrownBy(() -> usuarioService.cambiarEstado(1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("propio usuario");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        void noPermiteDesactivarAlUltimoAdministradorActivo() {
            Rol rolAdmin = rol(1, "ADMINISTRADOR", true);
            Usuario otroAdmin = usuarioExistente(2, "otroadmin", "hash", rolAdmin);
            when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otroAdmin));
            when(usuarioActualProvider.obtenerUsuario()).thenReturn("admin"); // quien ejecuta la accion
            when(usuarioRepository.countByRol_NombreAndEstado("ADMINISTRADOR", true)).thenReturn(1L);

            assertThatThrownBy(() -> usuarioService.cambiarEstado(2L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("último administrador");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        void permiteDesactivarAUnAdministradorSiHayOtrosActivos() {
            Rol rolAdmin = rol(1, "ADMINISTRADOR", true);
            Usuario otroAdmin = usuarioExistente(2, "otroadmin", "hash", rolAdmin);
            when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otroAdmin));
            when(usuarioActualProvider.obtenerUsuario()).thenReturn("admin");
            when(usuarioRepository.countByRol_NombreAndEstado("ADMINISTRADOR", true)).thenReturn(2L);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.cambiarEstado(2L);

            assertThat(resultado.getEstado()).isFalse();
        }

        @Test
        void permiteDesactivarAUnUsuarioQueNoEsAdministrador() {
            Rol rolMedico = rol(2, "MEDICO", true);
            Usuario medico = usuarioExistente(3, "medico", "hash", rolMedico);
            when(usuarioRepository.findById(3L)).thenReturn(Optional.of(medico));
            when(usuarioActualProvider.obtenerUsuario()).thenReturn("admin");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = usuarioService.cambiarEstado(3L);

            assertThat(resultado.getEstado()).isFalse();
            verify(usuarioRepository, never()).countByRol_NombreAndEstado(any(), any());
        }
    }
}
