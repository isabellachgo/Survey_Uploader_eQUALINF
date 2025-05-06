
import React from 'react';

// Selector de procesos
export function ProcessSelector({ processes, selectedProcess, onChange }) {
    return (
        <div className="process-selector">
            <label htmlFor="processSelect">Selecciona el proceso:</label>
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

// Selector de atributos y valores posibles
export function AttributeSelector({attributes, selectedAttribute, onAttributeChange, possibleValues, selectedValue, onValueChange,attributeError, data}) {
    return (
        <div className="attribute-selector">
            <div className="attribute-group">
                <label htmlFor="attributeSelect">Selecciona el atributo:</label>
                <select
                    id="attributeSelect"
                    value={selectedAttribute}
                    onChange={e => onAttributeChange(e.target.value)}
                    className={`attribute-select ${attributeError ? "input-error" : ""}`}
                >
                    <option value="">-- Seleccione un atributo --</option>
                    {attributes.map(a => (
                        <option key={a.id} value={a.id}>
                            {a.coding} - {a.description}
                        </option>
                    ))}
                </select>
                {attributeError && <p className="error-text">{attributeError}</p>}
            </div>
            {selectedAttribute && (
                <div className="possible-value-group">
                    <label htmlFor="possibleValueSelect">Selecciona la columna con los valores del atributo:</label>
                    <select
                        id="possibleValueSelect"
                        value={selectedValue}
                        onChange={e => onValueChange(e.target.value)}
                        className="value-select"
                    >
                        <option value="">-- Seleccione una columna --</option>
                        {data.length > 0 && Object.keys(data[0]).map((col, index) => (
                            <option key={index} value={col}>
                                {col}
                            </option>
                        ))}
                    </select>
                </div>
            )}
        </div>
    );
}

// Selector de fecha
export function DateSelector({ selectedDate, onChange,error }) {
    return (
        <div className="date-selector">
            <label htmlFor="dateInput">Escoge una fecha:</label>
            <input
                type="date"
                id="dateInput"
                value={selectedDate}
                onChange={e => onChange(e.target.value)}
                className={`date-input ${error ? "input-error" : ""}`}
            />
            {error && <p className="error-text">{error}</p>}
        </div>
    );
}
// Selector de Ano academico
// Selector de Año Académico
export function AcademicYearColumnSelector({ data, selectedYear, onChange, error }) {
    return (
        <div className="academic-year-selector">
            <label htmlFor="academicYearSelect">Selecciona la columna del año académico:</label>
            <select
                id="academicYearSelect"
                value={selectedYear}
                onChange={(e) => onChange(e.target.value)}
            >
                <option value="">-- Seleccione una columna --</option>
                {data.length > 0 &&
                    Object.keys(data[0]).map((col) => (
                        <option key={col} value={col}>
                            {col}
                        </option>
                    ))}
            </select>

            {/* Mensaje de error visual */}
            {error && <p className="error-message">{error}</p>}
        </div>
    );
}


// Selector de hoja
// Selector de hoja
export function SheetSelector({ sheetNames, selectedSheet, onChange }) {
    return (
        sheetNames.length > 1 && ( // solo muestra el selector si hay más de una hoja
            <div className="sheet-selector">
                <label htmlFor="sheetSelect">Selecciona la hoja:</label>
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


// Tabla de datos con selección de columnas
export function DataTable({data, rowsToShow, selectedColumns, onHeaderClick, showMore, showLess}) {
    if (!data || !data.length) return <p>Cargando previsualización.</p>;
    const headers = Object.keys(data[0]);
    return (
        <>
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
                                    style={{ cursor: 'pointer' }}
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
            <div className="column-instructions">
                <p>Haz clic en cualquier celda o encabezado para seleccionar o deseleccionar la columna.</p>
            </div>
        </>
    );
}

// Panel de columnas seleccionadas
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
                        style={{ borderLeft: `30px solid ${color}` }}
                    >
                        <span className="column-label">{col}</span>
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

// Feedback de subida
export function UploadFeedback({ uploadResults }) {
    if (!uploadResults || !uploadResults.length) return null;
    return (
        <div className="upload-feedback">
            <h3>Resumen de subida:</h3>
            <table className="results-table">
                <thead>
                <tr>
                    <th>Base de datos</th><th>Columna</th><th>Indicador</th><th>Valor</th><th>Filas afectadas</th><th>Estado / Error</th>
                </tr>
                </thead>
                <tbody>
                {uploadResults.map((r, i) => (
                    <tr key={i} className={r.success ? 'row-success' : 'row-fail'}>
                        <td>{r.year || '-'}</td>
                        <td>{r.column || '-'}</td>
                        <td>{r.indicator || '-'}</td>
                        <td>{r.value ?? '-'}</td>
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
                                </span>
                                                        ) : (
                                                            <span className="flex items-center">
                                  <img
                                      src="/icons/cross.png"
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

// Botones de navegación
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
