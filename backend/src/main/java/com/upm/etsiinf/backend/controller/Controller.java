package com.upm.etsiinf.backend.controller;

import com.upm.etsiinf.backend.model.UpdateResult;
import com.upm.etsiinf.backend.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/file")
public class Controller {

    private final FileProcessingService fileProcessingService;
    private final DatabaseService databaseService;
    private final FilePreviewService previsualizationService;
    private final FileStorageService fileStorageService;

    @Autowired
    public Controller(FileProcessingService fileProcessingService, DatabaseService databaseService,
                      FilePreviewService previsualizationService, FileStorageService fileStorageService) {
        this.fileProcessingService = fileProcessingService;
        this.databaseService = databaseService;
        this.previsualizationService = previsualizationService;
        this.fileStorageService = fileStorageService;

    }

    /**
     * Método para guardar info de un archivo tanto CSV como XLS/ XLSX en cuyo caso se muestra la primera hoja.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> handleFileUpload(
            @RequestParam("file") MultipartFile file) {
        try {
            String fileId = UUID.randomUUID().toString();
            Map<String, Object> response = new HashMap<>();
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.endsWith(".csv")) {
                List<Map<String, String>> parsedData = previsualizationService.previsualizeCSV(file);// llamo a previsualización
                fileStorageService.saveCSVFile(fileId, parsedData); // lo guardo para poder luego trbajar sobre el
                response.put("parsedData", parsedData);            // devuelve lista de (nombre atributo, valor)
            } else if (fileName != null && (fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
                // Procesar todas las hojas del Excel
                Map<String, List<Map<String, String>>> sheetsData = previsualizationService.previsualizeExcelAllSheets(file); // Guarda el nombre de la hoja y su previsualizacion
                fileStorageService.saveExcelFile(fileId, sheetsData); // lo guardo para poder luego trbajar sobre el
                // Se establece como hoja por defecto la primera
                String defaultSheet = sheetsData.keySet().iterator().next();
                response.put("parsedData", sheetsData.get(defaultSheet));
                response.put("sheetNames", new ArrayList<>(sheetsData.keySet()));
            } else {
                throw new IllegalArgumentException("Formato de archivo no soportado. Solo se admiten CSV y Excel.");
            }
            response.put("fileId", fileId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Método para  recuperar la previsualizacion de  otra hoja de un excel.
     */
    @GetMapping("/{fileId}/sheet")
    public ResponseEntity<List<Map<String, String>>> getSheetData(@PathVariable("fileId") String fileId,
                                                                  @RequestParam("nombreHoja") String sheetName) {
        // Recupero la info de las hojas de ese fichero
        Map<String, List<Map<String, String>>> sheetsData = fileStorageService.getExcelFile(fileId);  // Recuperas el fichero guardado
        if (sheetsData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<Map<String, String>> sheetData = sheetsData.get(sheetName);        // Recuperas info de la hoja pedida
        if (sheetData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(sheetData);
    }

    /**
     * Método para devolver la info de un fichero previamente almacenado (LLAMA AL FRONNTED PARA PREVISUALIZARLO).
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFileData(@PathVariable("fileId") String fileId) {
        // Intentamos obtener el archivo desde el almacenamiento de CSV con id 'fileId'
        List<Map<String, String>> fileData = fileStorageService.getCSVFile(fileId);
        if (fileData != null) {
            // Si se encontró en CSV, lo devolvemos directamente
            return ResponseEntity.ok(fileData);
        } else {
            // Si no se encontró en CSV, intentamos buscarlo en Excel con id 'fileId'
            Map<String, List<Map<String, String>>> excelData = fileStorageService.getExcelFile(fileId);
            if (excelData != null) {
                String defaultSheet = excelData.keySet().iterator().next(); // Establecemos la hoja por defecto (la primera)
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("parsedData", excelData.get(defaultSheet));
                response.put("sheetNames", new ArrayList<>(excelData.keySet()));
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    /**
     * Devuelve la lista de procesos existentes en la bbdd
     */
    @GetMapping("/processes") /*pasarle el atributo del año en el que estas */
    public ResponseEntity<List<Map<String, Object>>> getProcesos() {
        try {
            List<Map<String, Object>> procesos = databaseService.obtenerProcesos();
            return ResponseEntity.ok(procesos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Devuelve la lista de indicadores de un proceso especifico existentes en la bbdd
     */
    @GetMapping("/processes/{processId}/indicators")
    public ResponseEntity<List<Map<String, Object>>> getIndicadoresByProcess(@PathVariable("processId") int processId) {
        List<Map<String, Object>> indicadores = databaseService.obtenerIndicadoresPorProceso(processId);
        return ResponseEntity.ok(indicadores);
    }


    /**
     * Devuelve la lista de atributos de un proceso especifico existentes en la bbdd
     */
    @GetMapping("/attributes")
    public ResponseEntity<List<Map<String, Object>>> getAtributos() {
        List<Map<String, Object>> atributos= databaseService.obtenerAtributos();
        return ResponseEntity.ok(atributos);
    }

    @GetMapping("/attributes/{attributeId}/valores")
    public ResponseEntity<List<Map<String, Object>>> getAtributoValues(@PathVariable("attributeId") int attributeId) {
        try {
            String sql = "SELECT id, value FROM possible_value WHERE attribute_id = ?";
            List<Map<String, Object>> possibleValues = databaseService.obtenerPossibleValuesPorAtributo(attributeId);
            return ResponseEntity.ok(possibleValues);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * Metodo que sube las columnas seleccionadas al indicador corresondiente en la bbdd.
     */

    @PostMapping("/updateInd")
    public ResponseEntity<List<UpdateResult>> procesarArchivo(
            @RequestParam("fileId") String fileId,
            @RequestParam(value = "nombreHoja", required = false) String nombreHoja,
            @RequestParam("mapeoColumnas") String mapeoColumnasJson,
            @RequestParam("process") String processId,
            @RequestParam("date") Date date,
            @RequestParam("attribute") String attribute,
            @RequestParam("possibleValue") String possibleValue,
            @RequestParam("academicYearColumn") String academicYearColumn)

    {

        try {
            System.out.println("SOLICITUD RECIBIDA-> ");
            System.out.println(" - FileId recibido: " + fileId);
            System.out.println(" - Nombre de la hoja: " + nombreHoja);
            System.out.println(" - JSON recibido (mapeoColumnas): " + mapeoColumnasJson);
            System.out.println(" - Proceso: " + processId);
            System.out.println(" - Fecha: " + date);
            System.out.println(" - Atributo: " + attribute);
            System.out.println(" - PosibleValor: " + possibleValue);
            System.out.println(" - Columna año académico: " + academicYearColumn);


            // Convertir el JSON recibido a un Map.
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> mapeoColumnas = objectMapper.readValue(
                    mapeoColumnasJson, new TypeReference<Map<String, String>>() {});

            List<Map<String, Object>> resultado = null;

            // Intentar recuperar datos preprocesados (CSV).
            List<Map<String, String>> csvData = fileStorageService.getCSVFile(fileId);
            if (csvData != null) {
                resultado = fileProcessingService.filtrarDatos(csvData, mapeoColumnas,academicYearColumn);
            } else {
                // Si no es CSV, probar con Excel.
                Map<String, List<Map<String, String>>> excelData = fileStorageService.getExcelFile(fileId);
                if (excelData != null) {
                    // Si no se especificó la hoja, usar la primera.
                    if (nombreHoja == null || nombreHoja.trim().isEmpty()) {
                        nombreHoja = excelData.keySet().iterator().next();
                    }
                    List<Map<String, String>> sheetData = excelData.get(nombreHoja);
                    resultado = fileProcessingService.filtrarDatos(sheetData, mapeoColumnas,academicYearColumn);
                }
            }

            if (resultado == null) {
                throw new IllegalArgumentException("No se pudieron recuperar datos para el fileId: " + fileId);
            }


// Actualizar indicator_instance usando el mapeo y los datos filtrados.
           List<UpdateResult> res= databaseService.actualizarIndicatorInstance(processId, mapeoColumnas, resultado, date, attribute, possibleValue, academicYearColumn);
            return ResponseEntity.ok(res);
           // return ResponseEntity.ok("Archivo procesado y actualizado en indicator_instance.");
        } catch (Exception e) {
           /* e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());*/
            e.printStackTrace();
            // Construimos un UpdateResult de error genérico
          UpdateResult errorResult = new UpdateResult(
                    /* year */        null,
                    /* column */      null,
                    /* indicator */   null,
                    /* value */       null,
                    /* success */     false,
                    /* rowsUpdated */ 0,
                    /* errorMessage*/ e.getMessage()
            );
            // Devolvemos una lista con un único elemento de error
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonList(errorResult));
        }
    }


}


