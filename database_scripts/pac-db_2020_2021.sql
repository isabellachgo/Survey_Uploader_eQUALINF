CREATE DATABASE IF NOT EXISTS pac_db_2020_2021;
USE pac_db_2020_2021;



DROP TABLE IF EXISTS linked_document_instance;
DROP TABLE IF EXISTS indicator_instance;
DROP TABLE IF EXISTS linked_evidence_instance;
DROP TABLE IF EXISTS evidence_instance;
DROP TABLE IF EXISTS document_instance;
DROP TABLE IF EXISTS dbstate;
DROP TABLE IF EXISTS inds_group_have_attribs;
DROP TABLE IF EXISTS evis_have_attribs;
DROP TABLE IF EXISTS docs_have_attribs;
DROP TABLE IF EXISTS possible_value;
DROP TABLE IF EXISTS attribute;
DROP TABLE IF EXISTS indicator;
DROP TABLE IF EXISTS indicator_group;
DROP TABLE IF EXISTS origin;
DROP TABLE IF EXISTS linked_evidence;
DROP TABLE IF EXISTS evidence;
DROP TABLE IF EXISTS linked_document;
DROP TRIGGER IF EXISTS check_coding_before_insert;
DROP TABLE IF EXISTS document;
DROP TABLE IF EXISTS process;

