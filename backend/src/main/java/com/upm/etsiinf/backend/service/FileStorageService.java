package com.upm.etsiinf.backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Servicio que se encarga de alamcenar los datos del archivo subido.
 */
@Service
public class FileStorageService {
    /**
     * Constructir por defecto.No realiza ninguna operación.
     */
    public FileStorageService(){

    }

    private final Map<String, List<Map<String, String>>> storage = new HashMap<>();   // Almacenamiento para archivos CSV
    private final Map<String, Map<String, List<Map<String, String>>>> excelStorage = new HashMap<>();  //Almacenamiento para archivos Excel con múltiples hojas

    /**
     * Guarda los datos de un archivo tipo CSV (id, lista de mapas con pares (nombreColumna, valor)).
     * @param fileId identificador del archivo
     * @param data lista de mapas con sus pares clave-valor (nombre de columna → valor de celda).
     */
    public void saveCSVFile(String fileId, List<Map<String, String>> data) {
        storage.put(fileId, data);
    }

    /**
     * Devuelve la información almacenada de un CVS a través de su identificador.
     * @param fileId  identificador del archivo
     * @return lista de mapas con pares (nombreColumna, valor)
     */
    public List<Map<String, String>> getCSVFile(String fileId) {
        return storage.get(fileId);
    }

    /**
     * Guarda los datos de un archivo excel. Un mapa donde cada clave es el nombre de una hoja, y su valor es la lista de filas representadas como mapas.
     * @param fileId identificador del archivo
     * @param data Un mapa donde cada clave es el nombre de una hoja, y su valor es la lista de filas representadas como mapas.
     */
    public void saveExcelFile(String fileId, Map<String, List<Map<String, String>>> data) {
        excelStorage.put(fileId, data);
    }

    /**
     * Devuelve la información almacenada de un Excel a través de su identificador.
     * @param fileId  identificador del archivo
     * @return Un mapa donde cada clave es el nombre de una hoja, y su valor es la lista de filas representadas como mapas.
     */
    public Map<String, List<Map<String, String>>> getExcelFile(String fileId) {
        return excelStorage.get(fileId);
    }

}
