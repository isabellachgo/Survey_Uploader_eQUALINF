# 📊 Survey Uploader – TFG eQUALINF

El sistema Survey Uploader, desarrollado como parte de un Trabajo de Fin de Grado en la plataforma de calidad eQUALINF, permite la carga y análisis semiautomático de encuestas académicas.  
Consta de un backend en Spring Boot y un frontend en React, ambos orquestados mediante Docker para facilitar el despliegue e integración en otros entornos.

---
## 📁 Estructura del proyecto
```
Survey_Uploader_eQUALINF/
├── docker-compose.yml         # Orquestación de servicios
├── backend/                   # Backend en Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/upm/etsiinf/backend/
│   │   │   │   ├── controller/            # Controladores REST
│   │   │   │   ├── service/               # Lógica de negocio
│   │   │   │   ├── model/                 # Modelos de datos
│   │   │   │   └── BackendApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db-config.properties
├── frontend/                  # Frontend en React
│   ├── Dockerfile
│   ├── public/icons/
│   ├── src/
│   │   ├── components/                   # Paneles y contenedores
│   │   ├── api/                          # Conector HTTP
│   │   ├── styles/                       # Estilos
│   │   └── index.js, App.js, etc.    
├── database_scripts
```

## 🚀 ¿Cómo ejecutar el proyecto?

### ✅ Requisitos

- Docker instalado (https://www.docker.com/)
- Git para clonar el repositorio
- MySQL corriendo localmente con las siguientes bases de datos ya creadas:
  - `pac_db_2018_2019`
  - `pac_db_2019_2020`
  - `pac_db_2020_2021`
  - `pac_db_2021_2022`
  - `pac_db_2022_2023`
  - `pac_db_2023_2024`
  - `pac_db_2024_2025` (usada como principal)
  - Nota: los scrips de dichas bases de datos, que son las utilizadas en las pruebas, 
    se proporcionan como ejemplo en la carpeta  📁 /database_scripts.
  - Verifica que el usuario (root/1234) tiene permisos suficientes. 
  - Si estás en Windows, permite a Docker el acceso a redes locales (firewall).

### 📋 Pasos para la ejecución
1. Abrir Docker Desktop y esperar a que diga “Docker Desktop is running”.
2. Clonar el proyecto
  -git clone https://github.com/isabellachgo/Survey_Uploader_eQUALINF.git
3. Acceder a la carpeta del proyecto:     cd Survey_Uploader_eQUALINF
4. Ejecutar con Docker compose:  
    - 🐳 docker-compose up -–build
5. Acceder a los servicios:
    - 🌐 Backend en http://localhost:8080
    - 🖥️ Frontend en http://localhost:3000

## ⚙️ Configuración de base de datos (db-config.properties)

El backend accede a distintas bases de datos según el curso académico. Esto se configura en el archivo:

📁 backend/src/main/resources/db-config.properties

Ejemplo:
```properties
db.2020_2021.url=jdbc:mysql://host.docker.internal:3306/pac_db_2020_2021?useSSL=false&allowPublicKeyRetrieval=true&requireSSL=false
db.2020_2021.username=root
db.2020_2021.password=1234

db.2021_2022.url=jdbc:mysql://host.docker.internal:3306/pac_db_2021_2022?useSSL=false&allowPublicKeyRetrieval=true&requireSSL=false
```
...
- 🔁 Este archivo permite que el sistema se conecte a diferentes BBDD en tiempo de ejecución según el año académico del archivo cargado.
- 📌 host.docker.internal permite que los contenedores Docker accedan a tu MySQL local desde dentro del backend.
- NOTA:Se incluyen los atributos
`?useSSL=false&allowPublicKeyRetrieval=true&requireSSL=false` en las URLs de conexión debido a que el backend corre en Docker y accede a MySQL en local.
Estos parámetros evitan errores relacionados con certificados SSL. Si en lugar de una base de datos local se utilizara una base de datos contenida también en Docker, 
estos parámetros no serían necesarios.



## 🛠️ Personalización y mantenimiento

- Los servicios están desacoplados, por lo que se pueden adaptar fácilmente.
- Se puede modificar la configuración de bases de datos: editar `db-config.properties` para cambiar URLs, credenciales o bien añadir nuevas bases de datos.
- Cambiar puertos: modificar el archivo `docker-compose.yml` para ajustar los puertos expuestos externamente.
- Personalizar interfaz: editar `frontend/src/styles/styles.css` para cambiar colores, fuentes y aspecto visual de la interfaz.
- Añadir funcionalidades: crear nuevos controladores, servicios o rutas en el backend siguiendo el patrón MVC de Spring Boot.
- Actualizar dependencias: usar `mvn versions:display-dependency-updates` en el backend o `npm update` en el frontend.


## 📌 Licencia y autoría  

Este proyecto ha sido desarrollado como parte del Trabajo Fin de Grado en la Escuela Técnica Superior de Ingenieros Informáticos de la Universidad Politécnica de Madrid.

---
Isabella Chaves Gómez
