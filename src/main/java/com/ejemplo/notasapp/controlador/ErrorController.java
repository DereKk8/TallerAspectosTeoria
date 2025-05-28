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

    @GetMapping("/violacion-seguridad")
    public String violacionSeguridad(
            @RequestParam(value = "tipoViolacion", defaultValue = "ACCESO_DENEGADO") String tipoViolacion,
            @RequestParam(value = "usuario", defaultValue = "Usuario") String usuario,
            @RequestParam(value = "operacion", defaultValue = "Operación") String operacion,
            @RequestParam(value = "detalles", defaultValue = "Acceso denegado por falta de permisos") String detalles,
            @RequestParam(value = "codigoError", defaultValue = "403") String codigoError,
            Model model) {

        model.addAttribute("tipoViolacion", tipoViolacion);
        model.addAttribute("usuario", usuario);
        model.addAttribute("operacion", operacion);
        model.addAttribute("detalles", detalles);
        model.addAttribute("codigoError", codigoError);

        return "error_violacion_seguridad";
    }
}