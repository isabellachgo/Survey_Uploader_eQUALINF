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

@Service
public class DBManagerService {

    private final Map<String, JdbcTemplate> plantillasPorAnyo = new HashMap<>();

    public DBManagerService() throws IOException {
        Properties props = new Properties();
      //  props.load(new FileInputStream("src/main/resources/db-config.properties"));
        props.load(getClass().getClassLoader().getResourceAsStream("db-config.properties"));

        Set<String> anyos = props.stringPropertyNames().stream()
                .map(key -> key.split("\\.")[1])
                .collect(Collectors.toSet());

        for (String anyo : anyos) {
            String url = props.getProperty("db." + anyo + ".url");
            String username = props.getProperty("db." + anyo + ".username");
            String password = props.getProperty("db." + anyo + ".password");

            DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            plantillasPorAnyo.put(anyo, jdbcTemplate);
        }
    }

    public JdbcTemplate getJdbcTemplate(String anyo) {
        return plantillasPorAnyo.get(anyo);
    }
}
