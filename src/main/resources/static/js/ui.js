// Utilidades de interfaz compartidas por el panel (index.html), el login y las plantillas
// Thymeleaf: iconos, toasts, diálogo de confirmación y sombra de la columna de acciones.
// Se carga sin "defer" para que el script del panel pueda usar estas funciones al instante.

const ICONOS = {
  logo: '<path d="M12 5v14M5 12h14" stroke-width="2.6"/>',
  productos: '<path d="m7.5 4.27 9 5.15"/><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/>',
  categorias: '<path d="M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z"/><circle cx="7.5" cy="7.5" r="1" fill="currentColor"/>',
  unidades: '<path d="M21.3 15.3a2.4 2.4 0 0 1 0 3.4l-2.6 2.6a2.4 2.4 0 0 1-3.4 0L2.7 8.7a2.41 2.41 0 0 1 0-3.4l2.6-2.6a2.41 2.41 0 0 1 3.4 0Z"/><path d="m14.5 12.5 2-2"/><path d="m11.5 9.5 2-2"/><path d="m8.5 6.5 2-2"/><path d="m17.5 15.5 2-2"/>',
  proveedores: '<path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"/><path d="M15 18H9"/><path d="M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/>',
  auditoria: '<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M12 7v5l4 2"/>',
  usuarios: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  roles: '<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/>',
  editar: '<path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/>',
  power: '<path d="M12 2v10"/><path d="M18.4 6.6a9 9 0 1 1-12.77.04"/>',
  exito: '<circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/>',
  error: '<circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/>',
  advertencia: '<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/>',
  info: '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
  cerrar: '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
  buscar: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
  salir: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
  mas: '<path d="M5 12h14"/><path d="M12 5v14"/>',
  vacio: '<path d="M22 12h-6l-2 3h-4l-2-3H2"/><path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/>',
  anterior: '<path d="m15 18-6-6 6-6"/>',
  siguiente: '<path d="m9 18 6-6-6-6"/>',
  ver: '<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/>',
  ocultar: '<path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49"/><path d="M14.084 14.158a3 3 0 0 1-4.242-4.242"/><path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143"/><path d="m2 2 20 20"/>',
  limpiar: '<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/>',
  guardar: '<path d="M20 6 9 17l-5-5"/>'
};

function icono(nombre, clase) {
  return `<svg class="icon${clase ? " " + clase : ""}" viewBox="0 0 24 24" fill="none" stroke="currentColor" ` +
    `stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONOS[nombre] || ""}</svg>`;
}

// <span data-icon="editar"></span> en el HTML estático se reemplaza por el SVG
function pintarIconos(raiz) {
  (raiz || document).querySelectorAll("[data-icon]").forEach(el => {
    el.outerHTML = icono(el.dataset.icon, el.className);
  });
}

