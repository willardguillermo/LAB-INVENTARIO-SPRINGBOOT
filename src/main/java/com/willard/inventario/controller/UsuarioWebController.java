package com.willard.inventario.controller;

import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.service.RolService;
import com.willard.inventario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    public UsuarioWebController(UsuarioService usuarioService, RolService rolService) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }

    // Usuario de la sesión activa, para la barra superior de las plantillas.
    @ModelAttribute("sesionUsuario")
    public Usuario sesionUsuario() {
        return usuarioService.obtenerUsuarioAutenticado();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolService.listarActivos());
        model.addAttribute("rolInactivoActual", null);
        return "usuarios/form";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult bindingResult,
                             @RequestParam(required = false) Long rolId, Model model) {
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "password.required", "La contraseña es obligatoria");
        }
        if (rolId == null) {
            bindingResult.reject("rolId.required", "Debe seleccionar un rol");
        } else {
            Rol rol = new Rol();
            rol.setId(rolId);
            usuario.setRol(rol);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", rolService.listarActivos());
            model.addAttribute("rolInactivoActual", null);
            return "usuarios/form";
        }

        try {
            usuarioService.registrar(usuario);
        } catch (ResponseStatusException ex) {
            model.addAttribute("roles", rolService.listarActivos());
            model.addAttribute("rolInactivoActual", null);
            model.addAttribute("error", ex.getReason());
            return "usuarios/form";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        model.addAttribute("usuario", usuario);
        cargarRolesParaFormulario(usuario, model);
        return "usuarios/form";
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                              @Valid @ModelAttribute("usuario") Usuario usuario, BindingResult bindingResult,
                              @RequestParam(required = false) Long rolId, Model model) {
        if (rolId == null) {
            bindingResult.reject("rolId.required", "Debe seleccionar un rol");
        } else {
            Rol rol = new Rol();
            rol.setId(rolId);
            usuario.setRol(rol);
        }

        if (bindingResult.hasErrors()) {
            usuario.setId(id);
            cargarRolesParaFormulario(usuario, model);
            return "usuarios/form";
        }

        try {
            usuarioService.modificar(id, usuario);
        } catch (ResponseStatusException ex) {
            usuario.setId(id);
            cargarRolesParaFormulario(usuario, model);
            model.addAttribute("error", ex.getReason());
            return "usuarios/form";
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarEstado(id);
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
        }
        return "redirect:/usuarios";
    }

    // Roles activos + el rol actual del usuario aunque esté inactivo (se marca "(inactivo)" en la vista).
    // Se vuelve a leer por id porque, al volver a mostrar el formulario tras un error, el usuario
    // solo trae el id del rol elegido (no el resto de sus datos).
    private void cargarRolesParaFormulario(Usuario usuario, Model model) {
        model.addAttribute("roles", rolService.listarActivos());
        Long rolId = usuario.getRol() != null ? usuario.getRol().getId() : null;
        if (rolId == null) {
            model.addAttribute("rolInactivoActual", null);
            return;
        }
        Rol rolCompleto = rolService.obtenerPorId(rolId);
        model.addAttribute("rolInactivoActual", Boolean.TRUE.equals(rolCompleto.getEstado()) ? null : rolCompleto);
    }
}
