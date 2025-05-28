package com.ejemplo.notasapp.aspecto;

import com.ejemplo.notasapp.excepcion.ViolacionSeguridad;
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

/**
 * Clase de aspectos para la autorización y control de acceso en el sistema de
 * notas.
 * 
 * Este sistema implementa Programación Orientada a Aspectos (AOP) para
 * interceptar
 * operaciones y validar permisos de usuario basado en roles:
 * 
 * - ALUMNOS: Solo operaciones de lectura y únicamente sobre sus propias notas
 * - PROFESORES: Acceso completo a todas las funcionalidades
 * 
 * MANEJO DE VIOLACIONES DE SEGURIDAD:
 * - Todas las violaciones se registran en la base de datos (tabla
 * log_violacion_seguridad)
 * - Se lanza ViolacionSeguridad (excepción personalizada) que es capturada por
 * GlobalExceptionHandler
 * - El usuario es redirigido a una pantalla de error específica
 * (/error/violacion-seguridad)
 * - Se mantiene logging detallado en consola para monitoreo en tiempo real
 * 
 * TIPOS DE VALIDACIONES:
 * 1. Operaciones de escritura en notas (crear, guardar, editar, eliminar)
 * 2. Acceso a notas de otros estudiantes
 * 3. Gestión de estudiantes (solo profesores)
 * 4. Servicios de negocio (cálculo de promedios, etc.)
 * 5. Validación de autenticación
 */
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

    // Pointcuts para operaciones de gestión de estudiantes
    @Pointcut("execution(* com.ejemplo.notasapp.controlador.EstudianteController.nuevo(..))")
    public void operacionesCrearEstudiante() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.EstudianteController.guardar(..))")
    public void operacionesGuardarEstudiante() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.EstudianteController.editar(..))")
    public void operacionesEditarEstudiante() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.EstudianteController.eliminar(..))")
    public void operacionesEliminarEstudiante() {
    }

    @Pointcut("execution(* com.ejemplo.notasapp.controlador.EstudianteController.listar(..))")
    public void operacionesListarEstudiantes() {
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

            throw ViolacionSeguridad.operacionNoPermitida(
                    usuario.getNombre(),
                    accionIntentada,
                    "Los alumnos no pueden realizar operaciones de escritura en notas");
        }
    }

    /**
     * Aspecto que valida que los alumnos no puedan gestionar estudiantes
     * Intercepta: crear, guardar, editar y eliminar estudiantes
     */
    @Before("operacionesCrearEstudiante() || operacionesGuardarEstudiante() || operacionesEditarEstudiante() || operacionesEliminarEstudiante()")
    public void validarGestionEstudiantesAlumno(JoinPoint joinPoint) {
        UsuarioSesion usuario = obtenerUsuarioSesion();

        if (usuario != null && usuario.isAlumno()) {
            String accionIntentada = joinPoint.getSignature().getName();
            String controlador = joinPoint.getTarget().getClass().getSimpleName();
            String detalles = String.format(
                    "Intento de gestión de estudiantes: %s.%s con parámetros: %s. Los alumnos no tienen permisos para gestionar estudiantes.",
                    controlador, accionIntentada, Arrays.toString(joinPoint.getArgs()));

            // Registrar violación de seguridad
            registrarViolacionSeguridad(usuario, accionIntentada, detalles);

            throw new SecurityException(
                    String.format(
                            "Acceso denegado: Los alumnos no pueden gestionar estudiantes. Operación: %s",
                            accionIntentada));
        }
    }

    /**
     * Aspecto que controla el acceso de alumnos a la lista de estudiantes
     * Los alumnos solo pueden ver la lista pero no pueden realizar acciones de
     * gestión
     */
    @Around("operacionesListarEstudiantes()")
    public Object validarAccesoListaEstudiantesAlumno(ProceedingJoinPoint joinPoint) throws Throwable {
        UsuarioSesion usuario = obtenerUsuarioSesion();

        if (usuario != null && usuario.isAlumno()) {
            // Log de acceso a lista de estudiantes por parte de un alumno
            System.out.println("=== ACCESO A LISTA DE ESTUDIANTES ===");
            System.out.println("Usuario alumno: " + usuario.getNombre());
            System.out.println("Estudiante ID: " + usuario.getEstudianteId());
            System.out.println("Acceso permitido solo para visualización");
            System.out.println("=====================================");

            // Los alumnos pueden ver la lista pero no pueden realizar acciones
            // Esto se controla en la vista y con los otros aspectos
        }

        // Permitir que continúe la ejecución
        return joinPoint.proceed();
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

                    throw ViolacionSeguridad.accesoNoAutorizado(
                            usuario.getNombre(),
                            metodo,
                            String.format(
                                    "Los alumnos solo pueden acceder a sus propias notas. Intento de acceso a estudiante ID: %d",
                                    estudianteIdSolicitado));
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

                    throw ViolacionSeguridad.accesoNoAutorizado(
                            usuario.getNombre(),
                            "ServicioNota." + metodo,
                            String.format(
                                    "Los alumnos solo pueden acceder a servicios de sus propias notas. Intento de acceso a estudiante ID: %d",
                                    estudianteIdSolicitado));
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

                            throw ViolacionSeguridad.recursoNoPermitido(
                                    usuario.getNombre(),
                                    metodo,
                                    String.format(
                                            "Los alumnos solo pueden %s sus propias notas. Nota ID: %d pertenece a estudiante ID: %d",
                                            metodo, notaId, estudiantePropietario));
                        }
                    } else {
                        // La nota no existe
                        String detalles = String.format("Intento de %s nota inexistente ID: %d", metodo, notaId);
                        registrarViolacionSeguridad(usuario, metodo, detalles);

                        throw ViolacionSeguridad.recursoNoPermitido(
                                usuario.getNombre(),
                                metodo,
                                String.format("La nota solicitada (ID: %d) no existe en el sistema", notaId));
                    }
                } catch (ViolacionSeguridad vs) {
                    // Re-lanzar excepciones de violación de seguridad
                    throw vs;
                } catch (Exception e) {
                    // Error al consultar la base de datos
                    String detalles = String.format("Error al validar acceso para %s nota ID: %d - %s",
                            metodo, notaId, e.getMessage());
                    registrarViolacionSeguridad(usuario, metodo, detalles);

                    System.err.println("Error en validación de acceso: " + e.getMessage());
                    throw ViolacionSeguridad.accesoNoAutorizado(
                            usuario.getNombre(),
                            metodo,
                            "Error técnico al validar permisos de acceso a la nota");
                }
            } else {
                // Parámetros incorrectos
                String detalles = String.format("Intento de %s con parámetros inválidos: %s",
                        metodo, Arrays.toString(args));
                registrarViolacionSeguridad(usuario, metodo, detalles);

                throw ViolacionSeguridad.operacionNoPermitida(
                        usuario.getNombre(),
                        metodo,
                        "Parámetros inválidos para la operación solicitada");
            }
        }

        // Si llegamos aquí, el usuario es profesor o la validación pasó
        return joinPoint.proceed();
    }

    /**
     * Aspecto general que intercepta todas las operaciones del EstudianteController
     * para validar acceso y realizar logging detallado
     * 
     * NOTA: Este aspecto reemplaza los aspectos específicos para evitar duplicación
     * e intercepta TODAS las operaciones de gestión de estudiantes
     */
    @Around("execution(* com.ejemplo.notasapp.controlador.EstudianteController.*(..))")
    public Object validarAccesoGeneralEstudiantesController(ProceedingJoinPoint joinPoint) throws Throwable {
        UsuarioSesion usuario = obtenerUsuarioSesion();
        String metodo = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        if (usuario != null) {
            // Log detallado de acceso
            System.out.println("=== ACCESO A ESTUDIANTE CONTROLLER ===");
            System.out.println("Usuario: " + usuario.getNombre());
            System.out.println("Rol: " + usuario.getRol());
            System.out.println("Método: " + metodo);
            System.out.println("Parámetros: " + Arrays.toString(args));

            if (usuario.isAlumno()) {
                System.out.println("Tipo de acceso: ALUMNO - Verificando permisos...");

                // Los alumnos solo pueden acceder a "listar" estudiantes
                if ("listar".equals(metodo)) {
                    System.out.println("Acceso permitido: Visualización de lista de estudiantes");
                } else {
                    System.out.println("Acceso DENEGADO: Operación no permitida para alumnos");

                    String detalles = String.format(
                            "Intento de acceso directo a EstudianteController.%s con parámetros: %s. Acceso denegado para rol ALUMNO.",
                            metodo, Arrays.toString(args));

                    registrarViolacionSeguridad(usuario, "EstudianteController." + metodo, detalles);

                    throw ViolacionSeguridad.operacionNoPermitida(
                            usuario.getNombre(),
                            "EstudianteController." + metodo,
                            "Los alumnos no pueden gestionar estudiantes. Solo pueden visualizar la lista");
                }
            } else if (usuario.isProfesor()) {
                System.out.println("Tipo de acceso: PROFESOR - Acceso completo permitido");
            }

            System.out.println("======================================");
        } else {
            System.out.println("=== ACCESO SIN AUTENTICACIÓN ===");
            System.out.println("Método: " + metodo);
            System.out.println("Usuario no autenticado - Redirigiendo al login");
            System.out.println("=================================");

            // Usuario no autenticado, redirigir al login
            throw ViolacionSeguridad.usuarioNoAutenticado(
                    "EstudianteController." + metodo,
                    "Intento de acceso sin autenticación a funcionalidades de gestión de estudiantes");
        }

        return joinPoint.proceed();
    }

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
        }
    }
}