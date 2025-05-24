import React from 'react';

/**
 * Componente selector de procesos. Permite al usuario seleccionar un proceso de una lista disponible.
 *
 * @param processes - Lista de procesos disponibles para seleccionar.
 * @param selectedProcess - ID del proceso actualmente seleccionado.
 * @param onChange - Función que se ejecuta al cambiar la selección.
 * @returns {JSX.Element} Elemento JSX que representa el selector de procesos.
 * @constructor
 */
export function ProcessSelector({ processes, selectedProcess, onChange }) {
    return (
        <div className="process-selector">
            <label htmlFor="processSelect">Proceso: *</label>
            <select
                id="processSelect"
                value={selectedProcess}
                onChange={e => onChange(e.target.value)}
                className="process-select"
            >
                <option value="">-- Seleccione un proceso --</option>
                {processes.map(p => (
                    <option key={p.id} value={p.id}>
                        {p.process_name}
                    </option>
                ))}
            </select>

        </div>
    );
}

/**
 * Componente selector de atributos y valores posibles. Permite al usuario elegir un atributo y, en función de este,
 * seleccionar uno de sus valores asociados.
 *
 * @param attributes - Lista de nombres de atributos disponibles.
 * @param selectedAttribute - Atributo actualmente seleccionado.
 * @param onAttributeChange - Función que se ejecuta al cambiar el atributo seleccionado.
 * @param possibleValues - Lista de valores posibles asociados al atributo seleccionado.
 * @param selectedValue - Valor actualmente seleccionado.
 * @param onValueChange - Función que se ejecuta al cambiar el valor seleccionado.
 * @param error - Indica si hay un error de validación en la selección.
 * @param data - Información adicional relacionada con la selección (puede usarse para mostrar contexto o validaciones).
 * @returns {JSX.Element} Elemento JSX que representa el selector de atributos y valores.
 * @constructor
 */
export function AttributeSelector({attributes, selectedAttribute, onAttributeChange, possibleValues, selectedValue, onValueChange,error, data}) {
    return (
        <div className="attribute-selector">
            <div className="attribute-group">
                <label htmlFor="attributeSelect" >
                    Atributo:
                </label>

                <div className="select-with-tooltip">
                    <select
                        id="attributeSelect"
                        value={selectedAttribute}
                        onChange={e => onAttributeChange(e.target.value)}
                        className="attribute-select"
                    >
                        <option value="">-- Seleccione un atributo --</option>
                        {attributes.map(a => (
                            <option key={a.id} value={a.id}>
                                {a.coding} - {a.description}
                            </option>
                        ))}
                    </select>

                    <div className="tooltip-container">
                        <span className="tooltip-icon">?</span>
                        <div className="tooltip-text">
                            El atributo detalla las características de un grupo de indicadores.
                        </div>
                    </div>
                </div>
            </div>

            {
        selectedAttribute && (
            <div className="possible-value-group">
                <label htmlFor="possibleValueSelect">Columna con los valores del atributo: *</label>
                <select
                    id="possibleValueSelect"
                    value={selectedValue}
                    onChange={e => onValueChange(e.target.value)}
                    //className="value-select"
                    className={`value-select ${error ? "input-error" : ""}`}
                >
                    <option value="">-- Seleccione una columna --</option>
                    {data.length > 0 && Object.keys(data[0]).map((col, index) => (
                        <option key={index} value={col}>
                            {col}
                        </option>
                    ))}
                </select>
                <div className="tooltip-container">
                    <span className="tooltip-icon">?</span>
                    <div className="tooltip-text">Debes indicar en cúal columna de la tabla previsulizada, se ecuentran los valores del atributo que seleccionaste.
                    </div>
                </div>
                {error && <p className="error-text">{error}</p>}
            </div>
        )}
        </div>
    );
}

/**
 * Componente selector de fecha. Permite al usuario seleccionar una fecha válida mediante un campo calendario.
 * @param selectedDate - Fecha actualmente seleccionada (en formato ISO o compatible).
 * @param onChange - Función que se ejecuta al cambiar la fecha seleccionada.
 * @param error - Indica si existe un error de validación en la selección de la fecha.
 * @returns {JSX.Element} Elemento JSX que representa el selector de fecha.
 */
