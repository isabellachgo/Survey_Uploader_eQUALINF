package com.upm.etsiinf.backend.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileProcessingService {
    /**
     * Método auxiliar que filtra la lista de filas preprocesadas,
     * conservando únicamente las columnas indicadas en el mapeo.
     */
    public List<Map<String, Object>> dataFilter(List<Map<String, String>> datos, Map<String, String> mapeoColumnas, String columnaAnoAcademico) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map<String, String> fila : datos) {
            Map<String, Object> filaFiltrada = new HashMap<>();

            for (String columna : fila.keySet()) {
                if (mapeoColumnas.containsKey(columna) || columna.equals(columnaAnoAcademico)) {
                    filaFiltrada.put(columna, fila.get(columna));
                }
            }

            resultado.add(filaFiltrada);
        }

        return resultado;
    }
}