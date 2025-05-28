package com.ejemplo.notasapp.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index() {
        return "identificacion";
    }

    @PostMapping("/identificacion")
    public String procesarIdentificacion(
            @RequestParam("nombre") String nombre,
            @RequestParam("rol") String rol,
            Model model) {

        // Agregar información al modelo si es necesario
        model.addAttribute("usuario", nombre);
        model.addAttribute("rol", rol);

        // Redirigir según el rol o a una página principal
        if ("PROFESOR".equals(rol)) {
            return "redirect:/estudiantes";
        } else if ("ALUMNO".equals(rol)) {
            return "redirect:/estudiantes";
        }

        // En caso de error, volver a la pantalla de identificación
        return "redirect:/";
    }
}