package com.upm.etsiinf.backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FileStorageService {

    private final Map<String, List<Map<String, String>>> storage = new HashMap<>();   // Almacenamiento para archivos CSV

    private final Map<String, Map<String, List<Map<String, String>>>> excelStorage = new HashMap<>();  //Almacenamiento para archivos Excel con múltiples hojas

    public void saveCSVFile(String fileId, List<Map<String, String>> data) {
        storage.put(fileId, data);
    }

    public List<Map<String, String>> getCSVFile(String fileId) {
        return storage.get(fileId);
    }

    public void saveExcelFile(String fileId, Map<String, List<Map<String, String>>> data) {
        excelStorage.put(fileId, data);
    }

    public Map<String, List<Map<String, String>>> getExcelFile(String fileId) {
        return excelStorage.get(fileId);
    }

}
