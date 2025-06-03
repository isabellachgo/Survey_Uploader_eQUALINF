package com.upm.etsiinf.backend.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Servicio que se encarga de aplicar el mapeo de las columnas y prepara los datos para enviarlos a la base de datos.
 */
@Service
public class FileProcessingService {
    /**
     * Constructir por defecto.No realiza ninguna operación.
     */
    public FileProcessingService(){

    }

    /**
     * Método auxiliar que filtra la lista de filas preprocesadas,
     * conservando únicamente las columnas indicadas en el mapeo y la que contiene los años académicos (necesaria para la conexión a la base de data).
     * @param data informacion guardada del archivo
     * @param columnMapping columnas mapeadas por el usuario
     * @param academicYearColumn  columna con los años academicos
     * @return lista de mapas con  pares clave-valor (nombre de columna → valor de celda).
     */
    public List<Map<String, Object>> dataFilter(List<Map<String, String>> data, Map<String, String> columnMapping, String academicYearColumn) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map<String, String> fila : data) {
            Map<String, Object> filaFiltrada = new HashMap<>();

            for (String columna : fila.keySet()) {
                if (columnMapping.containsKey(columna) || columna.equals(academicYearColumn)) {
                    filaFiltrada.put(columna, fila.get(columna));
                }
            }
            resultado.add(filaFiltrada);
        }
        return resultado;
    }
}