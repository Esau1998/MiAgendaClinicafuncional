// Selector de fondo: elige una imagen incluida, sube la tuya o pega una URL.
// La eleccion se guarda en este navegador (localStorage).
(function () {
    'use strict';

    var K_IMG = 'mac.fondo.img', K_VELO = 'mac.fondo.velo', K_BLUR = 'mac.fondo.blur';
    var PRESETS = [
        ['clinica', 'Clínica'], ['menta', 'Menta'], ['latido', 'Latido'], ['cruces', 'Cruces'],
        ['ondas', 'Ondas'], ['celulas', 'Células'], ['atardecer', 'Atardecer'], ['minimal', 'Minimal']
    ];

    function leer(k) { try { return localStorage.getItem(k); } catch (e) { return null; } }
    function guardar(k, v) { try { localStorage.setItem(k, v); return true; } catch (e) { return false; } }
    function borrar(k) { try { localStorage.removeItem(k); } catch (e) { /* nada */ } }

    function urlCss(valor) { return 'url("' + String(valor).replace(/"/g, '%22') + '")'; }

    function aplicar() {
        var raiz = document.documentElement.style;
        var img = leer(K_IMG);
        if (img) raiz.setProperty('--fondo-img', urlCss(img)); else raiz.removeProperty('--fondo-img');
        var velo = leer(K_VELO);
        if (velo !== null) raiz.setProperty('--fondo-velo', velo); else raiz.removeProperty('--fondo-velo');
        var blur = leer(K_BLUR);
        if (blur !== null) raiz.setProperty('--fondo-blur', blur + 'px'); else raiz.removeProperty('--fondo-blur');
        marcarActivo();
    }

    function marcarActivo() {
        var actual = leer(K_IMG) || '/img/fondos/clinica.svg';
        document.querySelectorAll('.fondo-opcion').forEach(function (b) {
            b.classList.toggle('activo', b.getAttribute('data-img') === actual);
        });
    }

    var cuadricula = document.getElementById('fondosGrid');
    if (cuadricula) {
        PRESETS.forEach(function (p) {
            var ruta = '/img/fondos/' + p[0] + '.svg';
            var b = document.createElement('button');
            b.type = 'button';
            b.className = 'fondo-opcion';
            b.setAttribute('data-img', ruta);
            b.innerHTML = '<div class="mini" style="background-image:url(' + ruta + ')"></div><span>' + p[1] + '</span>';
            b.addEventListener('click', function () { guardar(K_IMG, ruta); aplicar(); });
            cuadricula.appendChild(b);
        });
    }

    // Subir imagen propia (se reduce para que quepa en el navegador)
    var archivo = document.getElementById('fondoArchivo');
    if (archivo) {
        archivo.addEventListener('change', function () {
            var f = archivo.files && archivo.files[0];
            if (!f) return;
            if (!/^image\//.test(f.type)) { alert('Elige un archivo de imagen (JPG, PNG, WEBP o SVG).'); return; }
            var lector = new FileReader();
            lector.onload = function () {
                var img = new Image();
                img.onload = function () {
                    var max = 1920, esc = Math.min(1, max / Math.max(img.width, img.height));
                    var c = document.createElement('canvas');
                    c.width = Math.round(img.width * esc);
                    c.height = Math.round(img.height * esc);
                    c.getContext('2d').drawImage(img, 0, 0, c.width, c.height);
                    var data = c.toDataURL('image/jpeg', 0.82);
                    if (!guardar(K_IMG, data)) {
                        alert('La imagen es demasiado pesada para guardarla en el navegador. Prueba con una más pequeña o usa una URL.');
                        return;
                    }
                    aplicar();
                };
                img.onerror = function () { alert('No se pudo leer esa imagen.'); };
                img.src = lector.result;
            };
            lector.readAsDataURL(f);
            archivo.value = '';
        });
    }

    // Pegar URL
    var url = document.getElementById('fondoUrl');
    var btnUrl = document.getElementById('fondoUrlAplicar');
    if (btnUrl && url) {
        btnUrl.addEventListener('click', function () {
            var v = url.value.trim();
            if (!/^https?:\/\//i.test(v)) { alert('Escribe una URL que empiece con http:// o https://'); return; }
            guardar(K_IMG, v); aplicar();
        });
    }

    // Controles de oscurecer y desenfocar
    var velo = document.getElementById('fondoVelo');
    var blur = document.getElementById('fondoBlur');
    if (velo) {
        var vv = leer(K_VELO);
        if (vv !== null) velo.value = Math.round(parseFloat(vv) * 100);
        velo.addEventListener('input', function () { guardar(K_VELO, String(velo.value / 100)); aplicar(); });
    }
    if (blur) {
        var bb = leer(K_BLUR);
        if (bb !== null) blur.value = bb;
        blur.addEventListener('input', function () { guardar(K_BLUR, String(blur.value)); aplicar(); });
    }

    var reset = document.getElementById('fondoReset');
    if (reset) {
        reset.addEventListener('click', function () {
            borrar(K_IMG); borrar(K_VELO); borrar(K_BLUR);
            if (velo) velo.value = 30;
            if (blur) blur.value = 0;
            aplicar();
        });
    }

    aplicar();
})();
