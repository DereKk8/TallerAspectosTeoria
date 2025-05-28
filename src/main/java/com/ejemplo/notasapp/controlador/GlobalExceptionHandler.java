package com.ejemplo.notasapp.controlador;

import com.ejemplo.notasapp.excepcion.EstudianteNoEncontrado;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EstudianteNoEncontrado.class)
    public RedirectView handleEstudianteNoEncontrado(EstudianteNoEncontrado ex) {
        String redirectUrl = "/error/estudiante-no-encontrado?mensaje=" +
                java.net.URLEncoder.encode(ex.getMensaje(), java.nio.charset.StandardCharsets.UTF_8) +
                "&codigo=" + ex.getCodigoError();

        return new RedirectView(redirectUrl);
    }
}