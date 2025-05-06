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
    public List<Map<String, Object>> filtrarDatos(List<Map<String, String>> datos, Map<String, String> mapeoColumnas, String columnaAnoAcademico) {
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

   /* public List<Map<String, Object>> procesarData(List<Map<String, String>> csvData, Map<String, String> mapeoColumnas) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, String> row : csvData) {
            Map<String, Object> filaDatos = new HashMap<>();
            // Recorremos sólo las columnas seleccionadas según el mapeo.
            for (String header : mapeoColumnas.keySet()) {
                String valor = row.get(header);
                if (valor == null) {
                    valor = "";
                }
                // Convertir el valor (por ejemplo, a número, entero, boolean, etc.).
                filaDatos.put(header, convertirTipo(valor));
            }
            result.add(filaDatos);
        }
        return result;
    }

    private Object convertirTipo(String valor) {
        return obtenerValor(valor);
    }

    private Object obtenerValor(Object valor) {
        if (valor == null) return "";

        if (valor instanceof String) {
            String strValor = ((String) valor).trim();

            if (strValor.isEmpty()) return "";

            // Manejar formato de porcentaje en CSV
            if (strValor.contains("%")) {
                try {
                    // Reemplazar coma por punto para evitar errores en conversión (en caso de "100,00%")
                    strValor = strValor.replace(",", ".").replace("%", "").trim();
                    double value = Double.parseDouble(strValor) / 100.0; // Convertir "100%" -> 1.0

                    // Formatear a 2 decimales
                    DecimalFormat df = new DecimalFormat("#.##");
                    return Double.parseDouble(df.format(value).replace(",", "."));
                } catch (NumberFormatException e) {
                    return strValor; // Si falla, devolver como texto
                }
            }

            // Caso 1: Si tiene coma, convertir a número decimal
            if (strValor.contains(",")) {
                try {
                    // Reemplazar coma por punto para convertir correctamente a decimal
                    return Double.parseDouble(strValor.replace(",", "."));
                } catch (NumberFormatException e) {
                    return strValor; // Si no es un número válido, devolverlo como texto
                }
            }

            // Caso 2: Si no tiene punto ni coma, tratar como entero
            if (!strValor.contains(".")) {
                try {
                    return Integer.parseInt(strValor); // Convertir a entero directamente
                } catch (NumberFormatException e) {
                    return strValor; // Si no es un número válido, devolverlo como texto
                }
            }

            // Caso 3: Si tiene punto, tratarlo como número decimal y eliminar el punto solo si es necesario
            if (strValor.contains(".")) {
                try {
                    // Eliminar el punto solo si se trata de un número entero con punto (como 5142)
                    strValor = strValor.replace(".", "");
                    return Integer.parseInt(strValor); // Convertir a entero sin el punto
                } catch (NumberFormatException e) {
                    return Double.valueOf(strValor); // Si no es entero, dejarlo como decimal
                }
            }

            // Intentar convertir a booleano
            if (strValor.equalsIgnoreCase("true") || strValor.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(strValor);
            }

            return strValor; // Si no es número ni booleano, devolverlo como String
        }

        return valor; // Si ya es un tipo numérico, booleano o fecha, devolverlo tal cual
    }



    /**
     * Método
     */
    /*private Object evaluarFormula(Cell cell) {
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        CellValue cellValue = evaluator.evaluate(cell);

        switch (cellValue.getCellType()) {
            case STRING:
                return cellValue.getStringValue();
            case NUMERIC:
                return cellValue.getNumberValue();
            case BOOLEAN:
                return cellValue.getBooleanValue();
            default:
                return "";
        }
    }*/

    /**
     * Método para
     */
    /*private Object obtenerValorCelda(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return obtenerValor(cell.getStringCellValue());

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue(); // Si es fecha, devolver como Date
                }
                return cell.getNumericCellValue(); // Devolver número como está

            case BOOLEAN:
                return cell.getBooleanCellValue();

            case FORMULA:
                return evaluarFormula(cell);

            case BLANK:
                return "";

            default:
                return cell.toString().trim();
        }
    }
    */
    /**
     * Método para leer y procesar los datos de un archivo cuyo formato es .csv.
     */
    /*private List<Map<String, Object>> procesarCSV(MultipartFile file, Map<String, String> criterios) throws IOException {
        List<Map<String, Object>> datosFiltrados = new ArrayList<>();       // Almacena la lista de datos que se quieren subir a la bbd con formato nombreAtributo : valor

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            String[] encabezados = null;
            Map<String, Integer> columnasCriterios = new HashMap<>();       //  Almacena nombre de la columna (que se quiere procesar)  y el indice

            int filaIndex = 0;
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(";", -1);        // -1 para incluir celdas vacías

                // ENCABEZADO
                if (filaIndex == 0) {
                    encabezados = valores;

                    // Normalizar criterios para manejar ´/ñ/etc
                    Map<String, String> criteriosNormalizados = new HashMap<>();
                    for (Map.Entry<String, String> entry : criterios.entrySet()) {
                       // String claveNormalizada = normalizarTexto(entry.getKey());
                        String valorNormalizado = Normalization.normalizarTexto(entry.getValue());
                        criteriosNormalizados.put(entry.getKey(), valorNormalizado);
                    }

                    // Mapear las columnas con los criterios normalizados
                    for (int i = 0; i < encabezados.length; i++) {
                        String columnaNormalizada = Normalization.normalizarTexto(encabezados[i].trim());
                        if (criteriosNormalizados.containsValue(columnaNormalizada)) {
                            columnasCriterios.put(columnaNormalizada, i);
                        //    System.out.println("Columna encontrada: " + columnaNormalizada + " en posición " + i);
                        }
                    }
                    // Si no se encontró ninguna columna con los criterios, devolver error
                    if (columnasCriterios.isEmpty()) {
                        System.out.println("ERROR: No se encontraron las columnas especificadas en los criterios.");
                    }
                }
                // DATOS filaIndex >0
                else {
                    Map<String, Object> filaDatos = new HashMap<>();                            //< Atributo, valor>
                    for (Map.Entry<String, Integer> entry : columnasCriterios.entrySet()) {    //Recorre las columnas en las que estan los atributos que se quieres subir
                        int index = entry.getValue();                                          // index en el que esta el valor
                        String valor = (index < valores.length) ? valores[index].trim() : "";  // agarras el valor
                        filaDatos.put(entry.getKey(), convertirTipo(valor)); // Convertir el tipo de dato
                    }
                    datosFiltrados.add(filaDatos);
                }
                filaIndex++;
            }
        }

        return datosFiltrados;
    } */
    /**
     * Método para procesar excel
     */
/*
    private List<Map<String, Object>> procesarExcel(MultipartFile file, String sheetName, Map<String, String> criterios) throws IOException {
        List<Map<String, Object>> datosFiltrados = new ArrayList<>();           // Almacena la lista de datos que se quieren subir a la bbd con formato nombreAtributo : valor
        Workbook wb = WorkbookFactory.create(file.getInputStream());
        Sheet sheet;
        if (sheetName == null || sheetName.trim().isEmpty()) {
            sheet = wb.getSheetAt(0);  // Si no hay nombre, usa la primera hoja
        } else {
            sheet = wb.getSheet(sheetName);  // Si hay nombre, busca la hoja
        }

        if (sheet == null) {
            System.out.println("ERROR: No se encontró la hoja con nombre '" + sheetName + "'.");
        }

        else {
            // Buscar la fila de encabezados
            int headerIndex = -1;
            Row headerRow = null;
            int i =0;
            //recorre cada fila
            while (i < sheet.getPhysicalNumberOfRows() && headerIndex == -1 ){  // una vez lo encuentras chao
                Row row = sheet.getRow(i);
                if (row != null) {
                    int j =0;
                    // recorre cada celda de la fila
                    while( j < row.getPhysicalNumberOfCells() && headerIndex == -1){
                        Cell celda = row.getCell(j);
                        if(celda != null && criterios.containsValue(celda.getStringCellValue().trim())){  // si esa celda esta contenida en los criterios, estas en los encabezados
                            headerRow = row;
                            headerIndex = i;
                        }
                        j++;
                    }
                }
                i++;
            }

            if (headerIndex == -1 ) {
                System.out.println( "Error: No se encontró la fila de encabezado con los criterios especificados.");
            }

            // Mapa para almacenar los índices de las columnas que coinciden con los criterios
            Map<String, Integer> columnasCriterios = new HashMap<>();

            for (int k=0; k< headerRow.getPhysicalNumberOfCells(); k++ ) {
                Cell cell = headerRow.getCell(k);
                if ( criterios.containsValue(cell.getStringCellValue().trim())) {
                    columnasCriterios.put(cell.getStringCellValue().trim(), k); // Guardamos el índice de la columna
                }
            }
            // Si no se encontró ninguna columna, devolver un mensaje de error
            if (columnasCriterios.isEmpty()) {
                System.out.println( "Error: No se encontraron las columnas especificadas en los criterios.");
            } else {
                // Recorrer las filas desde la siguiente a la de encabezado
                for (int rowIndex = headerIndex + 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        Map<String, Object> filaDatos = new HashMap<>();
                        for (Map.Entry<String, Integer> entry : columnasCriterios.entrySet()) { // por cada fila se guardan los datis en los idices que nos interesan
                            Cell cell = row.getCell(entry.getValue());
                            Object valor = obtenerValorCelda(cell); // Método para extraer el valor correctamente
                            filaDatos.put(entry.getKey(), valor);
                        }
                        datosFiltrados.add(filaDatos);
                    }
                }
            }
            // Aquí puedes devolver un mensaje de éxito o hacer otra operación con 'datosFiltrados'

        }

        return  datosFiltrados;
    }*/


