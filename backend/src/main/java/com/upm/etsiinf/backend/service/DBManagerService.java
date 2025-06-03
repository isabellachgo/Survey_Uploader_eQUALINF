package com.upm.etsiinf.backend.service;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio encargado de establecer conexion con todas las bases de datos, segun el anioo academico.
 */
@Service
public class DBManagerService {

    private final Map<String, JdbcTemplate> yearTemplate = new HashMap<>();

    /**
     * Lee las credenciales de conexión desde el archivo de propiedades llamado db-config.properties.
     * En este archivo se especifica, para cada año académico, la URL de conexión, el nombre de usuario y la contraseña correspondientes.
     * A partir de esta información, se instancia un objeto {@code JdbcTemplate} por año,
     *  que luego se almacena en un mapa {@code Map<String, JdbcTemplate>}.
     * para acceder dinámicamente a la base de datos deseada.
     * @throws IOException excepcion
     */
    public DBManagerService() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("db-config.properties"));
        Set<String> years = props.stringPropertyNames().stream().map(key -> key.split("\\.")[1]).collect(Collectors.toSet());
        for (String year : years) {
            String url = props.getProperty("db." + year + ".url");
            String username = props.getProperty("db." + year + ".username");
            String password = props.getProperty("db." + year + ".password");

            DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            yearTemplate.put(year, jdbcTemplate);
        }
    }

    /**
     * Obtiene la plantilla de conexión asociada al año correspondiente.
     * @param year año academico
     * @return plantilla de conexión a la base de datos del año 'year'
     */
    public JdbcTemplate getJdbcTemplate(String year) {
        return yearTemplate.get(year);
    }
}
