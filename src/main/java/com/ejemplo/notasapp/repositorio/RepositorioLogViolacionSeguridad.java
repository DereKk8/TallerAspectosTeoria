package com.ejemplo.notasapp.repositorio;

import com.ejemplo.notasapp.modelo.LogViolacionSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioLogViolacionSeguridad extends JpaRepository<LogViolacionSeguridad, Long> {
    // JpaRepository ya incluye el método flush() que necesitamos
    // También podemos agregar métodos de consulta personalizados aquí si es
    // necesario
}