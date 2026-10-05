/* ==========================================================

   ========================================================== */
(() => {
    'use strict';

    const CLAVE = 'agenda.avatar.';
    const COLORES = ['#0f7c86', '#0a2e3d', '#7c3aed', '#c2631a', '#16a34a', '#be185d'];

    /* ---------- Almacenamiento (en este navegador) ---------- */
    const leer = id => {
        try { return JSON.parse(localStorage.getItem(CLAVE + id)) || {}; }
        catch (e) { return {}; }
    };
    const guardar = (id, estado) => {
        try {
            if (Object.keys(estado).length) localStorage.setItem(CLAVE + id, JSON.stringify(estado));
            else localStorage.removeItem(CLAVE + id);
            return true;
        } catch (e) { return false; }
    };

    /* ---------- Pintar un avatar según su estado ---------- */
    function pintar(av, estado, alt) {
        const original = av.dataset.original || '';
        const url = estado.quitar ? '' : (estado.foto || original);
        let img = av.querySelector('img');
        if (url) {
            if (!img) {
                img = document.createElement('img');
                av.prepend(img);
            }
            img.hidden = false;
            img.src = url;
            img.alt = alt || '';
            av.classList.add('con-foto');
        } else {
            if (img) img.hidden = true;
            av.classList.remove('con-foto');
        }
        av.style.background = estado.color || '';
    }

    /* ---------- Aplicar lo guardado al cargar ---------- */
    document.querySelectorAll('.doc-card[data-id]').forEach(card => {
        const av = card.querySelector('.avatar');
        if (!av) return;
        const img = av.querySelector('img');
        av.dataset.original = img ? img.getAttribute('src') : '';
        const estado = leer(card.dataset.id);
        if (Object.keys(estado).length) {
            pintar(av, estado, card.querySelector('.doc-nombre')?.textContent.trim());
        }
    });

    /* ---------- Iconos de los filtros de especialidad ---------- */
    const ICONOS = [
        ['cardio', 'bi-heart-pulse'], ['dermat', 'bi-droplet-half'], ['gineco', 'bi-gender-female'],
        ['general', 'bi-clipboard2-pulse'], ['oftalm', 'bi-eye'], ['pediatr', 'bi-balloon-heart'],
        ['neuro', 'bi-activity'], ['trauma', 'bi-bandaid'], ['psico', 'bi-chat-heart']
    ];
    const normalizar = t => t.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

    document.querySelectorAll('.chip').forEach(chip => {
        const nombre = chip.querySelector('span');
        if (!nombre) return; // "Todas" no lleva icono
        const texto = normalizar(nombre.textContent);
        const hallado = ICONOS.find(([clave]) => texto.includes(clave));
        const icono = document.createElement('i');
        icono.className = 'bi ' + (hallado ? hallado[1] : 'bi-plus-circle');
        chip.prepend(icono);
    });

    /* ---------- Foto del médico en el modal de cita ---------- */
    document.addEventListener('click', e => {
        const boton = e.target.closest('[data-solicitar]');
        if (!boton) return;
        const destino = document.getElementById('citaMedicoAvatar');
        const av = boton.closest('.doc-card')?.querySelector('.avatar');
        if (!destino || !av) return;
        destino.className = av.className + ' avatar-sm';
        destino.style.background = av.style.background;
        destino.innerHTML = av.innerHTML;
        destino.hidden = false;
    });

    /* ---------- Editor de avatar ---------- */
    const modalEl = document.getElementById('modalAvatar');
    if (!modalEl || typeof bootstrap === 'undefined') return;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    const vista = document.getElementById('avatarVista');
    const nombreEl = document.getElementById('avatarNombre');
    const archivo = document.getElementById('avatarArchivo');
    const urlInput = document.getElementById('avatarUrl');
    const coloresEl = document.getElementById('avatarColores');
    const aviso = document.getElementById('avatarAviso');

    let actual = null;   // { id, av, nombre }
    let estado = {};     // cambios en edición

    const mostrarAviso = texto => { aviso.textContent = texto || ''; aviso.hidden = !texto; };
    const refrescar = () => {
        pintar(vista, estado, actual?.nombre);
        coloresEl.querySelectorAll('button').forEach(b =>
            b.classList.toggle('activo', b.dataset.color === estado.color));
    };

    COLORES.forEach(color => {
        const b = document.createElement('button');
        b.type = 'button';
        b.dataset.color = color;
        b.style.background = color;
        b.setAttribute('aria-label', 'Color ' + color);
        b.addEventListener('click', () => { estado.color = color; refrescar(); });
        coloresEl.appendChild(b);
    });

    document.addEventListener('click', e => {
        const boton = e.target.closest('.avatar-editar');
        if (!boton) return;
        const card = boton.closest('.doc-card');
        const av = card.querySelector('.avatar');
        actual = { id: card.dataset.id, av, nombre: card.querySelector('.doc-nombre').textContent.trim() };
        estado = leer(actual.id);

        nombreEl.textContent = actual.nombre;
        vista.innerHTML = '';
        const iniciales = av.querySelector('.iniciales');
        if (iniciales) vista.appendChild(iniciales.cloneNode(true));
        vista.dataset.original = av.dataset.original || '';
        archivo.value = '';
        urlInput.value = (estado.foto && !estado.foto.startsWith('data:')) ? estado.foto : '';
        mostrarAviso('');
        refrescar();
        modal.show();
    });

    // Reduce la imagen a 320x320 para que ocupe poco espacio
    const reducir = file => new Promise((ok, mal) => {
        const lector = new FileReader();
        lector.onerror = mal;
        lector.onload = () => {
            const im = new Image();
            im.onerror = mal;
            im.onload = () => {
                const lado = Math.min(im.width, im.height);
                const lienzo = document.createElement('canvas');
                lienzo.width = lienzo.height = 320;
                lienzo.getContext('2d').drawImage(im, (im.width - lado) / 2, (im.height - lado) / 2, lado, lado, 0, 0, 320, 320);
                ok(lienzo.toDataURL('image/jpeg', 0.85));
            };
            im.src = lector.result;
        };
        lector.readAsDataURL(file);
    });

    archivo.addEventListener('change', async () => {
        const f = archivo.files[0];
        if (!f) return;
        try {
            estado.foto = await reducir(f);
            delete estado.quitar;
            urlInput.value = '';
            mostrarAviso('');
            refrescar();
        } catch (err) {
            mostrarAviso('No se pudo leer esa imagen. Prueba con otra.');
        }
    });

    urlInput.addEventListener('input', () => {
        const v = urlInput.value.trim();
        if (/^(https?:\/\/|\/)/i.test(v)) {
            estado.foto = v;
            delete estado.quitar;
            refrescar();
        }
    });

    document.getElementById('avatarQuitar').addEventListener('click', () => {
        estado.quitar = true;
        delete estado.foto;
        urlInput.value = '';
        archivo.value = '';
        refrescar();
    });

    document.getElementById('avatarReset').addEventListener('click', () => {
        estado = {};
        urlInput.value = '';
        archivo.value = '';
        refrescar();
    });

    document.getElementById('avatarGuardar').addEventListener('click', () => {
        if (!guardar(actual.id, estado)) {
            mostrarAviso('No hay espacio para guardar esta imagen. Prueba con una más pequeña.');
            return;
        }
        pintar(actual.av, estado, actual.nombre);
        modal.hide();
    });
})();