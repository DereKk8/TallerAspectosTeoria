package com.ejemplo.notasapp.repositorio;

import com.ejemplo.notasapp.modelo.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioEstudiante extends JpaRepository<Estudiante, Long> {}