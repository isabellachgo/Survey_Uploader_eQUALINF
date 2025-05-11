# 📊 Survey Uploader – TFG eQUALINF

Este proyecto forma parte de un Trabajo Fin de Grado (TFG) y tiene como objetivo permitir la carga, procesamiento y análisis de archivos académicos en formato Excel, conectando con múltiples bases de datos académicas segmentadas por curso. El sistema incluye un backend desarrollado en Spring Boot y un frontend en React, ambos orquestados mediante Docker.

---

## 🚀 ¿Cómo ejecutar el proyecto?

### ✅ Requisitos

- Docker instalado (https://www.docker.com/)
- Abrir Docker Desktop y esperar a que diga “Docker Desktop is running”.
- Clonar el proyecto
  - git clone https://github.com/isabellachgo/Survey_Uploader_eQUALINF.git
- MySQL corriendo localmente con las siguientes bases de datos ya creadas:
  - `pac_db_2020_2021`
  - `pac_db_2021_2022`
  - `pac_db_2022_2023`
  - `pac_db_2024_2025` (usada como principal)
- Usuario con permisos de lectura y escritura (por ejemplo, `root/1234`)
- Se proporciona script de ejemplo 
- cd Survey_Uploader_eQUALINF
-  la aplicación:
  - docker-compose up --build
- Acceder a:
  - Frontend: http://localhost:3000
---

## 📁 Estructura del proyecto

Survey_Uploader_eQUALINF/
├── backend/ # Backend Spring Boot
│ ├── Dockerfile
│ ├── pom.xml
│ └── src/
│ └── main/resources/
│ └── db-config.properties # Configuración múltiple de BBDD
├── frontend/ # Frontend React
│ ├── Dockerfile
│ └── src/
├── docker-compose.yml # Orquestación completa
└── README.md # Esta guía


---

## ⚙️ Configuración de base de datos (db-config.properties)

El backend accede a distintas bases de datos según el curso académico. Esto se configura en el archivo:

backend/src/main/resources/db-config.properties


Ejemplo:

```properties
db.2020_2021.url=jdbc:mysql://host.docker.internal:3306/pac_db_2020_2021?useSSL=false&allowPublicKeyRetrieval=true&requireSSL=false
db.2020_2021.username=root
db.2020_2021.password=1234

db.2021_2022.url=jdbc:mysql://host.docker.internal:3306/pac_db_2021_2022?useSSL=false&allowPublicKeyRetrieval=true&requireSSL=false
...
🔁 Este archivo permite que el sistema se conecte a diferentes BBDD en tiempo de ejecución según el año académico del archivo cargado.

📌 host.docker.internal permite que los contenedores Docker accedan a tu MySQL local desde dentro del backend.

🐳 Ejecutar con Docker Compose
Desde la raíz del proyecto, ejecuta:

docker-compose up --build
Esto levantará:

🌐 tfg-backend en http://localhost:8080

🖥️ tfg-frontend en http://localhost:3000

La interfaz permite cargar archivos Excel que serán analizados y contrastados con datos almacenados en las distintas bases de datos académicas.

🔌 Integración con otras aplicaciones
Este sistema está diseñado para integrarse fácilmente con otras webs o plataformas:

El backend expone endpoints REST que permiten:

Subida de archivos

Consulta de procesos

Análisis de indicadores

El frontend puede reemplazarse por cualquier otra interfaz que consuma los mismos endpoints.

Se puede extender db-config.properties para conectar con más bases de datos si se agregan más cursos.

📬 ¿Cómo probar correctamente?
Asegúrate de que las BBDD locales están creadas y pobladas correctamente.

Verifica que el usuario (root/1234) tiene permisos suficientes.

Si estás en Windows, permite a Docker el acceso a redes locales (firewall).

Comprueba que el archivo db-config.properties contiene todas las URLs con host.docker.internal.

🛠️ Personalización
Si vas a integrarlo con otro sistema:

Puedes modificar las credenciales, URLs o lógica de selección de BBDD.

Los servicios están desacoplados, por lo que se pueden adaptar fácilmente.

También puedes reemplazar el frontend por uno nuevo que consuma los mismos endpoints REST.

📌 Licencia y autoría
Este proyecto ha sido desarrollado como parte del Trabajo Fin de Grado en la Escuela Técnica Superior de Ingenieros Informáticos de la Universidad Politécnica de Madrid, en colaboración con el equipo de calidad de la plataforma eQUALINF.