export function DateSelector({selectedDate, onChange, error}) {
    return (
        <div className="date-selector">
            <label htmlFor="dateInput">Fecha de subida: *</label>
            <input
                type="date"
                id="dateInput"
                value={selectedDate}
                onChange={e => onChange(e.target.value)}
                className={`date-input ${error ? "input-error" : ""}`}
            />
            <div className="tooltip-container">
                <span className="tooltip-icon">?</span>
                <div className="tooltip-text">La fecha de subida, será la que aparezca como fecha de última modificación en la base de datos.
                </div>
            </div>
            {error && <p className="error-text">{error}</p>}
        </div>
    );
}

/**
 * Componente selector de Año Académico. Permite al usuario seleccionar un año académico disponible en los datos cargados.
 * @param data - Lista de años académicos extraídos del archivo.
 * @param selectedYear - Año académico actualmente seleccionado.
 * @param onChange - Función que se ejecuta al cambiar el año académico seleccionado.
 * @param error - Indica si hay un error de validación relacionado con la selección.
 * @returns {JSX.Element} Elemento JSX que representa el selector de año académico.
 */
export function AcademicYearColumnSelector({data, selectedYear, onChange, error}) {
    return (
        <div className="academic-year-selector">
            <label htmlFor="academicYearSelect">Columna con los años académicos: *</label>
            <select
                id="academicYearSelect"
                value={selectedYear}
                onChange={(e) => onChange(e.target.value)}
                className={`academic-year-select ${error ? "input-error" : ""}`}
            >
                <option value="">-- Seleccione una columna --</option>
                {data.length > 0 &&
                    Object.keys(data[0]).map((col) => (
                        <option key={col} value={col}>
                            {col}
                        </option>
                    ))}
            </select>
            <div className="tooltip-container">
                <span className="tooltip-icon">?</span>
                <div className="tooltip-text"> Selecciona el nombre de la columna de la tabla previsuaizada,  que contiene los años académicos.
                </div>
            </div>
            {/* Mensaje de error visual */}
            {error && <p className="error-text">{error}</p>}
        </div>
    );
}

/**
 * Componente selector de hoja. Permite al usuario seleccionar una hoja específica de un archivo Excel que contiene múltiples hojas.
 * @param sheetNames - Lista de nombres de hojas disponibles en el archivo.
 * @param selectedSheet - Nombre de la hoja actualmente seleccionada.
 * @param onChange - Función que se ejecuta al cambiar la hoja seleccionada.
 * @returns {false | JSX.Element} Elemento JSX que representa el selector de hoja, o `false` si no se debe renderizar.
 */
export function SheetSelector({sheetNames, selectedSheet, onChange}) {
    return (
        sheetNames.length > 1 && ( // solo muestra el selector si hay más de una hoja
            <div className="sheet-selector">
            <label htmlFor="sheetSelect">Hoja del Excel a procesar:</label>
                <select
                    id="sheetSelect"
                    value={selectedSheet}
                    onChange={e => onChange(e.target.value)}
                    className="sheet-select"
                >
                    {sheetNames.map((sheetName) => (
                        <option key={sheetName} value={sheetName}>
                            {sheetName}
                        </option>
                    ))}
                </select>
            </div>
        )
    );
}

/**
 * Componente de tabla que muestra los datos cargados y permite seleccionar columnas mediante clics en los encabezados.
 * Incluye funcionalidad para mostrar más o menos filas.
 * @param data - Datos tabulares a mostrar, en formato de matriz (filas y columnas).
 * @param rowsToShow - Número de filas que se deben mostrar inicialmente.
 * @param selectedColumns - Lista de nombres de columnas que han sido seleccionadas por el usuario.
 * @param onHeaderClick - Función que se ejecuta al hacer clic en un encabezado de columna.
 * @param showMore - Función que se ejecuta al pulsar el botón "Ver más" para mostrar más filas.
 * @param showLess - Función que se ejecuta al pulsar el botón "Ver menos" para mostrar menos filas.
 * @returns {JSX.Element} Elemento JSX que representa la tabla interactiva.
 */
