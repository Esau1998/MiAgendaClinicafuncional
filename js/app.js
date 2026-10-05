// JS general de Mi Agenda de Clinica
(function () {
    'use strict';

    // ---- Menu lateral (movil) ----
    var sidebar = document.getElementById('sidebar');
    var backdrop = document.querySelector('.sidebar-backdrop');
    document.querySelectorAll('[data-toggle-sidebar]').forEach(function (el) {
        el.addEventListener('click', function () {
            if (!sidebar) return;
            sidebar.classList.toggle('abierto');
            if (backdrop) backdrop.classList.toggle('visible', sidebar.classList.contains('abierto'));
        });
    });

    // ---- Resaltar el enlace activo del menu ----
    var ruta = window.location.pathname;
    var mejor = null;
    document.querySelectorAll('.sidebar-link[data-ruta]').forEach(function (a) {
        var r = a.getAttribute('data-ruta');
        if ((ruta === r || ruta.indexOf(r + '/') === 0) && (!mejor || r.length > mejor.getAttribute('data-ruta').length)) {
            mejor = a;
        }
    });
    if (mejor) mejor.classList.add('activo');

    // ---- Mostrar / ocultar contrasena ----
    document.querySelectorAll('[data-ver-password]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = document.getElementById(btn.getAttribute('data-ver-password'));
            if (!input) return;
            var oculto = input.type === 'password';
            input.type = oculto ? 'text' : 'password';
            var icono = btn.querySelector('i');
            if (icono) icono.className = oculto ? 'bi bi-eye-slash' : 'bi bi-eye';
        });
    });

    // ---- Medidor de fuerza de contrasena ----
    document.querySelectorAll('[data-fuerza]').forEach(function (input) {
        var barra = document.querySelector('#' + input.getAttribute('data-fuerza') + ' > span');
        var texto = document.querySelector('#' + input.getAttribute('data-fuerza') + ' + .fuerza-texto');
        input.addEventListener('input', function () {
            var v = input.value, puntos = 0;
            if (v.length >= 8) puntos++;
            if (v.length >= 12) puntos++;
            if (/[a-z]/.test(v) && /[A-Z]/.test(v)) puntos++;
            if (/\d/.test(v)) puntos++;
            if (/[^A-Za-z0-9]/.test(v)) puntos++;
            var niveles = [
                ['0%', '#dc3545', ''],
                ['25%', '#dc3545', 'Muy débil'],
                ['45%', '#f2a03d', 'Débil'],
                ['65%', '#e0b300', 'Aceptable'],
                ['85%', '#2e9e5b', 'Buena'],
                ['100%', '#0f7c86', 'Excelente']
            ];
            var n = v.length === 0 ? niveles[0] : niveles[Math.max(1, puntos)];
            if (barra) { barra.style.width = n[0]; barra.style.background = n[1]; }
            if (texto) texto.textContent = n[2];
        });
    });

    // ---- Validar que las dos contrasenas coincidan antes de enviar ----
    document.querySelectorAll('form[data-confirmar-password]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            var p = form.querySelector('[name="password"]');
            var c = form.querySelector('[name="confirmar"]');
            if (p && c && p.value !== c.value) {
                e.preventDefault();
                c.setCustomValidity('Las contraseñas no coinciden');
                c.reportValidity();
            }
        });
        var c = form.querySelector('[name="confirmar"]');
        if (c) c.addEventListener('input', function () { c.setCustomValidity(''); });
    });

    // ---- Los avisos de exito se cierran solos ----
    document.querySelectorAll('.alert-autocierre').forEach(function (a) {
        setTimeout(function () {
            a.style.transition = 'opacity .5s';
            a.style.opacity = '0';
            setTimeout(function () { a.remove(); }, 550);
        }, 7000);
    });
})();
