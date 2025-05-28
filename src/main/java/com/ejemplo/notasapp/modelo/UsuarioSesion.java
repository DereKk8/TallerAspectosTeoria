package com.ejemplo.notasapp.modelo;

import java.io.Serializable;

public class UsuarioSesion implements Serializable {
    private String nombre;
    private String rol; // ALUMNO o PROFESOR
    private Long estudianteId; // Solo para alumnos, null para profesores

    public UsuarioSesion() {
    }

    public UsuarioSesion(String nombre, String rol, Long estudianteId) {
        this.nombre = nombre;
        this.rol = rol;
        // Solo asignar estudianteId si es alumno
        this.estudianteId = "ALUMNO".equals(rol) ? estudianteId : null;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
        // Si cambia el rol a profesor, limpiar estudianteId
        if ("PROFESOR".equals(rol)) {
            this.estudianteId = null;
        }
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        // Solo permitir estudianteId si es alumno
        this.estudianteId = isAlumno() ? estudianteId : null;
    }

    public boolean isAlumno() {
        return "ALUMNO".equals(rol);
    }

    public boolean isProfesor() {
        return "PROFESOR".equals(rol);
    }

    /**
     * Retorna true si el usuario tiene permisos de profesor
     * (útil para futuras validaciones)
     */
    public boolean tienePermisosProfesor() {
        return isProfesor();
    }

    /**
     * Retorna true si el usuario tiene permisos limitados de alumno
     * (útil para futuras validaciones)
     */
    public boolean tienePermisosLimitados() {
        return isAlumno();
    }

    /**
     * Método para validar si puede acceder a datos de un estudiante específico
     * - Profesores: pueden acceder a cualquier estudiante
     * - Alumnos: solo a sus propios datos
     */
    public boolean puedeAccederAEstudiante(Long estudianteIdSolicitado) {
        if (isProfesor()) {
            return true; // Los profesores pueden acceder a cualquier estudiante
        } else if (isAlumno()) {
            return this.estudianteId != null && this.estudianteId.equals(estudianteIdSolicitado);
        }
        return false; // Rol no reconocido
    }

    @Override
    public String toString() {
        return String.format("UsuarioSesion{nombre='%s', rol='%s', estudianteId=%s}",
                nombre, rol, estudianteId);
    }
}