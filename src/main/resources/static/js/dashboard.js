document.addEventListener("DOMContentLoaded", async function () {
  const tablaProductos = document.querySelector("#seccion2 tbody");
  const tablaAnimales = document.querySelector("#tablaAnimales");
  const tablaVendedoresBloqueados = document.getElementById("tablaVendedoresBloqueados");
  const filtroTipoProducto = document.getElementById("filtroTipoProducto");

  let productos = [];
  let animales = [];

  function getCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  }

  const cargarDatos = async () => {
    try {
      const productosRes = await fetch("/productos/admin", { credentials: "include" });
      productos = await productosRes.json();

      const animalesRes = await fetch("/animal/api/animales", { credentials: "include" });
      const animalesData = await animalesRes.json();
      animales = Array.isArray(animalesData)
        ? animalesData
        : Array.isArray(animalesData.animales)
          ? animalesData.animales
          : Array.isArray(animalesData.data)
            ? animalesData.data
            : [];

      renderProductos();
      renderAnimales();
      renderGraficoProductos();
      renderGraficoAnimales();
    } catch (error) {
      console.error("Error cargando datos:", error);
    }
  };

  const renderProductos = () => {
    const tipo = filtroTipoProducto.value;
    const filtrados = tipo ? productos.filter(p => p.tipoProducto === tipo) : productos;

    tablaProductos.innerHTML = filtrados.map(p => `
      <tr>
        <td>${p.nombre}</td>
        <td>${p.tipoProducto}</td>
        <td>$${p.precio}</td>
        <td>${p.cantidad}</td>
        <td>${p.persona?.id ?? 'Sin vendedor'}</td>
        <td>
          <button class="btn btn-outline-danger btn-sm" onclick="verReclamos(${p.id}, '${p.nombre}', '${p.imagen}', ${p.persona?.id ?? null})">
            Ver (${p.reclamos?.length ?? 0})
          </button>
        </td>
      </tr>
    `).join('');
  };

  const renderAnimales = () => {
    if (!Array.isArray(animales)) return;

    tablaAnimales.innerHTML = animales.length > 0
      ? animales.map(a => `
        <tr>
          <td>${a.nombre}</td>
          <td>${a.edad}</td>
          <td>${a.raza}</td>
          <td>${a.tipo_animal}</td>
        </tr>
      `).join('')
      : `<tr><td colspan="4" class="text-muted">No hay animales disponibles</td></tr>`;
  };

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
          backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#81C784"]
        }]
      }
    });
  };

  const renderGraficoAnimales = () => {
    const canvas = document.getElementById("graficoAnimales");
    if (!canvas || !Array.isArray(animales)) return;
    const ctx = canvas.getContext("2d");

    const conteoPorTipo = animales.reduce((acc, a) => {
      acc[a.tipo_animal] = (acc[a.tipo_animal] || 0) + 1;
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
          backgroundColor: "#4DB6AC"
        }]
      }
    });
  };

  const cargarVendedoresBloqueados = async () => {
    try {
      const res = await fetch("/vendedores/bloqueados", { credentials: "include" });
      const vendedores = await res.json();

      tablaVendedoresBloqueados.innerHTML = vendedores.length > 0
        ? vendedores.map(v => `
          <tr>
            <td>${v.nombre}</td>
            <td>${v.correo}</td>
            <td>${v.reclamos?.join("<br>") ?? "Sin reclamos"}</td>
            <td>
              <button class="btn btn-success btn-sm" onclick="habilitarVendedor(${v.id})">Habilitar</button>
            </td>
          </tr>
        `).join('')
        : `<tr><td colspan="4" class="text-muted">No hay vendedores bloqueados</td></tr>`;
    } catch (error) {
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
        await cargarVendedoresBloqueados();
      } else {
        alert("No se pudo habilitar el vendedor");
      }
    } catch (error) {
      console.error("Error al habilitar vendedor:", error);
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
      alert("Vendedor bloqueado");
      await cargarDatos(); // 🔄 recarga productos y animales
      await cargarVendedoresBloqueados(); // 🔄 recarga tabla de bloqueados
      document.querySelector(".modal.show .btn-close").click();
    } else {
      alert("No se pudo bloquear al vendedor");
    }
  } catch (error) {
    console.error("Error al bloquear vendedor:", error);
  }
};


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
              <button class="btn btn-outline-secondary btn-sm" onclick="ignorarReclamo(${r.id})">Ignorar</button>
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
      location.reload(); // 🔄 recarga toda la página
    } else {
      alert("No se pudo eliminar el reclamo");
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
