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


@Service
public class FilePreviewService {

    /**
     * Previsualiza un CSV usando OpenCSV y devuelve lista de mapas header→valor,
     * preservando el orden de las columnas tal cual aparecen en el archivo.
     */
    public List<Map<String, String>> previsualizeCSV(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        try (
                InputStreamReader isr = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVReader reader = new CSVReaderBuilder(isr)
                        .withCSVParser(parser)
                        .build()
        ) {
            // Leer cabecera
            String[] headersLine = reader.readNext();
            if (headersLine == null) {
                return rows; // CSV vacío
            }
            List<String> headers = new ArrayList<>();
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
     */
    public Map<String, List<Map<String, String>>> previsualizeExcelAllSheets(MultipartFile file) throws IOException {
        Map<String, List<Map<String, String>>> sheetsData = new LinkedHashMap<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            for (Sheet sheet : wb) {
                sheetsData.put(sheet.getSheetName(), processSheet(sheet));
            }
        }
        return sheetsData;
    }

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
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
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

    private int detectHeader(Sheet sheet) {
        int maxCols = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                maxCols = Math.max(maxCols, row.getLastCellNum());
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
        return textCount > 1 && textCount >= numCount && nextNum >= nextText;
    }
}
