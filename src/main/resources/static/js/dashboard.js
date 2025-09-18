document.addEventListener("DOMContentLoaded", async function () {
  const tablaProductos = document.querySelector("#seccion2 tbody");
  const tablaAnimales = document.querySelector("#seccion4 tbody");

  const filtroTipoProducto = document.getElementById("filtroTipoProducto");
  const filtroTipoAnimal = document.getElementById("filtroTipoAnimal");

  let productos = [];
  let animales = [];

  const cargarDatos = async () => {
    try {
      const productosRes = await fetch("/productos/admin");
      productos = await productosRes.json();

      const animalesRes = await fetch("/animales/admin");
      animales = await animalesRes.json();

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
      </tr>
    `).join('');
  };

  const renderAnimales = () => {
    const tipo = filtroTipoAnimal.value;
    const filtrados = tipo ? animales.filter(a => a.tipo_animal === tipo) : animales;

    tablaAnimales.innerHTML = filtrados.map(a => `
      <tr>
        <td>${a.nombre}</td>
        <td>${a.edad}</td>
        <td>${a.raza}</td>
        <td>${a.tipo_animal}</td>
      </tr>
    `).join('');
  };

  const renderGraficoProductos = () => {
    const canvas = document.getElementById("grafico2");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const conteoPorTipo = productos.reduce((acc, prod) => {
      acc[prod.tipoProducto] = (acc[prod.tipoProducto] || 0) + 1;
      return acc;
    }, {});

    const labels = Object.keys(conteoPorTipo);
    const data = Object.values(conteoPorTipo);

    if (window.graficoProductos) window.graficoProductos.destroy();

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
    const canvas = document.getElementById("grafico4");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const conteoPorTipo = animales.reduce((acc, a) => {
      acc[a.tipo_animal] = (acc[a.tipo_animal] || 0) + 1;
      return acc;
    }, {});

    const labels = Object.keys(conteoPorTipo);
    const data = Object.values(conteoPorTipo);

    if (window.graficoAnimales) window.graficoAnimales.destroy();

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

  // Cambiar de sección al hacer click en el menú lateral
  const links = document.querySelectorAll("#menuLateral a");
  const sections = document.querySelectorAll("main section");

  links.forEach(link => {
    link.addEventListener("click", e => {
      e.preventDefault();
      const targetId = link.getAttribute("data-target");

      sections.forEach(section => {
        section.classList.toggle("d-none", section.id !== targetId);
      });

      if (targetId === "seccion2") {
        renderProductos();
        renderGraficoProductos();
      } else if (targetId === "seccion4") {
        renderAnimales();
        renderGraficoAnimales();
      }
    });
  });

  // Filtros
  filtroTipoProducto.addEventListener("change", () => {
    renderProductos();
    renderGraficoProductos();
  });

  filtroTipoAnimal.addEventListener("change", () => {
    renderAnimales();
    renderGraficoAnimales();
  });

  // Mostrar la primera sección por defecto
  document.querySelector("main section").classList.remove("d-none");

  await cargarDatos();

  // Descargar PDF
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

