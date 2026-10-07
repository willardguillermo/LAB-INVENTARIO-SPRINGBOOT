package com.willard.inventario.controller;

import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.service.RolService;
import com.willard.inventario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
public class RolWebController {

    private final RolService rolService;
    private final UsuarioService usuarioService;

    public RolWebController(RolService rolService, UsuarioService usuarioService) {
        this.rolService = rolService;
        this.usuarioService = usuarioService;
    }

    // Usuario de la sesión activa, para la barra superior de las plantillas.
    @ModelAttribute("sesionUsuario")
    public Usuario sesionUsuario() {
        return usuarioService.obtenerUsuarioAutenticado();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listar());
        return "roles/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("rol", new Rol());
        return "roles/form";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("rol") Rol rol, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "roles/form";
        }
        try {
            rolService.registrar(rol);
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            return "roles/form";
        }
        return "redirect:/roles";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("rol", rolService.obtenerPorId(id));
        return "roles/form";
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("rol") Rol rol,
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            rol.setId(id);
            return "roles/form";
        }
        try {
            rolService.modificar(id, rol);
        } catch (ResponseStatusException ex) {
            rol.setId(id);
            model.addAttribute("error", ex.getReason());
            return "roles/form";
        }
        return "redirect:/roles";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rolService.cambiarEstado(id);
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
        }
        return "redirect:/roles";
    }
}
