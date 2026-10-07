package com.willard.inventario.controller;

import com.willard.inventario.models.Rol;
import com.willard.inventario.service.RolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/roles")
public class RolWebController {

    private final RolService rolService;

    public RolWebController(RolService rolService) {
        this.rolService = rolService;
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
    public String registrar(@ModelAttribute Rol rol) {
        rolService.registrar(rol);
        return "redirect:/roles";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("rol", rolService.obtenerPorId(id));
        return "roles/form";
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Rol rol) {
        rolService.modificar(id, rol);
        return "redirect:/roles";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id) {
        rolService.cambiarEstado(id);
        return "redirect:/roles";
    }
}