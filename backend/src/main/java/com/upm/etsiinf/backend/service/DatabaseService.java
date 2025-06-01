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

/**
 * Consulta los procesos de la base de datos por defecto, especificada en el archivo 'application-properties'.
 * @return Lista de  mapas, donde cada mapa es un proceso con los campos (id, coding, process_name).
 */
    public List<Map<String, Object>> getProcesses() {
        String sql = "SELECT id, coding, process_name FROM process";
        return defaultJdbcTemplate.queryForList(sql);
    }

/**
 * Obtiene la lista de indicadores asociados a un proceso específico.
 * <p>Realiza una consulta SQL que une las tablas <code>indicator</code> e
 *    <code>indicator_group</code>, filtrando por el ID del proceso.
 *</p>
 * @param processId ID del proceso cuyos indicadores se desean consultar.
 * @return Una lista de mapas, donde cada mapa contiene los campos:id, coding, indicator_name, indicator_group_id,
 * ig_coding e indicator_group_name.
 */
    public List<Map<String, Object>> getIndicatorsbyProcess(int processId) {
        String sql = "SELECT i.id, i.coding, i.indicator_name, " +
                "ig.id as indicator_group_id, ig.coding as ig_coding, ig.indicator_group_name " +
                "FROM indicator i " +
                "JOIN indicator_group ig ON i.indicator_group_id = ig.id " +
                "WHERE ig.process_id = ?";
        return defaultJdbcTemplate.queryForList(sql, processId);
    }

    /**
     * Consulta los atributos de la base de datos por defecto, especificada en el archivo 'application-properties'.
     * @return Lista de  mapas, donde cada mapa es un atributo con los campos (id, coding, description).
     */
    public List<Map<String, Object>> getAttributes() {
        String sql = "SELECT id, coding, description, position FROM attribute";
        return defaultJdbcTemplate.queryForList(sql);
    }

    /**
     *  Actualiza el valor de los indicadores mapeados por el usuario, comprobando
     *  si el indicador existe y si su código sigue el formato esperado.
     *  <p> Por cada fila:
     *  -se toma el valor de 'columnaAnoAcademico' y se conecta a esa base de datos.
     * - se recupera el codigo del 'processId'
     * - Para cada columna asociada a un indicador, se obtiene el codigo interno del indicador
     * - se construye el codigo del InidcadorInstance a buscar:
     *   + Si no hay atributo: código_proceso - código_indicador
     *   + Si hay un atributo: código_proceso - código_indicador [valor_atributo]
     * - si se encuentra un indicatorInstance:
     *   + se modifica el valor del campo
     *   + se marca como válido
     *   + se actualizando la fecha de modificación a 'date'.
     *  </p>
     * @param processId
     * @param columnMapping
     * @param data
     * @param date
     * @param attribute
     * @param AttributeValueColumn
     * @param academicYearColumn
     * @return Lista de resultados de la actialización. tipo UpdateResult
     */
    public List<UpdateResult> updateIndicatorInstance(
            String processId,
            Map<String, String> columnMapping,
            List<Map<String, Object>> data,
            Date date,
            String attribute,
            String AttributeValueColumn,  String academicYearColumn) {

        List<UpdateResult> results = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return results;
        }

        String sql = "UPDATE indicator_instance "
                + "SET field = ?, valid = true, modified_date = ? "
                + "WHERE indicator_name = ? AND coding = ?";

        for (Map<String, Object> fila : data) {
            //  1. Obtener el año académico para conectar a la base de datos
            String rawYear = (String) fila.get(academicYearColumn);
            String year = transformDBYear(rawYear);
            JdbcTemplate jdbc = gestor.getJdbcTemplate(year);

            if (jdbc == null) {
                for (String col : columnMapping.keySet()) {
                    String indicator = columnMapping.get(col);
                    results.add(new UpdateResult(
                            year, col, indicator, fila.get(col),
                            false, 0,
                            "Sin conexión a la base de datos para el año " + year
                    ));
                }
                continue;
            }

            //  2. Obtener el coding del proceso
            String processCoding = obtenerCodingProceso(jdbc, processId);
            if (processCoding == null || processCoding.trim().isEmpty()) {
                for (String col : columnMapping.keySet()) {
                    String indicator = columnMapping.get(col);
                    results.add(new UpdateResult(
                            year, col, indicator, fila.get(col),
                            false, 0,
                            "Proceso no encontrado para ID: " + processId
                    ));
                }
                continue;
            }

            //  3. Procesar columnas mapeadas
            for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
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
                if(attribute==null ||AttributeValueColumn == null || AttributeValueColumn.trim().isEmpty()) {
                   composite = processCoding + "-" + indicatorCoding;
                }
                else {
                    //  4. Obtener el valor del atributo desde la columna seleccionada
                    String rawPossibleValue = (String) fila.get(AttributeValueColumn);
                    if (rawPossibleValue == null || rawPossibleValue.trim().isEmpty()) {
                        results.add(new UpdateResult(
                                year, fileColumn, indicatorName, value,
                                false, 0,
                                "Valor del atributo vacío en columna: " + AttributeValueColumn
                        ));
                        continue;
                    }
                    //  5. Construir el coding y hacer el UPDATE
                    if( "YY-ZZ".equals(getAttributeCodingByID(attribute))) rawPossibleValue = transformValueYear(rawPossibleValue);
                    composite = processCoding + "-" + indicatorCoding + "[" + rawPossibleValue + "]";
                }
                try {
                    int updated = jdbc.update(sql, value, date, indicatorName, composite);
                    results.add(new UpdateResult(
                            year, fileColumn, indicatorName, value,
                            updated > 0, updated,
                            updated > 0 ? null : "No se ha encontrado el indicador asociado al proceso y/o atributo seleccionado "
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

    /**
     * Recupera el código de un atributo cuyo id es 'id'.
     * @param id
     * @return código del atributo
     */
    private String getAttributeCodingByID(String id) {
        String sql = "SELECT coding FROM attribute WHERE id = ?";
        return defaultJdbcTemplate.queryForObject(sql, new Object[]{id}, String.class);
    }

    /**
     * Recupera el código de un proceso cuyo id es 'id'.
     * @param jdbcTemplate
     * @param processId
     * @return código del proceso
     */
    private String obtenerCodingProceso(JdbcTemplate jdbcTemplate, String processId) {
        String sql = "SELECT coding FROM process WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{processId}, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     *  Recupera el código de un indicador cuyo nombre es 'indicatorName'.
     * @param jdbcTemplate
     * @param indicatorName
     * @return código del indicador
     */
    private String obtenerCodingIndicator(JdbcTemplate jdbcTemplate, String indicatorName) {
        String sql = "SELECT coding FROM indicator WHERE indicator_name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{indicatorName}, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Convierte un año académico 'valor' del formato 'AAAA-AA' a 'AAAA-AAAA'.
     * @param valor
     * @return  String del año transformado
     */
    private String transformDBYear(String valor) {
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

    /**
     *Convierte un año académico 'valor' del formato 'AAAA-AAAA' a 'AAAA-AA'.
     * @param valor
     * @return String del año transformado
     */
    private String transformValueYear(String valor) {
        if (valor == null || !valor.contains("-")) return valor;

        try {
            String[] partes = valor.split("-");
            int inicio = Integer.parseInt(partes[0]);
            int fin = Integer.parseInt(partes[1]);
            if (fin > 100) fin -= 2000; // ej. "2021" → 21
            return inicio + "-" + fin;
        } catch (Exception e) {
            System.err.println("Error al convertir el año académico: " + valor);
            return valor;
        }
    }

}
