package com.ejemplo.notasapp.excepcion;

public class ViolacionSeguridad extends RuntimeException {

    private final String tipoViolacion;
    private final String usuario;
    private final String operacion;
    private final String detalles;
    private final String codigoError;

    public ViolacionSeguridad(String tipoViolacion, String usuario, String operacion, String detalles,
            String codigoError, Exception causa) {
        super(String.format("Violación de seguridad: %s - Usuario: %s - Operación: %s", tipoViolacion, usuario,
                operacion), causa);
        this.tipoViolacion = tipoViolacion;
        this.usuario = usuario;
        this.operacion = operacion;
        this.detalles = detalles;
        this.codigoError = codigoError;
    }

    public ViolacionSeguridad(String tipoViolacion, String usuario, String operacion, String detalles) {
        this(tipoViolacion, usuario, operacion, detalles, "403", null);
    }

    public String getTipoViolacion() {
        return tipoViolacion;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getDetalles() {
        return detalles;
    }

    public String getCodigoError() {
        return codigoError;
    }

    // Métodos de conveniencia para tipos comunes de violaciones
    public static ViolacionSeguridad operacionNoPermitida(String usuario, String operacion, String detalles) {
        return new ViolacionSeguridad("OPERACION_NO_PERMITIDA", usuario, operacion, detalles);
    }

    public static ViolacionSeguridad accesoNoAutorizado(String usuario, String operacion, String detalles) {
        return new ViolacionSeguridad("ACCESO_NO_AUTORIZADO", usuario, operacion, detalles);
    }

    public static ViolacionSeguridad recursoNoPermitido(String usuario, String operacion, String detalles) {
        return new ViolacionSeguridad("RECURSO_NO_PERMITIDO", usuario, operacion, detalles);
    }

    public static ViolacionSeguridad usuarioNoAutenticado(String operacion, String detalles) {
        return new ViolacionSeguridad("USUARIO_NO_AUTENTICADO", "ANONIMO", operacion, detalles);
    }
}