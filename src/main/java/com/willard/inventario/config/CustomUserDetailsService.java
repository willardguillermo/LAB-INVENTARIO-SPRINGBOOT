package com.willard.inventario.config;

import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String nombreRol = "ROLE_" + usuario.getRol().getNombre().toUpperCase();

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                true, // Indica que el usuario está activo
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(nombreRol))
        );
    }
}