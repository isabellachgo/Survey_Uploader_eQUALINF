import React from "react";

/**
 * Componente panel de subida de archivo. Permite al usuario seleccionar, arrastrar, eliminar y subir archivos.
 * Incluye una zona interactiva de arrastre y controles personalizados.
 *
 * @param files - Lista de archivos seleccionados para subir.
 * @param dropArea - Referencia al área de arrastre de archivos.
 * @param fileSelectorInput - Referencia al input tipo file para selección manual.
 * @param handleFileSelected - Función que se ejecuta cuando se seleccionan archivos manualmente.
 * @param handleDrop - Función que se ejecuta al soltar archivos en el área de arrastre.
 * @param handleFileDelete - Función que elimina un archivo de la lista.
 * @param handleUpload - Función que inicia el proceso de subida de archivos al backend.
 * @param iconSelector - Función que devuelve un icono JSX basado en el tipo de archivo.
 * @returns Elemento JSX que representa el panel de subida de archivos.
 */

const FileUploadPanel = ({files, dropArea, fileSelectorInput, handleFileSelected, handleDrop, handleFileDelete, handleUpload, iconSelector}) => {
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
                    onDragEnter={e => { e.preventDefault(); dropArea.current.classList.add("drag-over-effect"); }}
                    onDragOver={e => { e.preventDefault(); dropArea.current.classList.add("drag-over-effect"); }}
                    onDragLeave={e => { e.preventDefault(); dropArea.current.classList.remove("drag-over-effect"); }}
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
                                    <img src={`icons/${iconSelector(file.name)}`} alt="" />
                                </div>
                                <div className="col">
                                    <div className="file-name">
                                        <div className="name">{file.name}</div>
                                        <span>{file.progress}%</span>
                                    </div>
                                    <div className="file-progress">
                                        <span style={{ width: `${file.progress}%` }}></span>
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

export default FileUploadPanel;
