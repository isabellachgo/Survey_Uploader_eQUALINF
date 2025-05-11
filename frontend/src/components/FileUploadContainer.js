import React, { useState, useRef, useEffect } from "react";
import "../styles/styles.css";
import FileUploadPanel from "./FileUploadPanel";
import {uploadRawFile} from "../api/ApiConnector";
import { useNavigate } from "react-router-dom";

const FileUploadContainer = () => {
  const [files, setFiles] = useState([]);
  const navigate = useNavigate();
  const dropArea = useRef(null);
  const fileSelectorInput = useRef(null);

  useEffect(() => {
    const onDragOverWindow = e => e.preventDefault();
    const onDropWindow    = e => {  e.preventDefault(); };
    document.addEventListener("dragover", onDragOverWindow, { capture: true });
    document.addEventListener("drop",    onDropWindow,     { capture: true });
    return () => {
      document.removeEventListener("dragover", onDragOverWindow, { capture: true });
      document.removeEventListener("drop",    onDropWindow,     { capture: true });
    };
  }, []);




  const addFileToState = async file => {
    const newFile = {
      name: file.name,
      size: (file.size / 1024).toFixed(2),
      file,
      progress: 0,
      status: "uploading",
      errorMsg: null,
    };
    setFiles([newFile]);

    try {
      const result = await uploadRawFile(file);
      if (result.fileId) {
        setFiles([{
          ...newFile,
          progress: 100,
          status: "success",
          fileId: result.fileId,
          parsedData: result.parsedData,
        }]);
      } else {
        setFiles([{ ...newFile, status: "error", errorMsg: "No se recibió fileId" }]);
      }
    } catch (err) {
      setFiles([{ ...newFile, status: "error", errorMsg: err.message }]);
    }
  };

  const handleFileSelected = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (typeValidation(file)) addFileToState(file);
    else alert("Solo se permiten archivos CSV, XLS o XLSX.");
  };
  const iconSelector = fileName => {
    if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) return "xls.png";
    return "csv.png";
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

  const handleFileDelete = (fileName) => {
    setFiles((prev) => prev.filter((f) => f.name !== fileName));
  };

  const handleUpload = () => {
    const file = files[0];
    if (file && file.fileId) navigate(`/file/${file.fileId}`, { state: { parsedData: file.parsedData },});
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
      <FileUploadPanel
          files={files}
          dropArea={dropArea}
          fileSelectorInput={fileSelectorInput}
          handleFileSelected={handleFileSelected}
          handleDrop={handleDrop}
          handleFileDelete={handleFileDelete}
          handleUpload={handleUpload}
          iconSelector={iconSelector}
      />
  );
};

export default FileUploadContainer;