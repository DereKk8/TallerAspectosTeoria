package com.ejemplo.notasapp.repositorio;

import com.ejemplo.notasapp.modelo.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepositorioNota extends JpaRepository<Nota, Long> {
    List<Nota> findByEstudianteId(Long estudianteId);

    List<Nota> findByEstudianteIdAndMateria(Long estudianteId, String materia);
}