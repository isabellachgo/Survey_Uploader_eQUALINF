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

    private final Map<String, JdbcTemplate> yearTemplate = new HashMap<>();

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

    public JdbcTemplate getJdbcTemplate(String year) {
        return yearTemplate.get(year);
    }
}
