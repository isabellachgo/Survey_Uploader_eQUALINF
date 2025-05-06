package com.upm.etsiinf.backend.model;

public class UpdateResult {

    private String year;
    private String column;
    private String indicator;
    private Object value;
    private boolean success;
    private int rowsUpdated;
    private String errorMessage;

    // Constructor “todo en uno”
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

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getRowsUpdated() {
        return rowsUpdated;
    }

    public void setRowsUpdated(int rowsUpdated) {
        this.rowsUpdated = rowsUpdated;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}