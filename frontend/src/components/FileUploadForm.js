import React, { useState, useRef, useEffect } from "react";
import "../styles/styles.css";
import { useNavigate } from "react-router-dom";

const FileUploadForm = () => {
  const [files, setFiles] = useState([]);
  const navigate = useNavigate();
  const dropArea = useRef(null);
  const fileSelectorInput = useRef(null);

  /*useEffect(() => {
    const onDragOverWindow = e => e.preventDefault();
    const onDropWindow    = e => {  e.preventDefault(); };
    document.addEventListener("dragover", onDragOverWindow, { capture: true });
    document.addEventListener("drop",    onDropWindow,     { capture: true });
    return () => {
      document.removeEventListener("dragover", onDragOverWindow, { capture: true });
      document.removeEventListener("drop",    onDropWindow,     { capture: true });
    };
  }, []);*/




  const addFileToState = (file) => {
    const newFile = {name: file.name, size: (file.size / 1024).toFixed(2), file, progress: 0, status: "pending", errorMsg: null,};
    setFiles([newFile]);
    uploadFile(newFile);
  };

  const handleFileSelected = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (typeValidation(file)) addFileToState(file);
    else alert("Solo se permiten archivos CSV, XLS o XLSX.");
  };

  const handleDragOver = e => {
    e.preventDefault();
    e.dataTransfer.dropEffect = "copy";
    dropArea.current.classList.add("drag-over-effect");
  };
  const handleDragEnter = e => {
    e.preventDefault();
    e.dataTransfer.dropEffect = "copy";
    e.dataTransfer.effectAllowed = "copy";
    dropArea.current.classList.add("drag-over-effect");
  };



  const handleDrop = (e) => {
    // Evitamos el comportamiento por defecto (abrir el archivo en el navegador)
    e.preventDefault();
    //e.stopPropagation();
    dropArea.current.classList.remove("drag-over-effect");// Quitamos efecto visual de \"drag-over\" cuando el archivo es soltado
    console.log("🗂️ Drop en zona:", e.dataTransfer.files);
    let file = null;
    if (e.dataTransfer.items) {
      for (const item of e.dataTransfer.items) {      // Recorremos los items arrastrados
        if (item.kind === "file") { // Solo nos interesan los items que sean archivos
          const f = item.getAsFile();
          if (f && typeValidation(f)) {  // Validamos el tipo de archivo (CSV, XLS o XLSX)
            file = f; // Guardamos el primer archivo válido encontrado
            break;    // Terminamos la búsqueda, no necesitamos más
          }
        }
      }
    } else {
      // Si `dataTransfer.items` no existe (por compatibilidad), usamos `dataTransfer.files`
      for (const f of e.dataTransfer.files) {
        // Validamos el tipo de archivo
        if (typeValidation(f)) {
          file = f; // Guardamos el primer archivo válido encontrado
          break;
        }
      }
    }

    // Si encontramos un archivo válido, lo agregamos al estado
    if (file) {
      addFileToState(file);
    }
    // Si no hay archivos válidos pero sí hay archivos arrastrados, mostramos alerta
    else if (e.dataTransfer.files.length > 0) {
      alert("Solo se permiten archivos CSV, XLS o XLSX.");
    }
  };



  const handleDragLeave = e => {
    e.preventDefault();
    dropArea.current.classList.remove("drag-over-effect");
  };
  const handleFileDelete = (fileName) => {
    setFiles((prev) => prev.filter((f) => f.name !== fileName));
  };

  const handleUpload = () => {
    const file = files[0];
    if (file && file.fileId) navigate(`/file/${file.fileId}`, { state: { parsedData: file.parsedData },});
  };

  const iconSelector = (fileName) => { // escoge el icono segun el archivo
    if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) return "xls.png";
    else return "csv.png";
  };
  const uploadFile = (archivo) => {
    setFiles([{ ...archivo, status: "uploading", progress: 0, errorMsg: null }]);  // Sobrescribimos el estado de archivos, indicando que el archivo está en estado "uploading"

    const formData = new FormData();
    formData.append("file", archivo.file); //atributo a pasar al controller

    const request = new XMLHttpRequest();
    request.open("POST", "http://localhost:8080/file/upload", true); //inicializamos la request al endpoint al que nos conectaremos

    // Evento que se dispara mientras se va subiendo el archivo
    request.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        const percentCompleted = Math.round((e.loaded * 100) / e.total);
        // Actualizar el progreso de subida del archivo que se está cargando.
        // Como solo manejamos un archivo, actualizamos directamente el primer (y único) elemento.
        setFiles(prev => [{...prev[0], progress: percentCompleted}]);

      }
    };
    // Evento que se dispara cuando la petición termina (servidor respondió)
    request.onload = () => {
      if (request.status === 200) {
        let respuesta;
        try {
          respuesta = JSON.parse(request.responseText);        //  parsear la respuesta del servidor
        } catch {
          // Si el servidor envió algo que no es JSON válido, marcamos error
          return setFiles((prev) => prev.map((f) => f.name === archivo.name ? { ...f, status: "error", errorMsg: "Respuesta inválida del servidor", progress: 0 } : f)); //Copia el único archivo y actualiza su estado a error, su mensaje de error y su progreso
        }
        if (respuesta.fileId) { // se guardo correctamente el archivo y te devolvio su id
          setFiles((prev) =>
              prev.map((f) => f.name === archivo.name ? {...f, status: "success", progress: 100, fileId: respuesta.fileId, parsedData: respuesta.parsedData,} : f)); //Copia el único archivo y actualiza su estado a exito, y su progreso
        } else { // Si falta el fileId en la respuesta, consideramos que hubo un error
          setFiles((prev) => prev.map((f) => f.name === archivo.name ? { ...f, status: "error", errorMsg: "No se recibió fileId", progress: 0 } : f)); //Copia el único archivo y actualiza su estado a error, su mensaje de error y su progreso
        }
      } else {       // Si el estado HTTP no fue 200, marcamos error con el código recibido
        setFiles((prev) => prev.map((f) => f.name === archivo.name ? { ...f, status: "error", errorMsg: `HTTP ${request.status}`, progress: 0 } : f)); //Copia el único archivo y actualiza su estado a error, su mensaje de error y su progreso
      }
    };

    // Evento que se dispara si ocurre un error de red
    request.onerror = () => {
      setFiles((prev) => prev.map((f) => f.name === archivo.name ? { ...f, status: "error", errorMsg: "Error de red", progress: 0 } : f));
    };

    // enviamos el archivo al servidor
    request.send(formData);
  };
  const typeValidation = (file) => { // comprueba si el tipo de archivo es valido
    const allowedTypes = [
      "text/csv",
      "application/vnd.ms-excel",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    ];
    return allowedTypes.includes(file.type);
  };
  return (
      <div className="file-upload-container">
        <div className="file-uploader">
          <div className="header-section">
            <h1>Subir archivos</h1>
            <p>Sube el archivo que quieras procesar</p>
            <p>CSV, XLS y XLSX están permitidos.</p>
          </div>
          <div
              ref={dropArea}
              className="drop-section"
              onDragEnter={handleDragEnter}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
          >
          <div className="col">
              <div className="upload-icon">
                <img src="icons/upload_icon.png" alt="" width="70" height="70" />
              </div>
              <span>Arrastra y suelta tu archivo aquí</span>
              <span>o</span>
              <button onClick={() => fileSelectorInput.current.click()}>
                Seleccionar archivo
              </button>
              <input
                  ref={fileSelectorInput}
                  type="file"
                  style={{ display: "none" }}
                  onChange={handleFileSelected}
              />
          </div>
            <div className="col">
              <div className="drop-here">Suelta aquí</div>
            </div>
          </div>
          <div className="list-section" style={{ display: files.length > 0 ? "block" : "none" }}>
            <div className="list-title">Archivo subido</div>
            <ul className="list">
              {files.map((file) => (
                  <li key={file.name}>
                    <div className="col">
                      <img src={`icons/${iconSelector(file.name)}`} alt=""/>
                    </div>
                    <div className="col">
                      <div className="file-name">
                        <div className="name">{file.name}</div>
                        <span>{file.progress}%</span>
                      </div>
                      <div className="file-progress">
                        <span style={{width: `${file.progress}%`}}></span>
                      </div>
                      <div className="file-size">{file.size} KB</div>
                    </div>

                      <div className="icons">
                        <div className="cross">
                          <img
                              src="/icons/delete.png"
                              alt="Borrar archivo"
                              className="cross-icon"
                              onClick={() => handleFileDelete(file.name)}
                          />
                        </div>
                        {file.status === 'success' && (
                            <div className="check">
                              <img
                                  src="/icons/check.png"
                                  alt="Archivo subido"
                                  className="check-icon"
                              />
                            </div>
                        )}
                        {file.status === 'error' && (
                            <span className="error-message">{file.errorMsg}</span>
                        )}
                      </div>


                  </li>
              ))}
            </ul>
          </div>
        </div>
        {files.length > 0 && files[0].status === 'success' && (
            <div className="button-upload-container">
              <button className="nav-button" onClick={handleUpload}>
                Procesar
              </button>
            </div>
        )}
      </div>
  );
};

export default FileUploadForm;
