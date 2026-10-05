// Catalogo publico: filtros, busqueda y formulario de solicitud de cita.
(function () {
    'use strict';

    function sinAcentos(t) {
        return (t || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
    }

    var tarjetas = Array.prototype.slice.call(document.querySelectorAll('.doc-card'));
    var chips = Array.prototype.slice.call(document.querySelectorAll('.chip[data-esp]'));
    var buscador = document.getElementById('buscador');
    var vacio = document.getElementById('sinResultados');
    var contador = document.getElementById('contadorResultados');
    var espActiva = '';

    function filtrar() {
        var q = sinAcentos(buscador ? buscador.value : '');
        var visibles = 0;
        tarjetas.forEach(function (t) {
            var okEsp = !espActiva || t.getAttribute('data-esp') === espActiva;
            var okTxt = !q || (t.getAttribute('data-buscar') || '').indexOf(q) !== -1;
            var mostrar = okEsp && okTxt;
            t.hidden = !mostrar;
            if (mostrar) visibles++;
        });
        if (vacio) vacio.hidden = visibles !== 0 || tarjetas.length === 0;
        if (contador) contador.textContent = visibles + (visibles === 1 ? ' especialista' : ' especialistas');
    }

    chips.forEach(function (c) {
        c.addEventListener('click', function () {
            espActiva = c.getAttribute('data-esp');
            chips.forEach(function (x) { x.classList.toggle('activo', x === c); });
            filtrar();
        });
    });
    if (buscador) {
        buscador.addEventListener('input', filtrar);
        buscador.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                var dest = document.getElementById('especialistas');
                if (dest) dest.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }
    filtrar();

    // ---- Formulario de solicitud ----
    var modalEl = document.getElementById('modalCita');
    if (!modalEl) return;
    var form = document.getElementById('formCita');
    var api = form.getAttribute('data-api');
    var inMedico = document.getElementById('citaMedicoId');
    var titulo = document.getElementById('citaMedicoNombre');
    var inFecha = document.getElementById('citaFecha');
    var selHora = document.getElementById('citaHora');
    var aviso = document.getElementById('citaAviso');
    var btnEnviar = document.getElementById('citaEnviar');

    function limpiarHoras(texto) {
        selHora.innerHTML = '';
        var o = document.createElement('option');
        o.value = '';
        o.textContent = texto;
        selHora.appendChild(o);
        selHora.disabled = true;
        btnEnviar.disabled = true;
    }

    function mostrarAviso(msg) {
        aviso.textContent = msg || '';
        aviso.hidden = !msg;
    }

    function cargarHoras() {
        mostrarAviso('');
        if (!inFecha.value || !inMedico.value) { limpiarHoras('Primero elige la fecha'); return; }
        limpiarHoras('Buscando horas...');
        fetch(api + '?medicoId=' + encodeURIComponent(inMedico.value) + '&fecha=' + encodeURIComponent(inFecha.value))
            .then(function (r) { return r.json(); })
            .then(function (d) {
                if (!d.horas || d.horas.length === 0) {
                    limpiarHoras('Sin horas disponibles');
                    mostrarAviso(d.mensaje || 'No hay horas libres ese día.');
                    return;
                }
                selHora.innerHTML = '<option value="">Elige una hora</option>';
                d.horas.forEach(function (h) {
                    var o = document.createElement('option');
                    o.value = h;
                    o.textContent = h;
                    selHora.appendChild(o);
                });
                selHora.disabled = false;
            })
            .catch(function () {
                limpiarHoras('No se pudo consultar');
                mostrarAviso('No se pudo consultar la disponibilidad. Intenta de nuevo.');
            });
    }

    document.querySelectorAll('[data-solicitar]').forEach(function (b) {
        b.addEventListener('click', function () {
            inMedico.value = b.getAttribute('data-solicitar');
            titulo.textContent = b.getAttribute('data-nombre');
            inFecha.value = '';
            limpiarHoras('Primero elige la fecha');
            mostrarAviso('');
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        });
    });

    inFecha.addEventListener('change', cargarHoras);
    selHora.addEventListener('change', function () { btnEnviar.disabled = !selHora.value; });
    form.addEventListener('submit', function () { btnEnviar.disabled = true; btnEnviar.textContent = 'Enviando...'; });
})();
