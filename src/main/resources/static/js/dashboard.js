document.addEventListener("DOMContentLoaded", async function () {
  const tablaProductos = document.querySelector("#seccion2 tbody");
  const tablaAnimales = document.querySelector("#tablaAnimales");
  const tablaVendedoresBloqueados = document.getElementById("tablaVendedoresBloqueados");
  const filtroTipoProducto = document.getElementById("filtroTipoProducto");
  const filtroEstadoAnimal = document.getElementById("filtroEstadoAnimal");

  let productos = [];
  let animales = [];

  function getCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  }

  const cargarDatos = async () => {
    let productosOk = false, animalesOk = false;
    // Cargar productos
    try {
      const productosRes = await fetch("/productos/admin", { credentials: "include" });
      productos = await productosRes.json();
      productosOk = Array.isArray(productos);
    } catch (error) {
      productos = [];
      console.error("Error cargando productos:", error);
    }
    // Cargar animales
    try {
      const animalesRes = await fetch("/animales/admin", { credentials: "include" });
      const animalesData = await animalesRes.json();
      // Acepta array directo o {animales: []} o {data: []}
      animales = Array.isArray(animalesData)
        ? animalesData
        : Array.isArray(animalesData.animales)
          ? animalesData.animales
          : Array.isArray(animalesData.data)
            ? animalesData.data
            : [];
      animalesOk = Array.isArray(animales);
    } catch (error) {
      animales = [];
      console.error("Error cargando animales:", error);
    }
    renderProductos();
    renderAnimales();
    renderGraficoProductos();
    renderGraficoAnimales();
  };

  const renderProductos = () => {
    const tipo = filtroTipoProducto.value;
    const filtrados = tipo ? productos.filter(p => p.tipoProducto === tipo) : productos;
    if (!Array.isArray(filtrados) || filtrados.length === 0) {
      tablaProductos.innerHTML = `<tr><td colspan="7" class="text-muted">No hay productos para mostrar</td></tr>`;
      return;
    }
    tablaProductos.innerHTML = filtrados.map(p => `
      <tr>
        <td>${p.nombre}</td>
        <td>${p.tipoProducto}</td>
        <td>$${p.precio}</td>
        <td>${p.cantidad}</td>
        <td>${p.nombreEstado ?? 'Sin estado'}</td>
        <td>${p.personaId ?? 'Sin vendedor'}</td>
        <td>
          <button class="btn btn-reclamo-difuminado btn-sm" onclick="verReclamos(${p.id}, '${p.nombre}', '${p.imagen}', ${p.personaId ?? null})">
            Ver (${p.cantidadReclamos ?? 0})
          </button>
        </td>
      </tr>
    `).join('');
  };

  const renderAnimales = () => {
    const estado = filtroEstadoAnimal.value;
    const filtrados = estado ? animales.filter(a => a.nombreEstado === estado) : animales;
    if (!Array.isArray(filtrados) || filtrados.length === 0) {
      tablaAnimales.innerHTML = `<tr><td colspan="5" class="text-muted">No hay animales disponibles</td></tr>`;
      return;
    }
    tablaAnimales.innerHTML = filtrados.map(a => `
      <tr>
        <td>${a.nombre}</td>
        <td>${a.edad}</td>
        <td>${a.raza}</td>
        <td>${a.tipo_animal || a.tipoAnimal}</td>
        <td>${a.nombreEstado ?? 'Sin estado'}</td>
      </tr>
    `).join('');
  };


  // Paleta basada en el degradado del menú: verde agua, verde oscuro, gris oscuro, azul oscuro
  const customColors = [
    "#4DB6AC", // Verde agua (principal)
    "#38817A", // Verde oscuro
    "#232526", // Gris oscuro
    "#1976D2"  // Azul oscuro
  ];

  const renderGraficoProductos = () => {
    const canvas = document.getElementById("grafico2");
    if (!canvas || !Array.isArray(productos)) return;
    const ctx = canvas.getContext("2d");

    const conteoPorTipo = productos.reduce((acc, prod) => {
      acc[prod.tipoProducto] = (acc[prod.tipoProducto] || 0) + 1;
      return acc;
    }, {});

    const labels = Object.keys(conteoPorTipo);
    const data = Object.values(conteoPorTipo);

    if (window.graficoProductos && typeof window.graficoProductos.destroy === "function") {
      window.graficoProductos.destroy();
    }

    window.graficoProductos = new Chart(ctx, {
      type: "pie",
      data: {
        labels: labels,
        datasets: [{
          label: "Productos por tipo",
          data: data,
          backgroundColor: customColors.slice(0, labels.length),
        }]
      },
      options: {
        plugins: {
          legend: {
            position: 'top',
            labels: {
              color: '#232526',
              font: { family: 'Poppins', size: 16 }
            }
          }
        }
      }
    });
  };

  const renderGraficoAnimales = () => {
    const canvas = document.getElementById("graficoAnimales");
    if (!canvas || !Array.isArray(animales)) return;
    const ctx = canvas.getContext("2d");

    const conteoPorTipo = animales.reduce((acc, a) => {
      const tipo = a.tipoAnimal || a.tipo_animal || 'Sin tipo';
      acc[tipo] = (acc[tipo] || 0) + 1;
      return acc;
    }, {});

    const labels = Object.keys(conteoPorTipo);
    const data = Object.values(conteoPorTipo);

    if (window.graficoAnimales && typeof window.graficoAnimales.destroy === "function") {
      window.graficoAnimales.destroy();
    }

    window.graficoAnimales = new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels,
        datasets: [{
          label: "Animales por tipo",
          data: data,
          backgroundColor: customColors.slice(0, labels.length)
        }]
      },
      options: {
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          x: {
            ticks: {
              color: '#232526',
              font: { family: 'Poppins', size: 16 }
            }
          },
          y: {
            ticks: {
              color: '#232526',
              font: { family: 'Poppins', size: 16 }
            }
          }
        }
      }
    });
  };

  const cargarVendedoresBloqueados = async () => {
    try {
      const res = await fetch("/vendedores/bloqueados", { credentials: "include" });
      const vendedores = await res.json();
      if (!Array.isArray(vendedores) || vendedores.length === 0) {
        tablaVendedoresBloqueados.innerHTML = `<tr><td colspan="4" class="text-muted">No hay vendedores bloqueados</td></tr>`;
        return;
      }
      tablaVendedoresBloqueados.innerHTML = vendedores.map(v => `
        <tr>
          <td>${v.nombre}</td>
          <td>${v.correo}</td>
          <td>${v.reclamos?.join("<br>") ?? "Sin reclamos"}</td>
          <td>
            <button class="btn btn-success btn-sm" onclick="habilitarVendedor(${v.id})">Habilitar</button>
          </td>
        </tr>
      `).join('');
    } catch (error) {
      tablaVendedoresBloqueados.innerHTML = `<tr><td colspan="4" class="text-muted">No hay vendedores bloqueados</td></tr>`;
      console.error("Error cargando vendedores bloqueados:", error);
    }
  };

  window.habilitarVendedor = async (id) => {
    try {
      const token = getCsrfToken();
      const res = await fetch(`/habilitar/${id}`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "X-XSRF-TOKEN": token
        }
      });
      if (res.ok) {
        localStorage.setItem('mensajeVendedor', 'Vendedor habilitado exitosamente');
        localStorage.setItem('colorMensajeVendedor', '#4DB6AC');
        location.reload();
      } else {
        localStorage.setItem('mensajeVendedor', 'No se pudo habilitar el vendedor');
        localStorage.setItem('colorMensajeVendedor', '#ff4d4d');
        location.reload();
      }
    } catch (error) {
      console.error("Error al habilitar vendedor:", error);
      localStorage.setItem('mensajeVendedor', 'Error al habilitar vendedor');
      localStorage.setItem('colorMensajeVendedor', '#ff4d4d');
      location.reload();
    }
  };

  window.bloquearVendedor = async (id) => {
    try {
      const token = getCsrfToken();
      const res = await fetch(`/bloquear/${id}`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "X-XSRF-TOKEN": token
        }
      });
      if (res.ok) {
        localStorage.setItem('mensajeVendedor', 'Vendedor bloqueado exitosamente');
        localStorage.setItem('colorMensajeVendedor', '#ff4d4d');
        location.reload();
      } else {
        localStorage.setItem('mensajeVendedor', 'No se pudo bloquear al vendedor');
        localStorage.setItem('colorMensajeVendedor', '#ff4d4d');
        location.reload();
      }
    } catch (error) {
      console.error("Error al bloquear vendedor:", error);
      localStorage.setItem('mensajeVendedor', 'Error al bloquear vendedor');
      localStorage.setItem('colorMensajeVendedor', '#ff4d4d');
      location.reload();
    }
  };


