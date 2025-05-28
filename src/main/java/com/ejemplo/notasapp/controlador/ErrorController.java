package com.ejemplo.notasapp.controlador;

import com.ejemplo.notasapp.excepcion.EstudianteNoEncontrado;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/estudiante-no-encontrado")
    public String estudianteNoEncontrado(
            @RequestParam(value = "mensaje", defaultValue = "Estudiante no encontrado") String mensaje,
            @RequestParam(value = "codigo", defaultValue = "404") String codigo,
            Model model) {

        model.addAttribute("error", mensaje);
        model.addAttribute("codigoError", codigo);

        return "error_estudiante_no_encontrado";
    }
}