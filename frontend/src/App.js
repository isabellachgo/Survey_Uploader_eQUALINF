import React from "react";
import FileUploadForm from "./components/FileUploadContainer"; // Importa tu componente
import "./styles/styles.css";
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import FileMappingContainer from "./components/FileMappingContainer";
import  './api/ApiConnector';

function App() {
  return (
    <div className="app-container">
      <Router>
        <Routes>
          <Route path="/" element={<FileUploadForm />} />
          <Route path="/file/:fileId" element={<FileMappingContainer />} />
        </Routes>
      </Router>
    </div>
  ); 
}

export default App;
