# ==============================================================
# Frontend — nginx со статикой и reverse proxy к backend
# ==============================================================
FROM nginx:1.27-alpine

# Убираем дефолтный конфиг и контент
RUN rm /etc/nginx/conf.d/default.conf \
    && rm -rf /usr/share/nginx/html/*

# Конфиг nginx (проксирование API → backend)
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/app.conf

# Файлы фронтенда
COPY frontend/ /usr/share/nginx/html/

# Инъекция API_BASE_URL = "" (same-origin через nginx proxy).
# В dev-режиме api.js фолбэчит на http://localhost:8080,
# в Docker все API-запросы идут через nginx → proxy_pass backend:8080.
RUN sed -i 's|</head>|<script>window.API_BASE_URL="";</script>\n</head>|g' \
    /usr/share/nginx/html/*.html

EXPOSE 80
