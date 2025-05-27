package com.ejemplo.notasapp.servicio;

import com.ejemplo.notasapp.modelo.Nota;
import com.ejemplo.notasapp.repositorio.RepositorioNota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioNota {

    @Autowired
    private RepositorioNota repositorioNota;

    public List<Nota> obtenerNotasPorEstudianteYMateria(Long estudianteId, String materia) {
        return repositorioNota.findByEstudianteIdAndMateria(estudianteId, materia);
    }
    public double calcularPromedioPorMateria(Long estudianteId, String materia) {
        List<Nota> notas = obtenerNotasPorEstudianteYMateria(estudianteId, materia);
        double sumaPonderada = notas.stream()
                .mapToDouble(n -> n.getValor() * n.getPorcentaje())
                .sum();
        double totalPorcentaje = notas.stream()
                .mapToDouble(Nota::getPorcentaje)
                .sum();
        return totalPorcentaje > 0 ? sumaPonderada / totalPorcentaje : null;
        //return notas.stream().mapToDouble((Nota::getValor)).average().orElse(0.0);
    }

}
