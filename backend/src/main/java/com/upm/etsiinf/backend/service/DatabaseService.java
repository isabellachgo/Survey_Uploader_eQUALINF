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
            String atributoId,
            String columnaValorAtributo,  String columnaAnoAcademico) {

        List<UpdateResult> results = new ArrayList<>();
        if (datos == null || datos.isEmpty()) {
            return results;
        }

        String sql = "UPDATE indicator_instance "
                + "SET field = ?, valid = true, modified_date = ? "
                + "WHERE indicator_name = ? AND coding LIKE ?";

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

                //  5. Verificar relaciones
                boolean tieneAtributo = verificarIndicadorTieneAtributo(jdbc, indicatorName, atributoId);
                if (!tieneAtributo) {
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            false, 0,
                            "El indicador no tiene el atributo  asociado"
                    ));
                    continue;
                }

                boolean valorValido = verificarValorPosible(jdbc, atributoId, rawPossibleValue);
                if (!valorValido) {
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            false, 0,
                            "Valor no válido para atributo: "+ atributoId +" " + rawPossibleValue
                    ));
                    continue;
                }

                //  6. Construir el coding y hacer el UPDATE
                String composite = processCoding + "-" + indicatorCoding + "[" + rawPossibleValue + "]%";
                try {
                    int updated = jdbc.update(sql, value, date, indicatorName, composite);
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            updated > 0, updated,
                            updated > 0 ? null : "No se actualizó ninguna fila" +composite
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





   /* public void actualizarIndicatorInstance(String processId, Map<String, String> mapeoColumnas, List<Map<String, Object>> datos) {
        if (datos == null || datos.isEmpty()) {
            System.out.println("No hay datos para actualizar.");
            return;
        }

        String sql = "UPDATE indicator_instance SET field = ?, valid = true, modified_date = ? WHERE indicator_name = ? AND coding LIKE ?";

        for (Map<String, Object> fila : datos) {

            String añoCrudo = (String) fila.get("Año académico");
            String añoConvertido = convertirAnoAcademico(añoCrudo);
            JdbcTemplate jdbcTemplate = gestor.getJdbcTemplate(añoConvertido);

            if (jdbcTemplate == null) {
                System.err.println(" No hay conexión para el año académico: " + añoConvertido);
                continue;
            }

            String processCoding = obtenerCodingProceso(jdbcTemplate, processId);

            for (Map.Entry<String, String> entry : mapeoColumnas.entrySet()) {
                String fileColumn = entry.getKey();
                String indicadorNombre = entry.getValue();
                Object valor = fila.get(fileColumn);
                Date modifiedDate = new Date(System.currentTimeMillis());
                String indicatorCoding = obtenerCodingIndicator(jdbcTemplate, indicadorNombre);
                String compositeCoding = processCoding + "-" + indicatorCoding + "%";

                int rowsUpdated = jdbcTemplate.update(sql, valor, modifiedDate, indicadorNombre, compositeCoding);
                System.out.println(" [" + añoConvertido + "] Indicador '" + indicadorNombre + "' actualizado con valor: " + valor +
                        " (" + rowsUpdated + " filas afectadas)");
            }
        }
    }*/

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
    public List<Map<String, Object>> obtenerAtributos() {
        String sql = "SELECT id, coding, description, position FROM attribute";
        return defaultJdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> obtenerPossibleValuesPorAtributo(int attributeId) {
        String sql = "SELECT id, value FROM possible_value WHERE attribute_id = ?";
        return defaultJdbcTemplate.queryForList(sql, attributeId);
    }
    private boolean verificarIndicadorTieneAtributo(JdbcTemplate jdbc, String indicatorName, String atributoId) {
        try {
            String sql = "SELECT COUNT(*) " +
                    "FROM indicator i " +
                    "JOIN indicator_group ig ON i.indicator_group_id = ig.id " +
                    "JOIN inds_group_have_attribs ig_attr ON ig.id = ig_attr.indicator_group_id " +
                    "WHERE i.indicator_name = ? AND ig_attr.attribute_id = ?";
            Integer count = jdbc.queryForObject(sql, Integer.class, indicatorName, atributoId);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("Error verificando si el indicador tiene el atributo: " + e.getMessage());
            return false; // Asumimos que no lo tiene si no podemos comprobarlo
        }
    }


    private boolean verificarValorPosible(JdbcTemplate jdbc, String atributoId, String valor) {
        try {
            // Intentamos buscar directamente
            String sql = "SELECT COUNT(*) FROM possible_value WHERE attribute_id = ? AND value = ?";
            Integer exactMatch = jdbc.queryForObject(sql, Integer.class, atributoId, valor);
            if (exactMatch != null && exactMatch > 0) {
                return true;
            }

            // Si no lo encuentra, intenta con valor convertido
            String alternativo = convertirAnoAcademico(valor); // ← tu método ya existente
            Integer altMatch = jdbc.queryForObject(sql, Integer.class, atributoId, alternativo);
            return altMatch != null && altMatch > 0;
        } catch (Exception e) {
            System.err.println("Error verificando valor posible: " + valor + e.getMessage());
            return false;
        }
    }




}
