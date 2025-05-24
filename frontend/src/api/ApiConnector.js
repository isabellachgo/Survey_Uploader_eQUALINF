const BASE_URL = "http://localhost:8080/file"; //conexión con el backend.

/**
 * Envía al backend el archivo proporcionado para su procesamiento o almacenamiento.
 * @param {File} file - Archivo que se desea subir al backend.
 * @returns {Promise<any>} Promesa que se resuelve con la respuesta del backend tras subir el archivo.
 */

export async function uploadRawFile(file) {
    // Creamos un formadata para enviar el archivo como multipart
    const formData = new FormData();
    formData.append("file", file); // Añadimos el archvio al formulario con la clave fila

    try {
        // Enviamos la petición POST usando fetch al endpoint correspondiente
        const res = await fetch(`${BASE_URL}/upload`, {
            method: "POST",
            body: formData,
        });

        // Respuesta exitosa
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        return await res.json();
    } catch (error) {
        // caso error
        throw {
            status: "error",
            message: error.message || "Error de red o respuesta inválida del servidor",
        };
    }
}
/**
 * Realiza una petición al backend para obtener la lista de procesos disponibles.
 * @returns {Promise<any>} Promesa que se resuelve con la lista de procesos disponibles.
 */

export async function getProcesses() {
    const res = await fetch(`${BASE_URL}/processes`);
    if (!res.ok) throw new Error("Error al cargar procesos");
    return res.json();
}

/**
 * Realiza una petición al backend para obtener los datos asociados a un archivo previamente cargado.
 * @param {string} fileId - ID del archivo del cual se desean obtener los datos.
 * @returns {Promise<any>} Promesa que se resuelve con los datos del archivo.
 */

export async function getFileData(fileId) {
    const res = await fetch(`${BASE_URL}/${fileId}`);
    if (!res.ok) throw new Error(`Error al cargar el archivo con ID ${fileId}`);
    return res.json();
}

/**
 * Realiza una petición al backend para obtener la lista de indicadores asociados a un proceso específico.
 * @param {string} processId - ID del proceso del cual se desean obtener los indicadores.
 * @returns {Promise<any>} Promesa que se resuelve con la lista de indicadores del proceso.
 */

export async function getIndicators(processId) {
    const res = await fetch(`${BASE_URL}/processes/${processId}/indicators`);
    if (!res.ok) throw new Error(`Error al cargar los indicadores del proceso con ID ${processId}`);
    return res.json();
}

/**
 * Realiza una petición al backend para obtener la lista de atributos disponibles para el mapeo de datos.
 * @returns {Promise<string[]>} Promesa que se resuelve con un array de nombres de atributos disponibles.
 */

export async function getAttributes() {
    const res = await fetch(`${BASE_URL}/attributes`);
    if (!res.ok) throw new Error("Error al cargar los archivos");
    return res.json();
}

/**
 * Obtiene los datos de una hoja específica de un archivo previamente cargado.
 * @param {string} fileId - ID del archivo del que se desea obtener la hoja.
 * @param {string} sheetName - Nombre de la hoja dentro del archivo (aplicable a archivos Excel).
 * @returns {Promise<any>} Promesa que se resuelve con los datos de la hoja solicitada.
 */

export async function getSheetData(fileId, sheetName) {
    const url = new URL(`${BASE_URL}/${fileId}/sheet`);
    url.searchParams.append("nombreHoja", sheetName);
    const res = await fetch(url);
    if (!res.ok) throw new Error(`Error al cargar la hoja con nombre ${sheetName}`);
    return res.json();
}

/**
 * Envía al backend los datos necesarios para actualizar los indicadores con la información procesada del archivo.
 * @param {string} fileId - ID del archivo cargado previamente.
 * @param {string} sheetName - Nombre de la hoja del archivo (en caso de archivos Excel con múltiples hojas).
 * @param {Object} mapping - Objeto que contiene la relación entre indicadores y columnas del archivo.
 * @param {string} process - Tipo de proceso al que pertenecen los indicadores a actualizar.
 * @param {string} date - Fecha de actualización o ejecución del proceso.
 * @param {string} attribute - Nombre del atributo .
 * @param {string} possibleValue - Valor específico del atributo para filtrar o actualizar.
 * @param {string} academicYearColumn - Nombre de la columna que contiene los años académicos.
 * @returns {Promise<any>} Promesa que se resuelve con la respuesta del backend tras actualizar los indicadores.
 */

export async function uploadMapping({ fileId, sheetName, mapping, process, date, attribute, possibleValue , academicYearColumn}) { // Recibe un unico objeto con todos esos parametro
    const form = new FormData();
    form.append("fileId", fileId);
    form.append("nombreHoja", sheetName);
    form.append("mapeoColumnas", JSON.stringify(mapping));
    form.append("process", process);
    form.append("date", typeof date === "string" ? date : date.toISOString());
    form.append("attribute", attribute);
    form.append("possibleValue", possibleValue);
    form.append("academicYearColumn", academicYearColumn);

    const res = await fetch(`${BASE_URL}/updateInd`, { method: "POST", body: form });
    if (!res.ok) throw new Error(`Error al subir el mapeo: ${res.status}`);
    return res.json();
}