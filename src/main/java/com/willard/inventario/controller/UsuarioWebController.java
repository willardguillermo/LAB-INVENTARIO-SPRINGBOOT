package com.willard.inventario.controller;

import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.service.RolService;
import com.willard.inventario.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    public UsuarioWebController(UsuarioService usuarioService, RolService rolService) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolService.listar());
        return "usuarios/form";
    }

    @PostMapping
    public String registrar(@ModelAttribute Usuario usuario, @RequestParam Long rolId) {
        Rol rol = new Rol();
        rol.setId(rolId);
        usuario.setRol(rol);
        usuarioService.registrar(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.obtenerPorId(id));
        model.addAttribute("roles", rolService.listar());
        return "usuarios/form";
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Usuario usuario, @RequestParam Long rolId) {
        Rol rol = new Rol();
        rol.setId(rolId);
        usuario.setRol(rol);
        usuarioService.modificar(id, usuario);
        return "redirect:/usuarios";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id) {
        usuarioService.cambiarEstado(id);
        return "redirect:/usuarios";
    }
}