package com.upm.etsiinf.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Constructor de la aplicación
 */
@SpringBootApplication
public class BackendApplication {
	/**
	 * Constructor por defecto. No realiza ninguna operación.
	 */
	public BackendApplication() {
	}
	/**
	 * Punto de entrada principal de la aplicación Spring Boot.
	 *
	 * @param args Argumentos pasados por línea de comandos (no utilizados en este caso).
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
