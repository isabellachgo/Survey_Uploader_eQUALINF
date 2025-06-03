package com.upm.etsiinf.backend.model;

/**
 * Modelo con los resultados de la actualización.
 */
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
     * @param year anio academico
     * @param column columna
     * @param indicator indicador
     * @param value valor
     * @param success resultado actualizacion
     * @param rowsUpdated filas actualizadas
     * @param errorMessage mensaje
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

    /**
     * Devuelve el nombre de la columna (indicador) que se subió
     * @return nombre columna
     */
    public String getColumn() {
        return column;
    }

    /**
     * Devuelve el nombre del indicador de la bbdd que se  intentó actualizar
     * @return nombre indicador
     */
    public String getIndicator() {
        return indicator;
    }

    /**
     * Devuelve el año académico (bbdd) que se actualizó
     * @return base de datos
     */
    public String getYear() {
        return year;
    }

    /**
     * Indica si la actualización para esa fila fue valida
     * @return true si se actualizó, false si ocurrió algún problema
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Numero de filas cambiadas en la base de datos para esa fila del archivo
     * @return Numero de filas
     */
    public int getRowsUpdated() {
        return rowsUpdated;
    }

    /**
     * Valor que se ha intentado añadir al indicador de la base de datos
     * @return valor indicador
     */
    public Object getValue() {
        return value;
    }

    /**
     * Mensaje de error en caso de que la actualización haya fallado
     * @return mensaje de error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

}