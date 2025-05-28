package com.ejemplo.notasapp.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LogViolacionSeguridad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreUsuario;
    private String rol;
    private String accionIntentada;
    private String detalles;
    private LocalDateTime fechaHora;

    // Constructores
    public LogViolacionSeguridad() {
    }

    public LogViolacionSeguridad(String nombreUsuario, String rol, String accionIntentada, String detalles) {
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.accionIntentada = accionIntentada;
        this.detalles = detalles;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getAccionIntentada() {
        return accionIntentada;
    }

    public void setAccionIntentada(String accionIntentada) {
        this.accionIntentada = accionIntentada;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}