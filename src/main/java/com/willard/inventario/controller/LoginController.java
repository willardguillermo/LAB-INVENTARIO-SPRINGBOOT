package com.willard.inventario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Sirve el panel estático (src/main/resources/static/index.html); no hay plantilla "index".
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}