function escHtml(v) {
  if (v === null || v === undefined) return "";
  return String(v).replace(/[&<>"']/g, c => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

// ---------- Toasts ----------
const TOAST_TITULOS = { success: "Listo", error: "No se pudo completar", warning: "Atención", info: "Aviso" };
const TOAST_ICONOS = { success: "exito", error: "error", warning: "advertencia", info: "info" };

function regionToasts() {
  let region = document.getElementById("toast-region");
  if (!region) {
    region = document.createElement("div");
    region.id = "toast-region";
    region.className = "toast-region";
    document.body.appendChild(region);
  }
  return region;
}

// toast("Producto registrado", "success")
// toast("Datos inválidos", "error", { detalle: ["El nombre es obligatorio"] })
function toast(mensaje, tipo, opciones) {
  tipo = tipo || "info";
  opciones = opciones || {};
  const el = document.createElement("div");
  el.className = `toast ${tipo}`;
  el.setAttribute("role", tipo === "error" ? "alert" : "status");
  const detalle = opciones.detalle && opciones.detalle.length
    ? `<ul>${opciones.detalle.map(d => `<li>${escHtml(d)}</li>`).join("")}</ul>` : "";
  el.innerHTML = `
    ${icono(TOAST_ICONOS[tipo] || "info")}
    <div class="toast-body">
      <div class="toast-title">${escHtml(opciones.titulo || TOAST_TITULOS[tipo] || "Aviso")}</div>
      <div class="toast-msg">${escHtml(mensaje)}${detalle}</div>
    </div>
    <button type="button" class="toast-close" aria-label="Cerrar aviso">${icono("cerrar")}</button>`;

  const cerrar = () => {
    if (el.classList.contains("saliendo")) return;
    el.classList.add("saliendo");
    setTimeout(() => el.remove(), 180);
  };
  el.querySelector(".toast-close").addEventListener("click", cerrar);
  const region = regionToasts();
  region.appendChild(el);
  while (region.children.length > 4) region.firstElementChild.remove();
  setTimeout(cerrar, opciones.duracion || (tipo === "error" ? 7000 : 4000));
}

// ---------- Diálogo de confirmación ----------
// const ok = await confirmar({ titulo, mensaje, textoConfirmar: "Desactivar", tipo: "danger" })
function confirmar(opciones) {
  opciones = opciones || {};
  let dialogo = document.getElementById("confirm-dialog");
  if (!dialogo) {
    dialogo = document.createElement("dialog");
    dialogo.id = "confirm-dialog";
    dialogo.className = "modal";
    dialogo.setAttribute("aria-labelledby", "confirm-titulo");
    dialogo.setAttribute("aria-describedby", "confirm-mensaje");
    dialogo.innerHTML = `
      <form method="dialog">
        <div class="modal-body">
          <div class="modal-icon" id="confirm-icono"></div>
          <div>
            <h2 class="modal-title" id="confirm-titulo"></h2>
            <p class="modal-msg" id="confirm-mensaje"></p>
          </div>
        </div>
        <div class="modal-footer">
          <button type="submit" value="cancelar" class="btn btn-secondary" id="confirm-cancelar">Cancelar</button>
          <button type="submit" value="ok" class="btn btn-danger" id="confirm-ok">Confirmar</button>
        </div>
      </form>`;
    document.body.appendChild(dialogo);
  }

  const peligro = (opciones.tipo || "danger") === "danger";
  const iconoEl = dialogo.querySelector("#confirm-icono");
  iconoEl.className = "modal-icon" + (peligro ? "" : " info");
  iconoEl.innerHTML = icono(peligro ? "advertencia" : "info");
  dialogo.querySelector("#confirm-titulo").textContent = opciones.titulo || "¿Confirmar acción?";
  dialogo.querySelector("#confirm-mensaje").textContent = opciones.mensaje || "";
  const ok = dialogo.querySelector("#confirm-ok");
  ok.textContent = opciones.textoConfirmar || "Confirmar";
  ok.className = "btn " + (peligro ? "btn-danger" : "btn-primary");

  return new Promise(resolve => {
    dialogo.returnValue = "";
    dialogo.addEventListener("close", () => resolve(dialogo.returnValue === "ok"), { once: true });
    dialogo.showModal();
    dialogo.querySelector("#confirm-cancelar").focus();
  });
}

// Formularios Thymeleaf con data-confirmar="mensaje": se pide confirmación antes de enviarlos
document.addEventListener("submit", async (e) => {
  const form = e.target;
  if (!(form instanceof HTMLFormElement) || !form.dataset.confirmar || form.dataset.confirmado) return;
  e.preventDefault();
  const ok = await confirmar({
    titulo: form.dataset.confirmarTitulo,
    mensaje: form.dataset.confirmar,
    textoConfirmar: form.dataset.confirmarBoton,
    tipo: form.dataset.confirmarTipo
  });
  if (ok) {
    form.dataset.confirmado = "true";
    form.submit();
  }
});

// ---------- Tablas ----------
// Marca .has-overflow en los contenedores que realmente tienen scroll horizontal, para que la
// columna de acciones fija muestre su separador solo cuando hace falta.
const observadorTablas = "ResizeObserver" in window ? new ResizeObserver(entradas => {
  entradas.forEach(en => {
    const wrap = en.target.closest(".table-wrap");
    if (wrap) wrap.classList.toggle("has-overflow", wrap.scrollWidth > wrap.clientWidth + 1);
  });
}) : null;

function observarTablas() {
  if (!observadorTablas) return;
  document.querySelectorAll(".table-wrap").forEach(wrap => {
    observadorTablas.observe(wrap);
    const tabla = wrap.querySelector("table");
    if (tabla) observadorTablas.observe(tabla);
  });
}

// ---------- Contraseña visible/oculta (login) ----------
document.addEventListener("click", (e) => {
  const btn = e.target.closest(".password-toggle");
  if (!btn) return;
  const input = document.getElementById(btn.dataset.target);
  const mostrar = input.type === "password";
  input.type = mostrar ? "text" : "password";
  btn.innerHTML = icono(mostrar ? "ocultar" : "ver");
  btn.setAttribute("aria-label", mostrar ? "Ocultar contraseña" : "Mostrar contraseña");
});

document.addEventListener("DOMContentLoaded", () => {
  pintarIconos();
  observarTablas();
});
