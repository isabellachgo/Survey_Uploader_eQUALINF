package com.upm.etsiinf.backend.model;

public class UpdateResult {

    private String year;
    private String column;
    private String indicator;
    private Object value;
    private boolean success;
    private int rowsUpdated;
    private String errorMessage;

    /**
     * Representa el resultado tras la subida de indicadores a la plataforma..
     * <p>
     * Contiene información básica de la subida como indicador, valor, filas actualizadas, columna y mensaje descriptivo.
     * Esta clase proporciona métodos getters estándar.
     */
    public UpdateResult(String year, String column, String indicator,
                        Object value, boolean success, int rowsUpdated, String errorMessage) {
        this.year = year;
        this.column = column;
        this.indicator = indicator;
        this.value = value;
        this.success = success;
        this.rowsUpdated = rowsUpdated;
        this.errorMessage = errorMessage;
    }

    // Nota: no borrar aunque aparezcan como 'no usages' SB los usa para devolver los JSON.
    public String getColumn() {
        return column;
    }

    public String getIndicator() {
        return indicator;
    }

    public String getYear() {
        return year;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getRowsUpdated() {
        return rowsUpdated;
    }

    public Object getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}