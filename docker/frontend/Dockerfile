# ==============================================================
# Frontend — nginx со статикой, reverse proxy, cache busting
# ==============================================================
FROM nginx:1.27-alpine

# Убираем дефолтный конфиг и контент
RUN rm /etc/nginx/conf.d/default.conf \
    && rm -rf /usr/share/nginx/html/*

# Конфиг nginx
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/app.conf

# Файлы фронтенда
COPY frontend/ /usr/share/nginx/html/

# Инъекция API_BASE_URL = "" (same-origin через nginx proxy)
RUN sed -i 's|</head>|<script>window.API_BASE_URL="";</script>\n</head>|g' \
    /usr/share/nginx/html/*.html

# Cache busting: api.js → api.a3f7c1b2.js
COPY docker/frontend/cache-bust.sh /tmp/cache-bust.sh
RUN chmod +x /tmp/cache-bust.sh && /tmp/cache-bust.sh && rm /tmp/cache-bust.sh

EXPOSE 80
