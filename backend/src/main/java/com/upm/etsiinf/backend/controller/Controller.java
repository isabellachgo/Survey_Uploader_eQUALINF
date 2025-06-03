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
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.*;
/**
 * Controlador de la aplicación, se comunica con el frontend y con los distintos servicios del backend.
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/file")
public class Controller {

    private final FileProcessingService fileProcessingService;
    private final DatabaseService databaseService;
    private final FilePreviewService previsualizationService;
    private final FileStorageService fileStorageService;

    /**
     * Constructor del controlador principal de la aplicación. Inyecta los servicios necesarios
     * para el procesamiento, previsualización, almacenamiento y gestión de datos en la plataforma.
     *
     * @param fileProcessingService Servicio encargado de procesar los datos subidos.
     * @param databaseService Servicio que gestiona el acceso y actualización de la base de datos.
     * @param previsualizationService Servicio que permite la previsualización de archivos (CSV/Excel).
     * @param fileStorageService Servicio que gestiona el almacenamiento de archivos.
     */
    @Autowired
    public Controller(FileProcessingService fileProcessingService, DatabaseService databaseService,
                      FilePreviewService previsualizationService, FileStorageService fileStorageService) {
        this.fileProcessingService = fileProcessingService;
        this.databaseService = databaseService;
        this.previsualizationService = previsualizationService;
        this.fileStorageService = fileStorageService;

    }

    /**
     * Maneja la subida de un archivo CSV o Excel (XLS/XLSX) para su previsualización y almacenamiento temporal.
     * <p>
     * Según el tipo de archivo, se aplica un procesamiento distinto:
     * <ul>
     *     <li><b>CSV:</b> Se parsea y guarda como una lista de filas con pares (atributo, valor).</li>
     *     <li><b>Excel:</b> Se procesan todas las hojas. Solo se incluyen aquellas que contienen datos válidos.
     *         Si no hay hojas válidas, se responde con un error 400.</li>
     * </ul>
     *
     * @param file Archivo subido por el usuario. Debe ser de tipo CSV, XLS o XLSX.
     * @return Respuesta HTTP con un mapa que puede contener:
     * <ul>
     *     <li><b>fileId:</b> ID generado para referenciar el archivo.</li>
     *     <li><b>parsedData:</b> Datos previsualizados (solo de la primera hoja válida en Excel).</li>
     *     <li><b>sheetNames:</b> Lista de hojas válidas (solo en archivos Excel).</li>
     * </ul>
     * En caso de error, se devuelve un mensaje apropiado con el código HTTP correspondiente.
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
                Map<String, List<Map<String, String>>> sheetsData = previsualizationService.previsualizeExcelAllSheets(file); // Guarda el nombre de la hoja y su previsualizacion (nombre, valor)
                fileStorageService.saveExcelFile(fileId, sheetsData); // lo guardo para poder luego trbajar sobre el
                // Filtro hojas válidas
                Map<String, List<Map<String, String>>> validSheets = new LinkedHashMap<>();
                for (Map.Entry<String, List<Map<String, String>>> entry : sheetsData.entrySet()) {
                    if (FilePreviewService.isValidSheet(entry.getValue())) {
                        validSheets.put(entry.getKey(), entry.getValue());
                        System.out.println("Hoja valida: " + entry.getKey());
                    }
                }
                if (validSheets.isEmpty()) {
                    System.out.println("Archivo sin hojas válidas");
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "No hay hojas válidas para previsualizar."
                    );



                }

                // Usar la primera hoja válida como predeterminada
                String defaultSheet = validSheets.keySet().iterator().next();
                response.put("parsedData", validSheets.get(defaultSheet));
                response.put("sheetNames", new ArrayList<>(validSheets.keySet()));
            } else {
                throw new IllegalArgumentException("Formato de archivo no soportado. Solo se admiten CSV y Excel.");
            }
            response.put("fileId", fileId);
            System.out.println("Archivo guardado con exito con ID: " + fileId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            // Trato de errores esperados (como falta de hojas válidas)
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", true, "message", "No hay hojas válidas"));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Método recuperar los datos  de un fichero previamente almacenado.
     * El archivo puede ser de tipo CSV o Excel (XLS/XLSX).
     * <p>
     * Si se trata de un archivo CSV, se devuelve directamente su contenido como una lista de mapas.
     * Si es un archivo Excel, se devuelve el contenido de la primera hoja junto con los nombres de todas las hojas disponibles.
     * @param fileId Identificador único del archivo que se desea recuperar.
     * @return Respuesta HTTP con los datos del archivo:
     *         <ul>
     *             <li>Para CSV: lista de filas con sus valores.</li>
     *             <li>Para Excel: un mapa con la hoja por defecto ("parsedData") y los nombres de todas las hojas ("sheetNames").</li>
     *             <li>404 si no se encuentra ningún archivo con ese ID.</li>
     *         </ul>
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
     * Recupera los datos de un archivo previamente subido para su previsualización.
     *
     * @param fileId Identificador único del archivo del que se desea obtener una vista previa.
     * @param sheetName Nombre de la hoja (en archivos Excel) que se desea previsualizar. Puede ser nulo si el archivo no es un Excel.
     * @return Respuesta HTTP que contiene la tabla de datos de la hoja especificada, lista para ser mostrada en la interfaz.
     */

    @GetMapping("/{fileId}/sheet")
    public ResponseEntity<List<Map<String, String>>> getSheetData(@PathVariable("fileId") String fileId,
                                                                  @RequestParam("nombreHoja") String sheetName) {
        // Recupero la info de las hojas de ese fichero
        Map<String, List<Map<String, String>>> sheetsData = fileStorageService.getExcelFile(fileId);  // Recuperas la info del fichero guardado
        if (sheetsData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<Map<String, String>> sheetData = sheetsData.get(sheetName);        // Recuperas info de la hoja pedida
        if (sheetData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        System.out.println("Se cambió a la hoja " + sheetName);
        return ResponseEntity.ok(sheetData);
    }




    /**
     * Devuelve la lista de procesos existentes en la bbdd.
     <p>
     * Este método consulta la base de datos correspondiente al año por defecto y devuelve
     * una lista de procesos.
     *
     * @return Respuesta HTTP con una lista de mapas que representan los procesos obtenidos.
     *         Si ocurre un error durante la consulta, se devuelve un error 500 (Internal Server Error).
     */
    @GetMapping("/processes") /*pasarle el atributo del año en el que estas */
    public ResponseEntity<List<Map<String, Object>>> getProcesos() {
        try {
            List<Map<String, Object>> procesos = databaseService.getProcesses();
            System.out.println("Se obtienen los procesos: "+ procesos);
            return ResponseEntity.ok(procesos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Devuelve la lista de indicadores de un proceso especifico existentes en la bbdd por defecto
     <p>
     * Este método consulta la base de datos correspondiente al año por defecto y devuelve
     * una lista de indicadores, asociados a un proceso.
     * @param processId Proceso del cual se quieren saber sus indicadores asociados.
     * @return Respuesta HTTP con una lista de mapas que representan los indicadores obtenidos.
     *   Si ocurre un error durante la consulta, se devuelve un error 500 (Internal Server Error).
     */
    @GetMapping("/processes/{processId}/indicators")
    public ResponseEntity<List<Map<String, Object>>> getIndicadoresByProcess(@PathVariable("processId") int processId) {
        try {
            List<Map<String, Object>> indicadores = databaseService.getIndicatorsbyProcess(processId);
            System.out.println("Se obtienen los indicadores: " + indicadores + " asociados al proceso: " + processId);
            return ResponseEntity.ok(indicadores);
        }catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * Devuelve la lista de atributos existentes en la bbdd por defecto.
     <p>
     * Este método consulta la base de datos correspondiente al año por defecto y devuelve
     * una lista de atributos.
     * @return Respuesta HTTP con una lista de mapas que representan los atributos obtenidos.
     *   Si ocurre un error durante la consulta, se devuelve un error 500 (Internal Server Error).
     */
    @GetMapping("/attributes")
    public ResponseEntity<List<Map<String, Object>>> getAtributos() {
        try {
            List<Map<String, Object>> atributos = databaseService.getAttributes();
            return ResponseEntity.ok(atributos);
        }catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Procesa un archivo previamente subido (CSV o Excel), aplica un mapeo de columnas
     * y actualiza los valores del indicador correspondiente en la base de datos.
     * <p>
     * El archivo se identifica mediante su ID (`fileId`) y puede contener una o varias hojas (en caso de Excel).
     * Se espera un mapeo en formato JSON que relacione las columnas del archivo con los indicadores del sistema.
     * La información se filtra por año académico antes de ser cargada en el indicator_instance de la base de datos.
     *
     * @param fileId ID del archivo previamente subido al sistema.
     * @param nombreHoja Nombre de la hoja a procesar (solo se aplica si el archivo es Excel).
     * @param mapeoColumnasJson Mapeo de columnas en formato JSON, donde cada clave representa una columna del archivo y su valor el indicador del sistema.
     * @param processId ID del proceso relacionado con los indicadores que se quieren actualizar.
     * @param date Fecha de referencia para registrar la carga en la base de datos.
     * @param attribute Atributo vinculados a los indicadores que se quieren actualizar.
     * @param possibleValue Valor posible del atributo.
     * @param academicYearColumn Nombre de la columna del archivo que contiene los años académicos, usada para conectarse a la base de datos que corresponda.
     * @return Una respuesta HTTP con la lista de resultados de la operación.
     *         Cada resultado (`UpdateResult`) indica si la actualización fue exitosa, cuántas filas fueron afectadas y posibles mensajes de error.
     *         En caso de error global, se devuelve un único `UpdateResult` con el mensaje correspondiente.
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
            Map<String, String> mapeoColumnas = objectMapper.readValue(mapeoColumnasJson, new TypeReference<Map<String, String>>() {});
            List<Map<String, Object>> resultado = null;

            // Intentar recuperar datos preprocesados (CSV).
            List<Map<String, String>> csvData = fileStorageService.getCSVFile(fileId);
            if (csvData != null) {
                resultado = fileProcessingService.dataFilter(csvData, mapeoColumnas,academicYearColumn);
            } else {
                // Si no es CSV, probar con Excel.
                Map<String, List<Map<String, String>>> excelData = fileStorageService.getExcelFile(fileId);
                if (excelData != null) {
                    // Si no se especificó la hoja, usar la primera posible.
                    if (nombreHoja == null || nombreHoja.trim().isEmpty()) {
                        nombreHoja = excelData.keySet().iterator().next();
                    }
                    List<Map<String, String>> sheetData = excelData.get(nombreHoja);
                    resultado = fileProcessingService.dataFilter(sheetData, mapeoColumnas,academicYearColumn);
                }
            }

            if (resultado == null) {
                throw new IllegalArgumentException("No se pudieron recuperar datos para el fileId: " + fileId);
            }
            // Actualizar indicator_instance usando el mapeo y los datos filtrados.
           List<UpdateResult> res= databaseService.updateIndicatorInstance(processId, mapeoColumnas, resultado, date, attribute, possibleValue, academicYearColumn);
            System.out.println("Datos subidos con éxito ");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
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
            return ResponseEntity.badRequest().body(Collections.singletonList(errorResult));
        }
    }


}


