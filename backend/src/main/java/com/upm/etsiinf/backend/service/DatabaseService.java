package com.upm.etsiinf.backend.service;

import com.upm.etsiinf.backend.model.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    private final JdbcTemplate defaultJdbcTemplate;  // para obtener procesos e indicadores
    private final DBManagerService gestor;

    @Autowired
    public DatabaseService(JdbcTemplate defaultJdbcTemplate, DBManagerService gestor) {
        this.defaultJdbcTemplate = defaultJdbcTemplate;
        this.gestor = gestor;
    }


    public List<Map<String, Object>> obtenerProcesos() {
      //  String sql = "SELECT id, type, coding, process_name, version, approval_date, is_subprocess FROM process";
        String sql = "SELECT id, coding, process_name FROM process";
        return defaultJdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> obtenerIndicadoresPorProceso(int processId) {
      //  String sql = "SELECT i.id, i.coding, i.indicator_name, i.description, i.calc_method, i.period, i.type, i.standard, " +
        String sql = "SELECT i.id, i.coding, i.indicator_name, " +
                "ig.id as indicator_group_id, ig.coding as ig_coding, ig.indicator_group_name " +
                "FROM indicator i " +
                "JOIN indicator_group ig ON i.indicator_group_id = ig.id " +
                "WHERE ig.process_id = ?";
        return defaultJdbcTemplate.queryForList(sql, processId);
    }

    public List<UpdateResult> actualizarIndicatorInstance(
            String processId,
            Map<String, String> mapeoColumnas,
            List<Map<String, Object>> datos,
            Date date,
            String atributo,
            String columnaValorAtributo,  String columnaAnoAcademico) {

        List<UpdateResult> results = new ArrayList<>();
        if (datos == null || datos.isEmpty()) {
            return results;
        }

        String sql = "UPDATE indicator_instance "
                + "SET field = ?, valid = true, modified_date = ? "
                + "WHERE indicator_name = ? AND coding = ?";

        for (Map<String, Object> fila : datos) {
            //  1. Obtener el año académico para conectar a la base de datos
          //  String rawYear = (String) fila.get("Año académico");
            String rawYear = (String) fila.get(columnaAnoAcademico);


            String year = convertirAnoAcademico(rawYear);
            JdbcTemplate jdbc = gestor.getJdbcTemplate(year);

            if (jdbc == null) {
                for (String col : mapeoColumnas.keySet()) {
                    String indicator = mapeoColumnas.get(col);
                    results.add(new UpdateResult(
                            year, col, indicator, fila.get(col),
                            false, 0,
                            "Sin conexión a BBDD para año " + year
                    ));
                }
                continue;
            }

            //  2. Obtener el coding del proceso
            String processCoding = obtenerCodingProceso(jdbc, processId);
            if (processCoding == null || processCoding.trim().isEmpty()) {
                for (String col : mapeoColumnas.keySet()) {
                    String indicator = mapeoColumnas.get(col);
                    results.add(new UpdateResult(
                            year, col, indicator, fila.get(col),
                            false, 0,
                            "Proceso no encontrado para ID: " + processId
                    ));
                }
                continue;
            }

            //  3. Procesar columnas mapeadas
            for (Map.Entry<String, String> entry : mapeoColumnas.entrySet()) {
                String fileColumn = entry.getKey();
                String indicatorName = entry.getValue();
                Object value = fila.get(fileColumn);

                String indicatorCoding = obtenerCodingIndicator(jdbc, indicatorName);
                if (indicatorCoding == null || indicatorCoding.trim().isEmpty()) {
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            false, 0,
                            "Indicador no encontrado: " + indicatorName
                    ));
                    continue;
                }
                String composite=null;
                if(atributo==null ||columnaValorAtributo == null || columnaValorAtributo.trim().isEmpty()) {
                   composite = processCoding + "-" + indicatorCoding;
                }
                else {
                    //  4. Obtener el valor del atributo desde la columna seleccionada
                    String rawPossibleValue = (String) fila.get(columnaValorAtributo);
                    if (rawPossibleValue == null || rawPossibleValue.trim().isEmpty()) {
                        results.add(new UpdateResult(
                                year, fileColumn, indicatorName, value,
                                false, 0,
                                "Valor del atributo vacío en columna: " + columnaValorAtributo
                        ));
                        continue;
                    }
                    //  5. Construir el coding y hacer el UPDATE
                    System.out.println("antes" + rawPossibleValue);
                    if( "YY-ZZ".equals(obtenerCodingPorId(atributo))) rawPossibleValue = convertirAnoAcademicoValor(rawPossibleValue);
                    System.out.println("despuees" + rawPossibleValue);
                    composite = processCoding + "-" + indicatorCoding + "[" + rawPossibleValue + "]";
                }
                try {
                    int updated = jdbc.update(sql, value, date, indicatorName, composite);
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            updated > 0, updated,
                            updated > 0 ? null : "No se actualizó ninguna fila porque no se encontro un indicador con el siguiente codigo " +composite
                    ));
                } catch (Exception ex) {
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            false, 0, ex.getMessage()
                    ));
                }
            }
        }

        return results;
    }

    private String obtenerCodingProceso(JdbcTemplate jdbcTemplate, String processId) {
        String sql = "SELECT coding FROM process WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{processId}, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String obtenerCodingIndicator(JdbcTemplate jdbcTemplate, String indicatorName) {
        String sql = "SELECT coding FROM indicator WHERE indicator_name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{indicatorName}, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    private String convertirAnoAcademico(String valor) {
        if (valor == null || !valor.contains("-")) return valor;

        try {
            String[] partes = valor.split("-");
            int inicio = Integer.parseInt(partes[0]);
            int fin = Integer.parseInt(partes[1]);
            if (fin < 100) fin += 2000; // ej. "21" → 2021
            return inicio + "_" + fin;
        } catch (Exception e) {
            System.err.println("Error al convertir el año académico: " + valor);
            return valor;
        }
    }
    private String convertirAnoAcademicoValor(String valor) {
        if (valor == null || !valor.contains("-")) return valor;

        try {
            String[] partes = valor.split("-");
            int inicio = Integer.parseInt(partes[0]);
            int fin = Integer.parseInt(partes[1]);
            if (fin > 100) fin -= 2000; // ej. "21" → 2021
            return inicio + "-" + fin;
        } catch (Exception e) {
            System.err.println("Error al convertir el año académico: " + valor);
            return valor;
        }
    }
    public List<Map<String, Object>> obtenerAtributos() {
        String sql = "SELECT id, coding, description, position FROM attribute";
        return defaultJdbcTemplate.queryForList(sql);
    }
    public String obtenerCodingPorId(String id) {
        String sql = "SELECT coding FROM attribute WHERE id = ?";
            return defaultJdbcTemplate.queryForObject(sql, new Object[]{id}, String.class);
    }


    public List<Map<String, Object>> obtenerPossibleValuesPorAtributo(int attributeId) {
        String sql = "SELECT id, value FROM possible_value WHERE attribute_id = ?";
        return defaultJdbcTemplate.queryForList(sql, attributeId);
    }
}
