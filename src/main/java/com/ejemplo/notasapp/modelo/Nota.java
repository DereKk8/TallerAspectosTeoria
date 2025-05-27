package com.ejemplo.notasapp.modelo;

import jakarta.persistence.*;

@Entity
public class Nota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String materia;
    private String observacion;
    private Double valor;
    private Double porcentaje;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    // Getters
    public Long getId() {
        return id;
    }

    public String getMateria() {
        return materia;
    }

    public String getObservacion() {
        return observacion;
    }

    public Double getValor() {
        return valor;
    }

    public Double getPorcentaje() {
        return porcentaje;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public void setPorcentaje(Double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }
}