export function DataTable({data, rowsToShow, selectedColumns, onHeaderClick, showMore, showLess}) {
    if (!data || !data.length) return <p>Cargando previsualización.</p>;
    const headers = Object.keys(data[0]);
    return (
        <>
            <div className="column-instructions">
                <div className="label-with-tooltip">
                    <label htmlFor="dateInput">
                        Haz clic en las columnas que deseas mapear: *
                    </label>
                    <div className="tooltip-container">
                        <span className="tooltip-icon">?</span>
                        <div className="tooltip-text">
                            Haz clic sobre las columnas que deseas mapear, después el el apartado de "Columnas seleccionadas"
                            debes seleccionae a qué indicadores de la
                            base de datos corresponde cada columna.
                        </div>
                    </div>
                </div>
            </div>
            <div className="table-container">
                <table className="w-full">
                    <thead>
                    <tr>
                        {headers.map(col => (
                            <th
                                key={col}
                                onClick={() => onHeaderClick(col)}
                                style={{
                                    backgroundColor: selectedColumns[col]
                                        ? selectedColumns[col] + '50'
                                        : 'inherit',
                                    border: selectedColumns[col]
                                        ? `4px solid ${selectedColumns[col]}`
                                        : '1px solid #ccc',
                                    cursor: 'pointer'
                                }}
                            >
                                {col}
                            </th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {data.slice(0, rowsToShow).map((row, rIdx) => (
                        <tr key={rIdx}>
                            {headers.map(col => (
                                <td
                                    key={col}
                                    onClick={() => onHeaderClick(col)}
                                    style={{cursor: 'pointer'}}
                                >
                                    {row[col]}
                                </td>
                            ))}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <div className="button-container">
                {rowsToShow < data.length && (
                    <button className="show-more-button" onClick={showMore}>
                        Ver más
                    </button>
                )}
                {rowsToShow > 5 && (
                    <button className="show-less-button" onClick={showLess}>
                        Ver menos
                    </button>
                )}
            </div>

        </>
    );
}

/**
 * Componente que muestra las columnas seleccionadas por el usuario junto con sus indicadores asignados.
 * Permite modificar los indicadores asignados a cada columna y eliminar columnas seleccionadas.
 *
 * @param selectedColumns - Lista de nombres de columnas que han sido seleccionadas.
 * @param columnToIndicadorMap - Mapeo entre nombres de columnas y los identificadores de indicadores asignados.
 * @param indicadores - Lista de indicadores disponibles para asignar a las columnas.
 * @param onIndicatorChange - Función que se ejecuta al cambiar el indicador asignado a una columna.
 * @param onRemoveColumn - Función que se ejecuta al eliminar una columna seleccionada.
 * @param error - Indica si hay errores de validación relacionados con la asignación de indicadores.
 * @param hasSubmitted - Indica si el formulario principal ha sido enviado (para mostrar errores en caso necesario).
 * @returns {JSX.Element|null} Elemento JSX del panel de columnas seleccionadas o `null` si no hay columnas seleccionadas.
 */
export function SelectedColumnsPanel({selectedColumns, columnToIndicadorMap, indicadores, onIndicatorChange, onRemoveColumn, error, hasSubmitted}) {
    if (!Object.keys(selectedColumns).length && !error) return null;
    return (
        <div className="selected-columns-container">
            <h3 className="selected-columns-title">Columnas Seleccionadas:</h3>
            <div className="selected-columns">
                {Object.entries(selectedColumns).map(([col, color]) => (
                    <div
                        key={col}
                        className="selected-column-item"
                        style={{borderLeft: `30px solid ${color}`}}
                    >
                        <span className="column-label">{col}</span>
                        <div className="tooltip-container">
                            <span className="tooltip-icon">?</span>
                            <div className="tooltip-text">
                               Selecciona a que indicador de la base de datos, quieres subir la columna seleccionada.
                            </div>
                        </div>
                        <select
                            className={`indicador-dropdown ${hasSubmitted && !columnToIndicadorMap[col] ? "input-error" : ""}`}
                            value={columnToIndicadorMap[col] || ""}
                            onChange={e => onIndicatorChange(col, e.target.value)}
                        >
                            <option value="">-- Selecciona indicador --</option>
                            {indicadores.map(ind => (
                                <option key={ind.id} value={ind.indicator_name}>
                                    {ind.indicator_name}
                                </option>
                            ))}
                        </select>
                        <span className="remove-tag" onClick={() => onRemoveColumn(col)}>
                         &#x2715;
                        </span>
                    </div>
                ))}
                {error && (
                    <p className="error-text">{error}</p>
                )}
            </div>
        </div>
    );
}

/**
 * Componente que muestra el resultado del proceso de subida de archivo, incluyendo posibles errores o confirmaciones.
 *
 * @param uploadResults - Objeto con el resultado de la subida, incluyendo estado, mensaje y detalles opcionales.
 * @returns {JSX.Element|null} Elemento JSX que representa el feedback de la subida, o `null` si no hay resultados que mostrar.
 * @constructor
 */
export function UploadFeedback({uploadResults}) {
    if (!uploadResults || !uploadResults.length) return null;
    return (
        <div className="upload-feedback">
            <div className="success-message">
                <img src="/icons/check.png" alt="Éxito" className="success-icon"/>
                <span>Las columnas han sido subidas!</span>
            </div>
            <div className="section-title-with-tooltip">
                <h3>Resumen de subida:</h3>
                <div className="tooltip-container">
                    <span className="tooltip-icon">?</span>
                    <div className="tooltip-text">
                        Esta tabla muestra el resultado del intento de subir datos a la base de datos:<br/>
                        <strong>Sede de datos</strong>: Base de datos a la que se intenta subir esa fila.<br/>
                        <strong>Columna</strong>: Columna del archivo seleccionada.<br/>
                        <strong>Valor</strong>: Valor del atributo que se intenta subir al indicador.<br/>
                        <strong>Indicador</strong>: Indicador de la base de datos que se intenta modificar.<br/>
                        <strong>Filas afectadas</strong>: Número de registros procesados.<br/>
                        <strong>Estado / Error</strong>: Resultado del proceso o mensaje de error si ha fallado.
                    </div>
                </div>
            </div>
            <table className="results-table">
                <thead>
                <tr>
                    <th>Sede de datos</th>
                    <th>Columna</th>
                    <th>Valor</th>
                    <th>Indicador</th>
                    <th>Filas afectadas</th>
                    <th>Estado / Error</th>
                </tr>
                </thead>
                <tbody>
                {uploadResults.map((r, i) => (
                    <tr key={i} className={r.success ? 'row-success' : 'row-fail'}>
                        <td>{r.year || '-'}</td>
                        <td>{r.column || '-'}</td>
                        <td>{r.value ?? '-'}</td>
                        <td>{r.indicator || '-'}</td>
                        <td>{r.rowsUpdated}</td>
                        <td>
                            {r.success ? (
                                <span className="flex items-center">
                                  <img
                                      src="/icons/check.png"
                                      alt="Marca de verificación"
                                      className="w-5 h-5 text-green-500 flex-shrink-0"
                                  />
                                  <span className="ml-2">OK</span>
                                </span>) : (
                                <span className="flex items-center">
                                  <img
                                      src="/icons/red-cross.png"
                                      alt="Marca de error"
                                      className="w-5 h-5 text-red-500 flex-shrink-0"
                                  />
                                  <span className="ml-2">{r.errorMessage}</span>
                                </span>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

/**
 * Componente que representa los botones de navegación y envío del formulario.
 * Incluye lógica para desactivar el botón de envío y mostrar estado de carga si es necesario.
 *
 * @param onBack - Función que se ejecuta al pulsar el botón "Volver".
 * @param onSubmit - Función que se ejecuta al pulsar el botón "Enviar".
 * @param disableSubmit - Indica si el botón de envío debe estar deshabilitado.
 * @param  uploading - Indica si hay un proceso de subida en curso.
 * @returns {JSX.Element} Elemento JSX que representa los botones del formulario.
 */
export function ActionButtons({onBack, onSubmit, disableSubmit, uploading}) {
    return (
        <div className="go-back-container">
            <button className="goBack-button" onClick={onBack}>
                Volver
            </button>
            {!disableSubmit && (
                <button className="subir-button" onClick={onSubmit}>
                    Subir
                </button>
            )}
        </div>
    );
}
