#!/bin/sh
# cache-bust.sh — Добавляет хэш содержимого в имена JS/CSS файлов.
#
# Пример: api.js → api.a3f7c1b2.js
#
# Затем обновляет ссылки в HTML-файлах.
# Запускается при сборке Docker-образа фронтенда.

set -e

ROOT="/usr/share/nginx/html"

echo "=== Cache busting: добавление хэшей в имена файлов ==="

# Обработать все JS и CSS файлы
find "$ROOT" -type f \( -name "*.js" -o -name "*.css" \) | while read -r filepath; do
    # Вычислить короткий хэш (первые 8 символов MD5)
    hash=$(md5sum "$filepath" | cut -c1-8)

    dir=$(dirname "$filepath")
    filename=$(basename "$filepath")
    name="${filename%.*}"       # api
    ext="${filename##*.}"       # js

    newname="${name}.${hash}.${ext}"   # api.a3f7c1b2.js
    newpath="${dir}/${newname}"

    # Переименовать файл
    mv "$filepath" "$newpath"

    # Относительный путь от ROOT для замены в HTML
    old_rel="${filepath#$ROOT/}"    # js/api.js
    new_rel="${newpath#$ROOT/}"     # js/api.a3f7c1b2.js

    echo "  $old_rel → $new_rel"

    # Обновить ссылки во всех HTML-файлах
    find "$ROOT" -name "*.html" -exec sed -i "s|${old_rel}|${new_rel}|g" {} +

    # Обновить ссылки в JS-файлах (import из generated-client)
    find "$ROOT" -name "*.js" -exec sed -i "s|${filename}|${newname}|g" {} +
done

echo "=== Cache busting завершён ==="
