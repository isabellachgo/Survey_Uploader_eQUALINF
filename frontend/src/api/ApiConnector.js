const BASE_URL = "http://localhost:8080/file";


export function uploadRawFile(file) {
    const formData = new FormData();
    formData.append("file", file);

    return new Promise((resolve, reject) => {
        const request = new XMLHttpRequest();
        request.open("POST", `${BASE_URL}/upload`, true);

        request.onload = () => {
            if (request.status === 200) {
                try {
                    const response = JSON.parse(request.responseText);
                    resolve(response);
                } catch {
                    reject({ status: "error", message: "Respuesta inválida del servidor" });
                }
            } else {
                reject({ status: "error", message: `HTTP ${request.status}` });
            }
        };

        request.onerror = () => reject({ status: "error", message: "Error de red" });

        request.send(formData);
    });
}


// Fetch processes
export async function getProcesses() {
    const res = await fetch(`${BASE_URL}/processes`);
    if (!res.ok) throw new Error("Error al cargar procesos");
    return res.json();
}

// Fetch file data by ID
export async function getFileData(fileId) {
    const res = await fetch(`${BASE_URL}/${fileId}`);
    if (!res.ok) throw new Error(`Error al cargar el archivo con ID ${fileId}`);
    return res.json();
}

// Fetch indicators for a process
export async function getIndicators(processId) {
    const res = await fetch(`${BASE_URL}/processes/${processId}/indicators`);
    if (!res.ok) throw new Error(`Error al cargar los indicadores del proceso con ID ${processId}`);
    return res.json();
}

// Fetch all attributes
export async function getAttributes() {
    const res = await fetch(`${BASE_URL}/attributes`);
    if (!res.ok) throw new Error("Error al cargar los archivos");
    return res.json();
}

// Fetch values for an attribute
export async function getPossibleValues(attributeId) {
    const res = await fetch(`${BASE_URL}/attributes/${attributeId}/valores`);
    if (!res.ok) throw new Error(`Error al cargar los posibles valores para el atributo con ID ${attributeId}`);
    return res.json();
}

// Fetch sheet data
export async function getSheetData(fileId, sheetName) {
    const url = new URL(`${BASE_URL}/${fileId}/sheet`);
    url.searchParams.append("nombreHoja", sheetName);
    const res = await fetch(url);
    if (!res.ok) throw new Error(`Error al cargar la hoja con nombre ${sheetName}`);
    return res.json();
}

// Upload mapping
export async function uploadMapping({ fileId, sheetName, mapping, process, date, attribute, possibleValue , academicYearColumn}) {
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