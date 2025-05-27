package com.ejemplo.notasapp.excepcion;

public class EstudianteNoEncontrado extends RuntimeException {

    private final String mensaje;
    private final String codigoError;

    public EstudianteNoEncontrado(String mensaje, String codigoError, Exception causa) {
        super(mensaje, causa);
        this.mensaje = mensaje;
        this.codigoError = codigoError;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