CREATE TABLE IF NOT EXISTS user (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Usuario', 'Administrador', 'Super-Administrador') NOT NULL DEFAULT 'Usuario',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS unit (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    unit_name VARCHAR(100) NOT NULL UNIQUE,
    responsible_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (responsible_id) 
        REFERENCES user(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

# Tabla de la relacion n:n entre unidad y usuario
CREATE TABLE IF NOT EXISTS user_belongs_to_unit (
    user_id INT NOT NULL,
    unit_id INT NOT NULL,
    PRIMARY KEY (user_id, unit_id),
    FOREIGN KEY (user_id) 
		REFERENCES user (id)
		ON DELETE CASCADE
        ON UPDATE CASCADE,	
    FOREIGN KEY (unit_id) 
		REFERENCES unit (id)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS process (
	id INT UNIQUE NOT NULL AUTO_INCREMENT,
    type ENUM('Estratégico', 'Clave', 'Soporte') NOT NULL,
	coding VARCHAR(50) NOT NULL UNIQUE,
    process_name VARCHAR(255) NOT NULL,
    responsible_unit_id INT NOT NULL,
    version VARCHAR(50) NOT NULL,
    approval_date DATE NOT NULL, 
    is_subprocess BOOLEAN NOT NULL,
    process_father_id INT,
	PRIMARY KEY (id),
	FOREIGN KEY (responsible_unit_id) 
        REFERENCES unit(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
	FOREIGN KEY (process_father_id) 
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS document (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    type ENUM('Documento', 'Enlace web') NOT NULL,
    coding VARCHAR(255),
    document_name VARCHAR(255) NOT NULL,
    description TEXT,
    responsible_update_id INT NOT NULL,
    process_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (responsible_update_id)
        REFERENCES unit(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
	FOREIGN KEY (process_id) 
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DELIMITER //

CREATE TRIGGER check_coding_before_insert
BEFORE INSERT ON document
FOR EACH ROW
BEGIN
    IF NEW.type = 'Documento' THEN
        IF NEW.coding IS NULL OR NEW.coding = '' THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'La codificación no puede estar vacia';
        END IF;
        IF (SELECT COUNT(*) FROM document WHERE coding = NEW.coding) > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La codificación ya existe';
        END IF;
    END IF;
END //

DELIMITER ;

ALTER TABLE document
ADD CONSTRAINT unique_web_link_name 
UNIQUE (type, document_name);


CREATE TABLE IF NOT EXISTS document_instance (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    type ENUM('Fichero', 'Enlace') NOT NULL,
    coding VARCHAR(255),
    document_name VARCHAR(255) NOT NULL,
    responsible_update_id INT NOT NULL,
    process_id INT NOT NULL,
    modified_date DATE,
    path TEXT,
    valid BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (responsible_update_id)
        REFERENCES unit (id)
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    FOREIGN KEY (process_id)
        REFERENCES process (id)
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS linked_document (
    process_id INT NOT NULL,
    document_id INT NOT NULL,
    PRIMARY KEY (process_id, document_id),
    FOREIGN KEY (process_id)
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (document_id)
        REFERENCES document(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS linked_document_instance (
    process_id INT NOT NULL,
    document_instance_id INT NOT NULL,
    PRIMARY KEY (process_id, document_instance_id),
    FOREIGN KEY (process_id)
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (document_instance_id)
        REFERENCES document_instance(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS evidence (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    coding VARCHAR(255) NOT NULL UNIQUE,
    evidence_name VARCHAR(255) NOT NULL,
    description TEXT,
    process_id INT NOT NULL,
    PRIMARY KEY (id),
	FOREIGN KEY (process_id) 
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS evidence_instance (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    coding VARCHAR(255) NOT NULL UNIQUE,
    evidence_name VARCHAR(255) NOT NULL,
    process_id INT NOT NULL,
    modified_date DATE,
    path TEXT,
    valid BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (process_id)
        REFERENCES process (id)
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS linked_evidence (
    process_id INT NOT NULL,
    evidence_id INT NOT NULL,
    PRIMARY KEY (process_id, evidence_id),
    FOREIGN KEY (process_id) 
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (evidence_id) 
        REFERENCES evidence(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS linked_evidence_instance (
    process_id INT NOT NULL,
    evidence_instance_id INT NOT NULL,
    PRIMARY KEY (process_id, evidence_instance_id),
    FOREIGN KEY (process_id) 
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (evidence_instance_id) 
        REFERENCES evidence_instance(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS origin (
    origin_name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (origin_name)
);

CREATE TABLE IF NOT EXISTS indicator_group (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    coding VARCHAR(255) NOT NULL UNIQUE,
    indicator_group_name VARCHAR(255) NOT NULL,
    process_id INT NOT NULL,
    origin VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
	FOREIGN KEY (process_id) 
        REFERENCES process(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
	FOREIGN KEY (origin) 
        REFERENCES origin(origin_name)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS indicator (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    coding VARCHAR(255) NOT NULL UNIQUE,
    indicator_name VARCHAR(255) NOT NULL,
    description TEXT,
    calc_method VARCHAR(255) NOT NULL,
    period VARCHAR(255) NOT NULL,
    type ENUM('Numérico', 'Porcentaje', 'Booleano', 'Texto plano') NOT NULL,
    standard VARCHAR(50) NOT NULL,
    indicator_group_id INT NOT NULL,
    PRIMARY KEY (id),
	FOREIGN KEY (indicator_group_id) 
        REFERENCES indicator_group(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS indicator_instance (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    coding VARCHAR(255) NOT NULL UNIQUE,
    indicator_name VARCHAR(255) NOT NULL,
    description TEXT,
    calc_method VARCHAR(255) NOT NULL,
    period VARCHAR(255) NOT NULL,
    type ENUM('Numérico', 'Porcentaje', 'Booleano', 'Texto plano') NOT NULL,
    standard VARCHAR(50) NOT NULL,
    indicator_group_id INT NOT NULL,
    modified_date DATE,
    field TEXT,
    valid BOOLEAN NOT NULL,
    PRIMARY KEY (id),
	FOREIGN KEY (indicator_group_id) 
        REFERENCES indicator_group(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS attribute (
    id INT UNIQUE NOT NULL AUTO_INCREMENT,
    coding VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    position INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS possible_value (
	id INT UNIQUE NOT NULL AUTO_INCREMENT,
    value VARCHAR(255) UNIQUE NOT NULL,
    attribute_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (attribute_id) 
        REFERENCES attribute(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS docs_have_attribs (
    document_id INT NOT NULL,
    attribute_id INT NOT NULL,
    PRIMARY KEY (document_id, attribute_id),
    FOREIGN KEY (document_id) 
        REFERENCES document(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (attribute_id)
        REFERENCES attribute(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS evis_have_attribs (
    evidence_id INT NOT NULL,
    attribute_id INT NOT NULL,
    PRIMARY KEY (evidence_id, attribute_id),
    FOREIGN KEY (evidence_id) 
        REFERENCES evidence(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (attribute_id)
        REFERENCES attribute(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS inds_group_have_attribs (
    indicator_group_id INT NOT NULL,
    attribute_id INT NOT NULL,
    PRIMARY KEY (indicator_group_id, attribute_id),
    FOREIGN KEY (indicator_group_id) 
        REFERENCES indicator_group(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (attribute_id)
        REFERENCES attribute(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS dbstate (
	id INT UNIQUE NOT NULL AUTO_INCREMENT,
    state ENUM('Abierto', 'Desplegado', 'Cerrado') NOT NULL,
    version VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO dbstate (id, state, version) VALUES (1, 'Abierto','0');

INSERT INTO attribute (coding, description, position) 
VALUES ('YY-ZZ', 'curso académico', 1);

INSERT INTO possible_value (value, attribute_id) 
VALUES ('2024-25', (SELECT id FROM attribute WHERE coding = 'YY-ZZ'));

#ISA
INSERT INTO attribute (coding, description, position) 
VALUES ('AA', 'Asignatura', 1);
INSERT INTO possible_value (value, attribute_id) 
VALUES ('2023-24', (SELECT id FROM attribute WHERE coding = 'YY-ZZ'));
INSERT INTO possible_value (value, attribute_id) 
VALUES ('2022-23', (SELECT id FROM attribute WHERE coding = 'YY-ZZ'));
INSERT INTO possible_value (value, attribute_id) 
VALUES ('2020-21', (SELECT id FROM attribute WHERE coding = 'YY-ZZ'));
-- 1. Inserta un usuario (tabla user)
INSERT INTO user (username, email, password, role)
VALUES ('testuser', 'test@example.com', 'secret', 'Usuario');

-- Valores posibles

INSERT INTO possible_value (value, attribute_id) 
VALUES ('2021-22', (SELECT id FROM attribute WHERE coding = 'YY-ZZ'));


-- 2. Inserta una unidad (tabla unit) usando el id del usuario (en este ejemplo, se asume que el usuario insertado tiene id = 1)
INSERT INTO unit (unit_name, responsible_id)
VALUES ('Departamento de Control', 1);

-- 3. Inserta un origen (tabla origin)
INSERT INTO origin (origin_name)
VALUES ('Interno');

--------------------------------------------------------------------------------
-- Proceso Académico y sus indicadores (Proceso 1)
--------------------------------------------------------------------------------

-- Proceso Académico (tabla process)
INSERT INTO process (type, coding, process_name, responsible_unit_id, version, approval_date, is_subprocess, process_father_id)
VALUES ('Estratégico', 'PROC-001', 'Proceso Académico', 1, '1.0', '2023-01-01', false, NULL);

-- Grupo de Indicadores Académicos (tabla indicator_group)
INSERT INTO indicator_group (coding, indicator_group_name, process_id, origin)
VALUES ('IG-001', 'Indicadores Académicos', 1, 'Interno');

-- Indicadores para el Proceso Académico (tabla indicator)
INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-001', 'Año académico', 'Año escolar correspondiente', '-', 'Anual', 'Texto plano', '-', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-002', 'Nº asignaturas matriculadas', 'Número de asignaturas en las que se matricula el estudiante', 'Conteo', 'Anual', 'Numérico', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-003', 'Nº asignaturas aprobadas', 'Número de asignaturas aprobadas por el estudiante', 'Conteo', 'Anual', 'Numérico', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-004', 'Nº de créditos ECTS matriculados', 'Número de créditos ECTS en los que se matricula el estudiante', 'Suma', 'Anual', 'Numérico', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-005', 'Nº de créditos ECTS aprobados', 'Número de créditos ECTS aprobados por el estudiante', 'Suma', 'Anual', 'Numérico', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-006', 'Nº de créditos ECTS superados', 'Número de créditos ECTS superados por el estudiante', 'Suma', 'Anual', 'Numérico', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-007', 'Nº de créditos ECTS presentados', 'Número de créditos ECTS presentados en evaluaciones', 'Suma', 'Anual', 'Numérico', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-008', 'Tasa de rendimiento', 'Porcentaje de rendimiento académico de los estudiantes', 'Porcentaje', 'Anual', 'Porcentaje', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-009', 'Tasa de éxito', 'Porcentaje de éxito en la aprobación de asignaturas', 'Porcentaje', 'Anual', 'Porcentaje', 'N/A', 1);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-010', 'Tasa de absentismo', 'Porcentaje de ausencias en clase', 'Porcentaje', 'Anual', 'Porcentaje', 'N/A', 1);

--------------------------------------------------------------------------------
-- Proceso de Recursos Humanos y sus indicadores (Proceso 2)
--------------------------------------------------------------------------------

-- Proceso de Recursos Humanos (tabla process)
INSERT INTO process (type, coding, process_name, responsible_unit_id, version, approval_date, is_subprocess, process_father_id)
VALUES ('Clave', 'PROC-002', 'Proceso de Recursos Humanos', 1, '2.0', '2023-02-01', false, NULL);

-- Grupo de Indicadores de Recursos Humanos (tabla indicator_group)
INSERT INTO indicator_group (coding, indicator_group_name, process_id, origin)
VALUES ('IG-002', 'Indicadores de Recursos Humanos', 2, 'Interno');

-- Indicadores para el Proceso de Recursos Humanos (tabla indicator)
INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-011', 'Nº de empleados', 'Número total de empleados en la organización', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-012', 'Tasa de rotación', 'Porcentaje de rotación de empleados en el periodo', 'Porcentaje', 'Mensual', 'Porcentaje', 'N/A', 2);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-013', 'Índice de satisfacción', 'Nivel de satisfacción de los empleados', 'Encuesta', 'Anual', 'Numérico', 'N/A', 2);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-014', 'Nº de horas de capacitación', 'Número de horas dedicadas a capacitación', 'Suma', 'Mensual', 'Numérico', 'N/A', 2);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-015', 'Nº de ausencias', 'Número de ausencias registradas', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2);


INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-016', 'CosteFormacion', 'Costes en formacion', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-017', 'T. absentismo', 'tasa de absentismo', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-018', 'Desempeño promedio', 'Desempeño promedio', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2);
--------------------------------------------------------------------------------
-- Proceso Administrativo y sus indicadores (Proceso 3)
--------------------------------------------------------------------------------

-- Proceso Administrativo (tabla process)
INSERT INTO process (type, coding, process_name, responsible_unit_id, version, approval_date, is_subprocess, process_father_id)
VALUES ('Soporte', 'PROC-003', 'Proceso Administrativo', 1, '1.5', '2023-03-01', false, NULL);

-- Grupo de Indicadores Administrativos (tabla indicator_group)
INSERT INTO indicator_group (coding, indicator_group_name, process_id, origin)
VALUES ('IG-003', 'Indicadores Administrativos', 3, 'Interno');

-- Indicadores para el Proceso Administrativo (tabla indicator)
INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-019', 'Costo operativo', 'Costo total de operaciones administrativas', 'Suma', 'Mensual', 'Numérico', 'N/A', 3);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-020', 'Tiempo de resolución', 'Tiempo promedio de resolución de incidencias', 'Promedio', 'Mensual', 'Numérico', 'N/A', 3);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-021', 'Eficiencia en procesos', 'Medida de eficiencia en procesos administrativos', 'Porcentaje', 'Mensual', 'Porcentaje', 'N/A', 3);

INSERT INTO indicator (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id)
VALUES ('IND-022', 'Nº de procesos automatizados', 'Cantidad de procesos automatizados en la administración', 'Conteo', 'Anual', 'Numérico', 'N/A', 3);


-- Proceso Académico (Indicator_group = 1)

-- Indicador 1: Año académico
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-001[2020-21]', 'Año académico', 'Año escolar correspondiente', '-', 'Anual', 'Texto plano', '-', 1, '2023-01-01', '', true);

-- Indicador 2: Nº asignaturas matriculadas
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-002[2020-21]', 'Nº asignaturas matriculadas', 'Número de asignaturas en las que se matricula el estudiante', 'Conteo', 'Anual', 'Numérico', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 3: Nº asignaturas aprobadas
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-003[2020-21]', 'Nº asignaturas aprobadas', 'Número de asignaturas aprobadas por el estudiante', 'Conteo', 'Anual', 'Numérico', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 4: Nº de créditos ECTS matriculados
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-004[2020-21]', 'Nº de créditos ECTS matriculados', 'Número de créditos ECTS en los que se matricula el estudiante', 'Suma', 'Anual', 'Numérico', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 5: Nº de créditos ECTS aprobados
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-005[2020-21]', 'Nº de créditos ECTS aprobados', 'Número de créditos ECTS aprobados por el estudiante', 'Suma', 'Anual', 'Numérico', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 6: Nº de créditos ECTS superados
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-006[2020-21]', 'Nº de créditos ECTS superados', 'Número de créditos ECTS superados por el estudiante', 'Suma', 'Anual', 'Numérico', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 7: Nº de créditos ECTS presentados
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-007[2020-21]', 'Nº de créditos ECTS presentados', 'Número de créditos ECTS presentados en evaluaciones', 'Suma', 'Anual', 'Numérico', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 8: Tasa de rendimiento
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-008[2020-21]', 'Tasa de rendimiento', 'Porcentaje de rendimiento académico de los estudiantes', 'Porcentaje', 'Anual', 'Porcentaje', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 9: Tasa de éxito
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-009[2020-21]', 'Tasa de éxito', 'Porcentaje de éxito en la aprobación de asignaturas', 'Porcentaje', 'Anual', 'Porcentaje', 'N/A', 1, '2023-01-01', '', true);

-- Indicador 10: Tasa de absentismo
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-001-IND-010[2020-21]', 'Tasa de absentismo', 'Porcentaje de ausencias en clase', 'Porcentaje', 'Anual', 'Porcentaje', 'N/A', 1, '2023-01-01', '', true);

--------------------------------------------------------------------------------
-- Proceso de Recursos Humanos (Indicator_group = 2)

-- Indicador 11: Nº de empleados
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-011[2020-21]', 'Nº de empleados', 'Número total de empleados en la organización', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);

-- Indicador 12: Tasa de rotación
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-012[2020-21]', 'Tasa de rotación', 'Porcentaje de rotación de empleados en el periodo', 'Porcentaje', 'Mensual', 'Porcentaje', 'N/A', 2, '2023-02-01', '', true);

-- Indicador 13: Índice de satisfacción
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-013[2020-21]', 'Índice de satisfacción', 'Nivel de satisfacción de los empleados', 'Encuesta', 'Anual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);

-- Indicador 14: Nº de horas de capacitación
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-014[2020-21]', 'Nº de horas de capacitación', 'Número de horas dedicadas a capacitación', 'Suma', 'Mensual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);

-- Indicador 15: Nº de ausencias
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-015[2020-21]', 'Nº de ausencias', 'Número de ausencias registradas', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);


-- Indicador 16: coste formacion
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-016[2020-21]', 'CosteFormacion', 'coste formacion de empleados', 'Encuesta', 'Anual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);

-- Indicador 17: tasa de absentismo
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-017[2020-21]', 'T. absentismo', 'Número de abstentismo', 'Suma', 'Mensual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);

-- Indicador 18: desempeño promedio
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-002-IND-018[2020-21]', 'Desempeño promedio', 'satisfaccion general', 'Conteo', 'Mensual', 'Numérico', 'N/A', 2, '2023-02-01', '', true);

--------------------------------------------------------------------------------
-- Proceso Administrativo (Indicator_group = 3)

-- Indicador 16: Costo operativo
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-003-IND-019', 'Costo operativo', 'Costo total de operaciones administrativas', 'Suma', 'Mensual', 'Numérico', 'N/A', 3, '2023-03-01', '', true);

-- Indicador 17: Tiempo de resolución
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-003-IND-020', 'Tiempo de resolución', 'Tiempo promedio de resolución de incidencias', 'Promedio', 'Mensual', 'Numérico', 'N/A', 3, '2023-03-01', '', true);

-- Indicador 18: Eficiencia en procesos
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-003-IND-021', 'Eficiencia en procesos', 'Medida de eficiencia en procesos administrativos', 'Porcentaje', 'Mensual', 'Porcentaje', 'N/A', 3, '2023-03-01', '', true);

-- Indicador 19: Nº de procesos automatizados
INSERT INTO indicator_instance (coding, indicator_name, description, calc_method, period, type, standard, indicator_group_id, modified_date, field, valid)
VALUES ('PROC-003-IND-022', 'Nº de procesos automatizados', 'Cantidad de procesos automatizados en la administración', 'Conteo', 'Anual', 'Numérico', 'N/A', 3, '2023-03-01', '', true);


INSERT INTO inds_group_have_attribs(indicator_group_id, attribute_id)
VALUES (
    1, 1
);
