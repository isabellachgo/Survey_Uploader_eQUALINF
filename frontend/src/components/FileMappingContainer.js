
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {getProcesses, getAttributes, getIndicators, getFileData, getSheetData, uploadMapping
} from "../api/ApiConnector";
import {ProcessSelector, AttributeSelector, DateSelector, SheetSelector, DataTable, SelectedColumnsPanel, UploadFeedback, ActionButtons, AcademicYearColumnSelector,
} from "./FileMappingPanel";
import "../styles/styles.css";

const FileMappingContainer = () => {
    const { fileId } = useParams();
    const navigate = useNavigate();
    const [processes, setProcesses] = useState([]);
    const [attributes, setAttributes] = useState([]);
    const [indicadores, setIndicadores] = useState([]);
    const [possibleValues, setPossibleValues] = useState([]);
    const [sheetNames, setSheetNames] = useState([]);
    const [data, setData] = useState([]);
    const [rowsToShow, setRowsToShow] = useState(5);
    const [selectedProcess, setSelectedProcess] = useState("");
    const [selectedAttribute, setSelectedAttribute] = useState("");
    const [selectedPossibleValue, setSelectedPossibleValue] = useState("");
    const [selectedSheet, setSelectedSheet] = useState("");
    const [selectedDate, setSelectedDate] = useState("");
    const [selectedColumns, setSelectedColumns] = useState({});
    const [columnToIndicadorMap, setColumnToIndicadorMap] = useState({});
    const [uploadResults, setUploadResults] = useState([]);
    const highlightColors = ["#b5d8f6", "#f6aa7e", "#ece681", "#732166",
        "#D1C4E9", "#2d8554", "#e88383", "#b6cd99",
        "#254585", "#FFCDD2"
    ];
    const [errorMessage, setErrorMessage]=useState({});
    const [hasSubmitted, setHasSubmitted] = useState(false);
    const [selectedAcademicYearColumn, setSelectedAcademicYearColumn] = useState("");


    // Cargar procesos, indicadores, atributos, etc
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

    // Una vez cargados los procesos, cargar indicadores
    useEffect(() => {
        if (selectedProcess) {
            getIndicators(selectedProcess)
                .then(setIndicadores)
                .catch(console.error);
        } else {
            setIndicadores([]);
        }
    }, [selectedProcess]);

    // Al cambia de hoja llamar al metodo de la clase de comunicaciones
    useEffect(() => {
        if (selectedSheet) {
            getSheetData(fileId, selectedSheet)
                .then(setData)
                .catch(console.error);
        }
    }, [fileId, selectedSheet]);

    // Handlers


    // Manejo de selección de columnas al hacer clic en el encabezado
    const handleHeaderClick = (colName) => {
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



    const showMore = () => setRowsToShow(n => Math.min(n + 5, data.length));
    const showLess = () => setRowsToShow(n => (n - 5 >= 5 ? n - 5 : 5));
    const goBack = () => navigate(-1);

    const handleSubmit = () => {
        setHasSubmitted(true); // ← Marca que el usuario ha intentado enviar
        const errors = {};

      //  if (!selectedAttribute) errors.selectedAttribute = "Campo obligatorio";
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

        if (Object.keys(errors).length > 0) return;

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
            .then(setUploadResults)
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
                            onValueChange={setSelectedPossibleValue}
                            error={errorMessage.selectedPossibleValue}
                            data={data}
                        />
                        <DateSelector
                            selectedDate={selectedDate}
                            onChange={setSelectedDate}
                            error={errorMessage.selectedDate}
                        />

                        <AcademicYearColumnSelector
                            data={data}
                            selectedYear={selectedAcademicYearColumn}
                            onChange={setSelectedAcademicYearColumn}
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
                            onIndicatorChange={(col, val) => setColumnToIndicadorMap(prev => ({ ...prev, [col]: val }))}
                            onRemoveColumn={removeSelectedColumn}
                            error={errorMessage.columnToIndicadorMap}
                            hasSubmitted={hasSubmitted}
                        />
                    </>
                ) : (
                    <div className="info-process">
                        <p>Por favor, selecciona un proceso para continuar.</p>
                    </div>
                )}
                <UploadFeedback uploadResults={uploadResults} />
                <ActionButtons
                    onBack={goBack}
                    onSubmit={handleSubmit}
                    disableSubmit={!selectedProcess}
                />


            </div>
        </div>
    );
};

export default FileMappingContainer;
