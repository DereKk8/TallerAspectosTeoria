package com.ejemplo.notasapp.aspecto;

import com.ejemplo.notasapp.modelo.LogViolacionSeguridad;
import com.ejemplo.notasapp.modelo.Nota;
import com.ejemplo.notasapp.modelo.UsuarioSesion;
import com.ejemplo.notasapp.repositorio.RepositorioLogViolacionSeguridad;
import com.ejemplo.notasapp.repositorio.RepositorioNota;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class AspectosAutorizacion {

    @Autowired
    private RepositorioNota repositorioNota;

    @Autowired
    private RepositorioLogViolacionSeguridad repositorioLog;

    // =================== POINTCUTS ===================

    // Pointcuts para operaciones de lectura en NotaController
    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.listar(..))")
    public void operacionesLecturaNotas() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.mostrarFormularioPromedio(..))")
    public void operacionesLecturaPromedio() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.calcularPromedio(..))")
    public void operacionesCalculoPromedio() {
    }

    // Pointcuts para operaciones de escritura en NotaController
    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.nueva(..))")
    public void operacionesEscrituraCrear() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.guardar(..))")
    public void operacionesEscrituraGuardar() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.editar(..))")
    public void operacionesEscrituraEditar() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.NotaController.eliminar(..))")
    public void operacionesEscrituraEliminar() {
    }

    // Pointcuts para operaciones en ServicioNota
    @Pointcut("execution(* com.ejemplo.notasapp.servicio.ServicioNota.*(..))")
    public void operacionesServicioNota() {
    }

    // =================== ASPECTOS PARA ALUMNOS ===================

    /**
     * Aspecto que valida que los alumnos solo puedan realizar operaciones de
     * lectura
     */
    @Before("operacionesEscrituraCrear() || operacionesEscrituraGuardar() || operacionesEscrituraEditar() || operacionesEscrituraEliminar()")
    public void validarOperacionesEscrituraAlumno(JoinPoint joinPoint) {
        UsuarioSesion usuario = obtenerUsuarioSesion();

        if (usuario != null && usuario.isAlumno()) {
            String accionIntentada = joinPoint.getSignature().getName();
            String detalles = String.format("Intento de operación de escritura: %s con parámetros: %s",
                    accionIntentada, Arrays.toString(joinPoint.getArgs()));

            // Registrar violación de seguridad
            registrarViolacionSeguridad(usuario, accionIntentada, detalles);

            throw new SecurityException(
                    String.format(
                            "Acceso denegado: Los alumnos no pueden realizar operaciones de escritura. Operación: %s",
                            accionIntentada));
        }
    }

    /**
     * Aspecto que valida que los alumnos solo puedan acceder a sus propias notas
     */
    @Around("operacionesLecturaNotas() || operacionesLecturaPromedio() || operacionesCalculoPromedio()")
    public Object validarAccesoNotasPropiasAlumno(ProceedingJoinPoint joinPoint) throws Throwable {
        UsuarioSesion usuario = obtenerUsuarioSesion();

        if (usuario != null && usuario.isAlumno()) {
            String metodo = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            // Extraer el estudianteId del primer parámetro (PathVariable)
            if (args.length > 0 && args[0] instanceof Long) {
                Long estudianteIdSolicitado = (Long) args[0];

                // Usar el nuevo método para validar acceso
                if (!usuario.puedeAccederAEstudiante(estudianteIdSolicitado)) {
                    String detalles = String.format(
                            "Intento de acceder a notas de estudiante ID: %d, pero el usuario autenticado corresponde al estudiante ID: %d",
                            estudianteIdSolicitado, usuario.getEstudianteId());

                    registrarViolacionSeguridad(usuario, metodo, detalles);

                    throw new SecurityException("Acceso denegado: Los alumnos solo pueden acceder a sus propias notas");
                }
            }
        }
        // Nota: Los profesores pasan sin restricciones por ahora
        // Aquí se pueden agregar futuras validaciones para profesores

        return joinPoint.proceed();
    }

    /**
     * Aspecto que intercepta llamadas al servicio de notas para validar acceso
     */
    @Around("operacionesServicioNota()")
    public Object validarAccesoServicioNotasAlumno(ProceedingJoinPoint joinPoint) throws Throwable {
        UsuarioSesion usuario = obtenerUsuarioSesion();

        if (usuario != null && usuario.isAlumno()) {
            String metodo = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            // Para métodos que requieren estudianteId como primer parámetro
            if (args.length > 0 && args[0] instanceof Long) {
                Long estudianteIdSolicitado = (Long) args[0];

                // Usar el nuevo método para validar acceso
                if (!usuario.puedeAccederAEstudiante(estudianteIdSolicitado)) {
                    String detalles = String.format("Intento de acceder al servicio %s para estudiante ID: %d",
                            metodo, estudianteIdSolicitado);

                    registrarViolacionSeguridad(usuario, metodo, detalles);

                    throw new SecurityException(
                            "Acceso denegado: Los alumnos solo pueden acceder a servicios de sus propias notas");
                }
            }
        }
        // Nota: Los profesores pasan sin restricciones por ahora
        // Aquí se pueden agregar futuras validaciones para profesores

        return joinPoint.proceed();
    }

    /**
     * Aspecto que valida el acceso a notas específicas por ID
     * Intercepta: /notas/editar/{id} y /notas/eliminar/{id}
     */
    @Around("execution(* com.ejemplo.notasapp.controlador.NotaController.editar(Long, ..)) || " +
            "execution(* com.ejemplo.notasapp.controlador.NotaController.eliminar(Long))")
    public Object validarAccesoNotaPorIdAlumno(ProceedingJoinPoint joinPoint) throws Throwable {
        UsuarioSesion usuario = obtenerUsuarioSesion();

        if (usuario != null && usuario.isAlumno()) {
            String metodo = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            // El primer argumento siempre es el ID de la nota
            if (args.length > 0 && args[0] instanceof Long) {
                Long notaId = (Long) args[0];

                try {
                    // Buscar la nota para verificar a qué estudiante pertenece
                    Optional<Nota> notaOpt = repositorioNota.findById(notaId);

                    if (notaOpt.isPresent()) {
                        Nota nota = notaOpt.get();
                        Long estudiantePropietario = nota.getEstudiante().getId();

                        // Verificar si el alumno está intentando acceder a una nota que no es suya
                        if (!estudiantePropietario.equals(usuario.getEstudianteId())) {
                            String detalles = String.format(
                                    "Intento de %s nota ID: %d que pertenece al estudiante ID: %d, pero el usuario autenticado es estudiante ID: %d",
                                    metodo, notaId, estudiantePropietario, usuario.getEstudianteId());

                            registrarViolacionSeguridad(usuario, metodo, detalles);

                            throw new SecurityException(
                                    String.format("Acceso denegado: Los alumnos solo pueden %s sus propias notas",
                                            metodo));
                        }
                    } else {
                        // La nota no existe
                        String detalles = String.format("Intento de %s nota inexistente ID: %d", metodo, notaId);
                        registrarViolacionSeguridad(usuario, metodo, detalles);

                        throw new SecurityException("Acceso denegado: La nota solicitada no existe");
                    }
                } catch (SecurityException se) {
                    // Re-lanzar excepciones de seguridad
                    throw se;
                } catch (Exception e) {
                    // Error al consultar la base de datos
                    String detalles = String.format("Error al validar acceso para %s nota ID: %d - %s",
                            metodo, notaId, e.getMessage());
                    registrarViolacionSeguridad(usuario, metodo, detalles);

                    System.err.println("Error en validación de acceso: " + e.getMessage());
                    throw new SecurityException("Error al validar permisos de acceso");
                }
            } else {
                // Parámetros incorrectos
                String detalles = String.format("Intento de %s con parámetros inválidos: %s",
                        metodo, Arrays.toString(args));
                registrarViolacionSeguridad(usuario, metodo, detalles);

                throw new SecurityException("Acceso denegado: Parámetros inválidos");
            }
        }

        // Si llegamos aquí, el usuario es profesor o la validación pasó
        return joinPoint.proceed();
    }

    // =================== ASPECTOS PARA PROFESORES ===================
    //
    // NOTA: Esta sección está preparada para implementar futuras validaciones
    // específicas para el rol PROFESOR. Algunos ejemplos podrían incluir:
    //
    // @Before("operacionesEscritura*()")
    // public void validarPermisosProfesor(JoinPoint joinPoint) {
    // UsuarioSesion usuario = obtenerUsuarioSesion();
    // if (usuario != null && usuario.isProfesor()) {
    // // Validaciones específicas para profesores
    // // Ej: horarios permitidos, materias asignadas, etc.
    // }
    // }
    //
    // @Around("operacionesEliminacion*()")
    // public Object validarEliminacionProfesor(ProceedingJoinPoint joinPoint)
    // throws Throwable {
    // UsuarioSesion usuario = obtenerUsuarioSesion();
    // if (usuario != null && usuario.isProfesor()) {
    // // Validar permisos de eliminación
    // // Ej: solo eliminar notas de sus materias asignadas
    // }
    // return joinPoint.proceed();
    // }

    // =================== MÉTODOS AUXILIARES ===================

    /**
     * Obtiene el usuario de la sesión HTTP actual
     */
    private UsuarioSesion obtenerUsuarioSesion() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    return (UsuarioSesion) session.getAttribute("usuario");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario de la sesión: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registra una violación de seguridad en la base de datos
     */
    private void registrarViolacionSeguridad(UsuarioSesion usuario, String accionIntentada, String detalles) {
        try {
            LogViolacionSeguridad log = new LogViolacionSeguridad(
                    usuario.getNombre(),
                    usuario.getRol(),
                    accionIntentada,
                    detalles);

            // Guardar en la base de datos
            LogViolacionSeguridad logGuardado = repositorioLog.save(log);

            // Forzar flush para asegurar persistencia inmediata
            repositorioLog.flush();

            System.out.println("=== VIOLACIÓN DE SEGURIDAD DETECTADA ===");
            System.out.println("ID Log: " + logGuardado.getId());
            System.out.println("Usuario: " + usuario.getNombre());
            System.out.println("Rol: " + usuario.getRol());
            System.out.println("Estudiante ID: " + usuario.getEstudianteId());
            System.out.println("Acción intentada: " + accionIntentada);
            System.out.println("Detalles: " + detalles);
            System.out.println("Fecha/Hora: " + logGuardado.getFechaHora());
            System.out.println("Registro guardado exitosamente en BD");
            System.out.println("=========================================");

        } catch (Exception e) {
            System.err.println("=== ERROR AL REGISTRAR VIOLACIÓN ===");
            System.err.println("Usuario: " + usuario.getNombre());
            System.err.println("Acción: " + accionIntentada);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("====================================");

            // No lanzar excepción aquí para no interferir con el flujo principal
            // El logging es importante pero no debe interrumpir la validación de seguridad
        }
    }
}