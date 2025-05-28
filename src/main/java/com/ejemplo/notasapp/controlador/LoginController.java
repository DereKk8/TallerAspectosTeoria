package com.ejemplo.notasapp.controlador;

import com.ejemplo.notasapp.excepcion.EstudianteNoEncontrado;
import com.ejemplo.notasapp.modelo.UsuarioSesion;
import com.ejemplo.notasapp.repositorio.RepositorioLogViolacionSeguridad;
import com.ejemplo.notasapp.repositorio.RepositorioEstudiante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private RepositorioLogViolacionSeguridad repositorioLog;

    @Autowired
    private RepositorioEstudiante repositorioEstudiante;

    @GetMapping("/")
    public String index() {
        return "identificacion";
    }

    @PostMapping("/identificacion")
    public String procesarIdentificacion(
            @RequestParam("nombre") String nombre,
            @RequestParam("rol") String rol,
            @RequestParam(value = "estudianteId", required = false) Long estudianteId,
            HttpSession session) {

        try {
            // Validar entrada básica
            if (nombre == null || nombre.trim().isEmpty()) {
                return "redirect:/?error=nombre_requerido";
            }

            if (!"ALUMNO".equals(rol) && !"PROFESOR".equals(rol)) {
                return "redirect:/?error=rol_invalido";
            }

            // Validar que los alumnos proporcionen su ID de estudiante
            if ("ALUMNO".equals(rol)) {
                if (estudianteId == null || estudianteId <= 0) {
                    return "redirect:/?error=estudiante_id_requerido";
                }

                // Verificar que el ID de estudiante existe en la base de datos
                if (!repositorioEstudiante.existsById(estudianteId)) {
                    throw new EstudianteNoEncontrado("estudiante no encontrado", "404", new RuntimeException());
                }
            } else {
                // Los profesores no necesitan estudianteId
                estudianteId = null;
            }

            // Crear usuario de sesión y guardarlo en la sesión HTTP
            UsuarioSesion usuario = new UsuarioSesion(nombre.trim(), rol, estudianteId);
            session.setAttribute("usuario", usuario);

            // Log de inicio de sesión
            System.out.println("=== INICIO DE SESIÓN ===");
            System.out.println("Usuario: " + usuario.toString());
            System.out.println("========================");

            // Redirigir según el rol
            if (usuario.isProfesor()) {
                // Los profesores van a la lista de estudiantes (acceso completo)
                return "redirect:/estudiantes";
            } else if (usuario.isAlumno()) {
                // Los alumnos van directamente a sus propias notas
                return "redirect:/notas/" + estudianteId;
            }

        } catch (Exception e) {
            System.err.println("Error en el procesamiento de identificación: " + e.getMessage());
            return "redirect:/?error=error_procesamiento";
        }

        // En caso de error, volver a la pantalla de identificación
        return "redirect:/?error=error_desconocido";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Obtener usuario antes de invalidar la sesión para logging
        UsuarioSesion usuario = (UsuarioSesion) session.getAttribute("usuario");
        if (usuario != null) {
            System.out.println("=== CIERRE DE SESIÓN ===");
            System.out.println("Usuario: " + usuario.toString());
            System.out.println("========================");
        }

        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession session) {
        UsuarioSesion usuario = (UsuarioSesion) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/";
        }

        // Aquí podrías implementar una vista de perfil
        // Por ahora, redirigir según el rol
        if (usuario.isProfesor()) {
            return "redirect:/estudiantes";
        } else {
            return "redirect:/notas/" + usuario.getEstudianteId();
        }
    }

    // Endpoint para debug: ver logs de violaciones de seguridad
    @GetMapping("/debug/logs")
    @ResponseBody
    public String verLogsViolaciones() {
        try {
            long totalLogs = repositorioLog.count();
            StringBuilder sb = new StringBuilder();
            sb.append("=== LOGS DE VIOLACIONES DE SEGURIDAD ===\n");
            sb.append("Total de registros: ").append(totalLogs).append("\n\n");

            repositorioLog.findAll().forEach(log -> {
                sb.append("ID: ").append(log.getId()).append("\n");
                sb.append("Usuario: ").append(log.getNombreUsuario()).append("\n");
                sb.append("Rol: ").append(log.getRol()).append("\n");
                sb.append("Acción: ").append(log.getAccionIntentada()).append("\n");
                sb.append("Detalles: ").append(log.getDetalles()).append("\n");
                sb.append("Fecha/Hora: ").append(log.getFechaHora()).append("\n");
                sb.append("----------------------------------------\n");
            });

            return sb.toString();
        } catch (Exception e) {
            return "Error al consultar logs: " + e.getMessage();
        }
    }
}
