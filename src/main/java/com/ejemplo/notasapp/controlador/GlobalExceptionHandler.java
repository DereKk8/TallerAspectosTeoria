package com.ejemplo.notasapp.controlador;

import com.ejemplo.notasapp.excepcion.EstudianteNoEncontrado;
import com.ejemplo.notasapp.excepcion.ViolacionSeguridad;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EstudianteNoEncontrado.class)
    public RedirectView handleEstudianteNoEncontrado(EstudianteNoEncontrado ex) {
        String redirectUrl = "/error/estudiante-no-encontrado?mensaje=" +
                URLEncoder.encode(ex.getMensaje(), StandardCharsets.UTF_8) +
                "&codigo=" + ex.getCodigoError();

        return new RedirectView(redirectUrl);
    }

    @ExceptionHandler(ViolacionSeguridad.class)
    public RedirectView handleViolacionSeguridad(ViolacionSeguridad ex) {
        String redirectUrl = "/error/violacion-seguridad?" +
                "tipoViolacion=" + URLEncoder.encode(ex.getTipoViolacion(), StandardCharsets.UTF_8) +
                "&usuario=" + URLEncoder.encode(ex.getUsuario(), StandardCharsets.UTF_8) +
                "&operacion=" + URLEncoder.encode(ex.getOperacion(), StandardCharsets.UTF_8) +
                "&detalles=" + URLEncoder.encode(ex.getDetalles(), StandardCharsets.UTF_8) +
                "&codigoError=" + ex.getCodigoError();

        return new RedirectView(redirectUrl);
    }

    @ExceptionHandler(SecurityException.class)
    public RedirectView handleSecurityException(SecurityException ex) {
        // Convertir SecurityException genérica a ViolacionSeguridad para manejo
        // uniforme
        String redirectUrl = "/error/violacion-seguridad?" +
                "tipoViolacion=" + URLEncoder.encode("ACCESO_DENEGADO", StandardCharsets.UTF_8) +
                "&usuario=" + URLEncoder.encode("Usuario", StandardCharsets.UTF_8) +
                "&operacion=" + URLEncoder.encode("Operación restringida", StandardCharsets.UTF_8) +
                "&detalles="
                + URLEncoder.encode(ex.getMessage() != null ? ex.getMessage() : "Acceso denegado",
                        StandardCharsets.UTF_8)
                +
                "&codigoError=403";

        return new RedirectView(redirectUrl);
    }
}