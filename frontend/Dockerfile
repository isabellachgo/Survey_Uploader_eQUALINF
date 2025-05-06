# 1) Builder: generar la build de React
FROM node:18-alpine AS builder
WORKDIR /app

# 1.1) Copiar archivos de dependencias para cachear
COPY package*.json ./

# 1.2) Instalar deps
RUN npm ci

# 1.3) Copiar el resto del código
COPY . .

# 1.4) Inyectar la URL del backend
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL

# ← Aquí inyectamos el flag de OpenSSL
ARG NODE_OPTIONS="--openssl-legacy-provider"
ENV NODE_OPTIONS=$NODE_OPTIONS

# 1.5) Generar la build de producción sin el error de OpenSSL
RUN npm run build


# 2) Runtime: servir los archivos estáticos con Nginx
FROM nginx:stable-alpine
WORKDIR /usr/share/nginx/html

# (opcional) Configuración SPA para React Router
# COPY nginx.conf /etc/nginx/nginx.conf

# Copiamos la build compilada
COPY --from=builder /app/build .

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
