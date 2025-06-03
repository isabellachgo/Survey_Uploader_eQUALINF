package com.upm.etsiinf.backend.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que se encarga de leer el archivo.
 */
@Service
public class FilePreviewService {
    /**
     * Constructir por defecto.No realiza ninguna operación.
     */
    public FilePreviewService() {

    }

    /**
     * Previsualiza el contenido de un archivo CSV subido por el usuario.
     *  <p>
     *  Lee las  filas del archivo y devuelve su contenido como una lista de mapas, donde cada mapa representa una fila del CSV
     *  con sus pares clave-valor (nombre de columna → valor de celda).
     * @param file atchivo a previsualizar
     * @return  Lista de mapas representando las filas del archivo.
     * @throws IOException excepcion
     */
    public List<Map<String, String>> previsualizeCSV(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (InputStreamReader isr = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8); //lector de caracteres de archivo
             CSVReader reader = new CSVReaderBuilder(isr).withCSVParser(parser).build()) { //lector de csv
            // Leer cabecera
            String[] headersLine = reader.readNext();
            if (headersLine == null) {
                return rows; // CSV vacío
            }
            List<String> headers = new ArrayList<>(); // lista con los encabezados
            for (String header : headersLine) {
                headers.add(header.trim());
            }

            // Leer filas
            String[] values;
            while ((values = reader.readNext()) != null) {
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String val = i < values.length ? values[i] : "";
                    rowMap.put(headers.get(i), val);
                }
                rows.add(rowMap);
            }
        } catch (CsvValidationException e) {
            throw new IOException("Error validando CSV", e);
        }
        return rows;
    }

    /**
     * Previsualiza todas las hojas de un archivo Excel con Apache POI.
     * <p>
     * Para cada hoja, se construye una lista de mapas, donde cada mapa representa una fila del Excel
     * como pares clave-valor (nombre de columna → valor de celda).
     * @param file archivo a previsualizar
     * @return Un mapa donde cada clave es el nombre de una hoja, y su valor es la lista de filas representadas como mapas.
     * @throws IOException excepcion
     */
    public Map<String, List<Map<String, String>>> previsualizeExcelAllSheets(MultipartFile file) throws IOException {
        Map<String, List<Map<String, String>>> sheetsData = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            for (Sheet sheet : wb) {
                try {
                    List<Map<String, String>> sheetPreview = processSheet(sheet);
                    sheetsData.put(sheet.getSheetName(), sheetPreview);
                } catch (IllegalArgumentException e) {
                    // Cabecera no encontrada, se ignora la hoja
                    System.out.println("Hoja ignorada por falta de cabecera: " + sheet.getSheetName());
                }
            }
        }
        return sheetsData;
    }

    /**
     * Comprueba si la información de una hoja tiene un formato correcto (tabular).
     * @param sheetData hoja a comprobar
     * @return  {@code true} si es una hoja válida; {@code false} en caso contrario.
     */
    public static boolean isValidSheet(List<Map<String, String>> sheetData) {
        // Es válida si tiene al menos una fila de datos
        return sheetData != null && !sheetData.isEmpty();
    }

    /**
     * Lee una hoja de excel.
     * Para cada fila guarda en un mapanpares clave-valor (nombre de columna → valor de celda).
     * @param sheet hoja a comprobar
     * @return Lista de mapas representando las filas del archivo.
     */
    private List<Map<String, String>> processSheet(Sheet sheet) {
        List<Map<String, String>> data = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        int headerRowIndex = detectHeader(sheet);
        if (headerRowIndex < 0) {
            throw new IllegalArgumentException("No se encontró fila de encabezado en hoja: " + sheet.getSheetName());
        }

        // Obtener nombres de columna
        Row headerRow = sheet.getRow(headerRowIndex);
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            headers.add(formatter.formatCellValue(cell).trim());
        }

        // Leer filas de datos
        for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Map<String, String> rowData = new LinkedHashMap<>();
            boolean anyValue = false;
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String value = formatter.formatCellValue(cell).trim();
                if (!value.isEmpty()) anyValue = true;
                rowData.put(headers.get(c), value);
            }
            if (anyValue) data.add(rowData);
        }
        return data;
    }

    /**
     * Busca la fila donde empieza el encabezado del excel, ya que estos suelen traer información y/o isntrucciones antes.
     * <p> compara cada fila con la siguiente, evaluando si la fila actual podría representar un encabezado y si la siguiente contiene datos
     * @param sheet hoja del archivo
     * @return Indice de la fila que contiene los encabezados.
     */
    private int detectHeader(Sheet sheet) {
        int maxCols = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                maxCols = Math.max(maxCols, row.getLastCellNum()); //compara una fila con la siguiente
            }
        }
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Row next = sheet.getRow(i + 1);
            if (row != null && next != null && isValidHeaderRow(row, next, maxCols)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Verifica que una fila es un encabezado.
     * <p> La fila de encabezados contiene más textos y la fila que le sigue más datos.
     * @param row fila candidata
     * @param nextRow siguiente fila
     * @param cols numero maximo de columnas que tienen las filas
     * @return {@code true} si es uuna fila de encabezado válida {@code false} en caso contrario.
     */
    private boolean isValidHeaderRow(Row row, Row nextRow, int cols) {
        int textCount = 0, numCount = 0;
        for (int c = 0; c < cols; c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
                textCount++;
            } else if (cell.getCellType() == CellType.NUMERIC) {
                numCount++;
            }
        }
        int nextText = 0, nextNum = 0;
        for (int c = 0; c < cols; c++) {
            Cell cell = nextRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
                nextText++;
            } else if (cell.getCellType() == CellType.NUMERIC) {
                nextNum++;
            }
        }
        return textCount > 1 && textCount >= numCount && nextNum >= nextText; // Si la fila candidata a encabezado tiene al menos dos celdas con texto, y tiene mas celdas de texto que datos númericos
                                                                              // la fila siguiente contiene más datos númericos que descriptivos
    }
}
