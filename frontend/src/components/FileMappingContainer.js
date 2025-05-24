import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {getProcesses, getAttributes, getIndicators, getFileData, getSheetData, uploadMapping
} from "../api/ApiConnector";
import {ProcessSelector, AttributeSelector, DateSelector, SheetSelector, DataTable, SelectedColumnsPanel, UploadFeedback, ActionButtons, AcademicYearColumnSelector,
} from "./FileMappingPanel";
import "../styles/styles.css";

/**
 * Contenedor principal para el mapeo de columnas con atributos e indicadores.
 * Coordina la visualización de la tabla, la selección de columnas y la asignación de metadatos.
 * @returns {JSX.Element}
 * @constructor
 */
const FileMappingContainer = () => {
    const { fileId } = useParams(); // Fichero subido
    const navigate = useNavigate();
    const [processes, setProcesses] = useState([]); // lista de procesos cargada del backend
    const [attributes, setAttributes] = useState([]); // lista de atributos cargada del backend
    const [indicadores, setIndicadores] = useState([]); // lista de indicadores cargada del backend
    const [possibleValues, setPossibleValues] = useState([]); //posibles valores del atributo cargada del backend
    const [sheetNames, setSheetNames] = useState([]); // nombre de hojas (para archivos tipo Excel) cargada del backend
    const [data, setData] = useState([]); // fecha de subida.
    const [rowsToShow, setRowsToShow] = useState(5); // Filas a enseñar de la tabla (5)
    const [selectedProcess, setSelectedProcess] = useState(""); // proceso seleccionado
    const [selectedAttribute, setSelectedAttribute] = useState(""); //atributo seleccionado
    const [selectedPossibleValue, setSelectedPossibleValue] = useState(""); //posible valor seleccionado
    const [selectedSheet, setSelectedSheet] = useState(""); // hoja seleccionada
    const [selectedDate, setSelectedDate] = useState(""); // fecha seleccioanda
    const [selectedColumns, setSelectedColumns] = useState({}); // columnas seleccionadas
    const [columnToIndicadorMap, setColumnToIndicadorMap] = useState({}); // mapeo (columna -> indicador)
    const [uploadResults, setUploadResults] = useState([]); // resultado de la subida
    const highlightColors = ["#b5d8f6", "#f6aa7e", "#ece681", "#732166", // colores para identificar columnas
        "#D1C4E9", "#2d8554", "#e88383", "#b6cd99",
        "#254585", "#FFCDD2"
    ];
    const [errorMessage, setErrorMessage]=useState({}); //Mensaje de error al intentar subir datos.
    const [hasSubmitted, setHasSubmitted] = useState(false); //  se le ha dado al boton de subir
    const [hasfinished, setHasfinished] = useState(false); // indica si ya se subió a las bbdd, los valores  solicitados.
    const [selectedAcademicYearColumn, setSelectedAcademicYearColumn] = useState(""); // columna de la tabla que contiene los años academicos

    /**
     * Cargar procesos, indicadores, atributos, etc
     */
    useEffect(() => {
        Promise.all([
            getProcesses(),
            getAttributes(),
            getFileData(fileId)
        ])
            .then(([pData, aData, fData]) => {
                setProcesses(pData);
                setAttributes(aData);
                if (fData.sheetNames && fData.sheetNames.length > 0) {
                    setSheetNames(fData.sheetNames);
                    setSelectedSheet(fData.sheetNames[0]); //  esta línea selecciona automáticamente la primera hoja
                } else {
                    setData(fData || []);
                }

            })
            .catch(console.error);
    }, [fileId]);

    /**
     *  Una vez cargados los procesos, cargar indicadores
     */
    useEffect(() => {
        if (selectedProcess) {
            getIndicators(selectedProcess)
                .then(setIndicadores)
                .catch(console.error);
        } else {
            setIndicadores([]);
        }
    }, [selectedProcess]);

    /**
     * Al cambia de hoja llamar al metodo de la clase de comunicaciones.
     */
    useEffect(() => {
        if (selectedSheet) {
            getSheetData(fileId, selectedSheet)
                .then(setData)
                .catch(console.error);
        }
    }, [fileId, selectedSheet]);

    /**------------------------------------------- HANDLERS------------------------------------------------ */

    /**
     * Manejo de selección de columnas al hacer clic.
     * @param colName
     */
    const handleHeaderClick = (colName) => {
        setErrorMessage(prev => {
            if (prev.columnToIndicadorMap === "Debe seleccionar al menos una columna para mapear") {
                const newErrors = { ...prev };
                delete newErrors.columnToIndicadorMap;
                return newErrors;
            }
            return prev;
        });
        setSelectedColumns((prevSelected) => {
            if (prevSelected[colName]) { // Si ya estaba seleccionada -> la deseleccionas
                setColumnToIndicadorMap((prevMap) => {// Quitar también la entrada de columnToIndicadorMap
                    const newMap = { ...prevMap }; // clonas objeto
                    delete newMap[colName]; //eliminas el par clave–valor correspondiente a esa columna.
                    return newMap; // Devuelve ese nuevo objeto como nuevo estado
                });
                // Crear un nuevo objeto sin esa propiedad
                const newSelected = { ...prevSelected }; // clonas el estado anterior
                delete newSelected[colName]; //añades el nuevo par clave-valor de la columna
                return newSelected; //Devuelve ese nuevo objeto como nuevo estado
            } else { //No estaba seleccionada -> seleccionar
                const usedColors = Object.values(prevSelected); //Miramos qué colores ya están en uso
                const availableColors = highlightColors.filter(c => !usedColors.includes(c)); //Construimos la lista de libres
                const newColor = availableColors.length > 0 ? availableColors[0] : highlightColors[0]; // Elegimos el primero libre, o si no hay, volvemos a la posición 0
                setColumnToIndicadorMap((prevMap) => ({...prevMap, [colName]: ""})); // Añadir también la entrada en columnToIndicadorMap con valor inicial ""
                return { ...prevSelected, [colName]: newColor }; //Devolver un nuevo objeto que incluye la nueva columna coloreada
            }
        });
    };

    /**
     * Maneja el borrado se una columna del mapeo.
     * @param col
     */
    const removeSelectedColumn = (col) => {
        setSelectedColumns((prev) => {
            const newSelected = { ...prev };
            delete newSelected[col];
            return newSelected;
        });

        setColumnToIndicadorMap((prevMap) => {
            const newMap = { ...prevMap };
            delete newMap[col];
            return newMap;
        });
    };
    /**
     * Manejo del botón para ver más filas.
     */
    const showMore = () => setRowsToShow(n => Math.min(n + 5, data.length));

    /**
     * Manejo del botón para ver menos flas.
     */
    const showLess = () => setRowsToShow(n => (n - 5 >= 5 ? n - 5 : 5));

    /**
     * Vuelve a la vista anterior (subir archivo).
     */
    const goBack = () => navigate(-1);

    /**
     * Recoge los datos mapeados por el usuario (columnas, atributos, indicadores, etc.)
     * Comprueba que no hayan errores y los envía para su validación y procesamiento.
     */
    const handleSubmit = () => {
        setHasSubmitted(true); // Marca que el usuario ha intentado enviar
        // Comprueba si hay errores (por ejemplo: faltan datos obligatorios por rellenar).
        const errors = {};
        if ( selectedAttribute && !selectedPossibleValue) errors.selectedPossibleValue = "Campo obligatorio";
        if (!selectedDate) errors.selectedDate = "Campo obligatorio";
        const mappedCols = Object.entries(columnToIndicadorMap);
        if (!selectedAcademicYearColumn) errors.selectedAcademicYearColumn = "Campo obligatorio";
        if (mappedCols.length === 0) {
            errors.columnToIndicadorMap = "Debe seleccionar al menos una columna para mapear";
        } else {
            const columnasSinIndicador = mappedCols.filter(([, val]) => !val);
            if (columnasSinIndicador.length > 0) {
                errors.columnToIndicadorMap = "Hay columnas seleccionadas sin indicador asignado";
            }
        }
        setErrorMessage(errors);
        // Si hay errores no permite la subida.
        if (Object.keys(errors).length > 0) return;
        // pasa los datos a subir.
        uploadMapping({
            fileId,
            sheetName: selectedSheet,
            mapping: columnToIndicadorMap,
            process: selectedProcess,
            date: selectedDate,
            attribute: selectedAttribute,
            possibleValue: selectedPossibleValue,
            academicYearColumn:selectedAcademicYearColumn
        })
            .then((result) => {
                setUploadResults(result);
                setHasfinished(true);
            })
            .catch(console.error);
    };

    return (
        <div className="table-view-container">
            <div className="prev-card">
                <h1>Procesado y mapeo</h1>
                <ProcessSelector
                    processes={processes}
                    selectedProcess={selectedProcess}
                    onChange={setSelectedProcess}
                />
                {selectedProcess ? (
                    <>
                        <SheetSelector
                            sheetNames={sheetNames}
                            selectedSheet={selectedSheet}
                            onChange={setSelectedSheet}
                        />
                        <AttributeSelector
                            attributes={attributes}
                            selectedAttribute={selectedAttribute}
                            onAttributeChange={setSelectedAttribute}
                            possibleValues={possibleValues}
                            selectedValue={selectedPossibleValue}
                            onValueChange={(value) => {
                                setSelectedPossibleValue(value);

                                setErrorMessage(prev => {
                                    if (prev.selectedPossibleValue && value) {
                                        const newErrors = { ...prev };
                                        delete newErrors.selectedPossibleValue;
                                        return newErrors;
                                    }
                                    return prev;
                                });
                            }}
                            error={errorMessage.selectedPossibleValue}
                            data={data}
                        />
                        <DateSelector
                            selectedDate={selectedDate}
                            onChange={(value) => {
                                setSelectedDate(value);
                                //  limpiar el error
                                setErrorMessage(prev => {
                                    if (prev.selectedDate && value) {

                                        const newErrors = { ...prev };
                                        delete newErrors.selectedDate;
                                        return newErrors;
                                    }
                                    return prev;
                                });
                            }}
                            error={errorMessage.selectedDate}
                        />
                        <AcademicYearColumnSelector
                            data={data}
                            selectedYear={selectedAcademicYearColumn}
                            onChange={(value) => {
                                setSelectedAcademicYearColumn(value);
                                setErrorMessage(prev => {
                                    if (prev.selectedAcademicYearColumn && value) {
                                        const newErrors = { ...prev };
                                        delete newErrors.selectedAcademicYearColumn;
                                        return newErrors;
                                    }
                                    return prev;
                                });
                            }}
                            error={errorMessage.selectedAcademicYearColumn}
                        />
                        <DataTable
                            data={data}
                            rowsToShow={rowsToShow}
                            selectedColumns={selectedColumns}
                            onHeaderClick={handleHeaderClick}
                            showMore={showMore}
                            showLess={showLess}
                        />
                        <SelectedColumnsPanel
                            selectedColumns={selectedColumns}
                            columnToIndicadorMap={columnToIndicadorMap}
                            indicadores={indicadores}
                            onIndicatorChange={(col, val) => {
                                setColumnToIndicadorMap(prev => ({ ...prev, [col]: val }));
                                setErrorMessage(prev => {
                                    if (prev.columnToIndicadorMap?.[col] && val) {
                                        const newMapErrors = { ...prev.columnToIndicadorMap };
                                        delete newMapErrors[col];
                                        const newErrors = { ...prev, columnToIndicadorMap: newMapErrors };

                                        // Si ya no quedan errores en ese objeto, lo borramos por limpieza
                                        if (Object.keys(newMapErrors).length === 0) {
                                            delete newErrors.columnToIndicadorMap;
                                        }
                                        return newErrors;
                                    }
                                    return prev;
                                });
                            }}
                            onRemoveColumn={removeSelectedColumn}
                            error={errorMessage.columnToIndicadorMap}
                            hasSubmitted={hasSubmitted}
                        />
                    </>
                ) : (
                    <div className="info-process">
                        <p> 🛈 Por favor, selecciona un proceso para continuar.</p>
                    </div>
                )}
                <UploadFeedback uploadResults={uploadResults} />
                <ActionButtons
                    onBack={goBack}
                    onSubmit={handleSubmit}
                    disableSubmit={!selectedProcess || hasfinished}
                />
            </div>
        </div>
    );
};

export default FileMappingContainer;