// Mostrar mensaje vendedor tras recarga
window.addEventListener('DOMContentLoaded', () => {
  const mensaje = localStorage.getItem('mensajeVendedor');
  const color = localStorage.getItem('colorMensajeVendedor');
  if (mensaje) {
    let div = document.getElementById("mensajeVendedor");
    if (!div) {
      div = document.createElement("div");
      div.id = "mensajeVendedor";
      div.style.position = "fixed";
      div.style.top = "30px";
      div.style.right = "30px";
      div.style.zIndex = 9999;
      div.style.padding = "16px 32px";
      div.style.borderRadius = "8px";
      div.style.fontWeight = "bold";
      div.style.fontSize = "1.1rem";
      div.style.boxShadow = "0 2px 12px rgba(0,0,0,0.12)";
      document.body.appendChild(div);
    }
    div.textContent = mensaje;
    div.style.background = color || '#ff4d4d';
    div.style.color = "#fff";
    div.style.display = "block";
    setTimeout(() => { div.style.display = "none"; }, 2200);
    localStorage.removeItem('mensajeVendedor');
    localStorage.removeItem('colorMensajeVendedor');
  }
});


  window.verReclamos = async (productoId, nombre, imagen, vendedorId) => {
    try {
      const res = await fetch(`/reclamos/producto/${productoId}`, { credentials: "include" });
      const reclamos = await res.json();

      const info = `
        <p><strong>ID Vendedor:</strong> ${vendedorId}</p>
        <p><strong>Producto:</strong> ${nombre}</p>
        ${imagen ? `<img src="${imagen}" alt="Imagen" style="max-width: 150px;">` : ''}
      `;
      document.getElementById("infoProducto").innerHTML = info;

      const tabla = document.getElementById("tablaReclamos");
      tabla.innerHTML = reclamos.length > 0
        ? reclamos.map(r => `
          <tr>
            <td>${r.descripcion}</td>
            <td>
              <button class="btn btn-outline-secondary btn-sm btn-ignorar-reclamo" onclick="ignorarReclamo(${r.id})">Ignorar</button>
              <button class="btn btn-outline-danger btn-sm" onclick="bloquearVendedor(${vendedorId})">Bloquear</button>
            </td>
          </tr>
        `).join('')
        : `<tr><td colspan="2" class="text-muted">Sin reclamos</td></tr>`;

      new bootstrap.Modal(document.getElementById("modalReclamos")).show();
    } catch (error) {
      console.error("Error al cargar reclamos:", error);
    }
  };

  window.ignorarReclamo = async (id) => {
  try {
    const token = getCsrfToken();
    const res = await fetch(`/reclamos/${id}`, {
      method: "DELETE",
      credentials: "include",
      headers: {
        "X-XSRF-TOKEN": token
      }
    });
    if (res.ok) {
      // Mostrar toast antes de recargar
      if (typeof showReclamoToast === 'function') {
        showReclamoToast();
        setTimeout(() => location.reload(), 1800);
      } else {
        location.reload();
      }
    } else {
      // Mostrar toast de error
      if (typeof showPdfToast === 'function') showPdfToast('errorReclamo');
      else alert("No se pudo eliminar el reclamo");
    }
  } catch (error) {
    console.error("Error al ignorar reclamo:", error);
  }
};


  

    const links = document.querySelectorAll("#menuLateral a");
  const sections = document.querySelectorAll("main section");


  function mostrarSeccion(id) {
    sections.forEach(section => {
      section.classList.toggle("d-none", section.id !== id);
    });
    // Marcar opción activa en el menú lateral
    links.forEach(link => {
      const targetId = link.getAttribute("data-target");
      if (targetId === id) {
        link.classList.add("active");
      } else {
        link.classList.remove("active");
      }
    });
  }

  links.forEach(link => {
    link.addEventListener("click", e => {
      e.preventDefault();
      const targetId = link.getAttribute("data-target");
      mostrarSeccion(targetId);

      if (targetId === "seccion2") {
        renderProductos();
        renderGraficoProductos();
      } else if (targetId === "seccion4") {
        renderAnimales();
        renderGraficoAnimales();
      } else if (targetId === "seccion5") {
        cargarVendedoresBloqueados();
      }
    });
  });

  filtroTipoProducto.addEventListener("change", () => {
    renderProductos();
    renderGraficoProductos();
  });

  filtroEstadoAnimal.addEventListener("change", () => {
    renderAnimales();
    renderGraficoAnimales();
  });

  mostrarSeccion("seccion2");
  await cargarDatos();

  const descargarPDF = (selector, nombreArchivo) => {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    doc.text(nombreArchivo.toUpperCase(), 14, 15);
    doc.autoTable({ html: selector, startY: 25 });
    doc.save(`${nombreArchivo}.pdf`);
  };

  document.getElementById("descargarProductos").addEventListener("click", () => {
    descargarPDF("#seccion2 table", "productos");
  });

  document.getElementById("descargarAnimales").addEventListener("click", () => {
    descargarPDF("#seccion4 table", "animales");
  });
});
