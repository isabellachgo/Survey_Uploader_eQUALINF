package com.upm.etsiinf.backend;

import java.text.Normalizer;

public class Normalization {
    public Normalization() {}

    /**
     * Método para normalizar texto eliminando acentos y espacios extra de un CSV.
     */
    public static String normalizarTexto(String texto) {
        texto = texto.toLowerCase().trim();

        // Convertimos caracteres especiales a su versión base (pero preservamos la "ñ")
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("(?<!n)\\p{M}", ""); // Quita tildes sin afectar "ñ"

        // Eliminamos caracteres invisibles y BOM
        texto = texto.replaceAll("[^\\p{ASCII}]", "").replaceAll("\\uFEFF", "");

        return texto;
    }
}
