/**
 * auth.js — Модуль авторизации.
 *
 * Проверяет сессию при загрузке страницы.
 * Если не авторизован — редирект на login.html.
 * Добавляет кнопку «Выйти» в навигацию.
 */

/**
 * Проверить авторизацию. Вызывать при загрузке каждой страницы.
 * Если не авторизован — редирект на login.html.
 */
export async function requireAuth() {
    try {
        const response = await fetch('/api/auth/me', { credentials: 'same-origin' });
        if (!response.ok) {
            redirectToLogin();
            return false;
        }
        document.body.classList.add('auth-ready');
        addLogoutButton();
        return true;
    } catch {
        redirectToLogin();
        return false;
    }
}

/**
 * Выйти из системы.
 */
export async function logout() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'same-origin',
        });
    } catch {
        // Игнорируем ошибку — всё равно редиректим
    }
    redirectToLogin();
}

/**
 * Перехватчик 401 — вызывать из API-клиента при ошибке.
 * Если сессия истекла — редирект на логин.
 */
export function handle401(response) {
    if (response?.status === 401) {
        redirectToLogin();
        return true;
    }
    return false;
}

function redirectToLogin() {
    if (!window.location.pathname.includes('login.html')) {
        window.location.href = '/login.html';
    }
}

function addLogoutButton() {
    const nav = document.querySelector('.header__nav');
    if (!nav || nav.querySelector('.logout-btn')) return;

    const btn = document.createElement('a');
    btn.href = '#';
    btn.textContent = 'Выйти';
    btn.className = 'logout-btn';
    btn.addEventListener('click', (e) => {
        e.preventDefault();
        logout();
    });
    nav.appendChild(btn);
}
