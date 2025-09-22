document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion("productos");
  cargarProductos();

  // Navegación entre secciones
  document.querySelectorAll("[data-target]").forEach(link => {
    link.addEventListener("click", e => {
      e.preventDefault();
      const target = link.getAttribute("data-target");
      mostrarSeccion(target);
    });
  });

  // Subida de CSV
  const uploadForm = document.getElementById("uploadForm");
  if (uploadForm) {
    uploadForm.addEventListener("submit", async function (e) {
      e.preventDefault();
      const formData = new FormData(this);

      try {
        const response = await fetch("/productos/upload-csv", {
          method: "POST",
          body: formData,
          credentials: "include"
        });

        const mensaje = await response.text();
        mostrarMensaje(mensaje, "info");

        bootstrap.Modal.getInstance(document.getElementById('uploadModal')).hide();
        cargarProductos();
        mostrarSeccion("productos");

      } catch (error) {
        console.error("Error:", error);
        mostrarMensaje("Error al subir el archivo", "danger");
      }
    });
  }

  // Previsualización de imagen
  const imagenInput = document.getElementById('imagen');
  const preview = document.getElementById('previewImagen');

  imagenInput?.addEventListener('change', function () {
    const file = imagenInput.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function (e) {
        preview.src = e.target.result;
        preview.style.display = 'block';
      };
      reader.readAsDataURL(file);
    } else {
      preview.src = '#';
      preview.style.display = 'none';
    }
  });

  // Crear nuevo producto
  document.getElementById('formCrearProducto')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    const formData = new FormData();
    formData.append("nombre", document.getElementById('nombre').value.trim());
    formData.append("precio", document.getElementById('precio').value);
    formData.append("cantidad", document.getElementById('cantidad').value.trim());
    formData.append("descripcion", document.getElementById('descripcion').value.trim());
    formData.append("tipoProducto", document.getElementById('tipoProducto').value);
    formData.append("imagen", imagenInput.files[0]);

    try {
      const res = await fetch('/productos/crear', {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      const mensaje = await res.text();
      if (res.ok) {
        mostrarMensaje(mensaje, "success");
        this.reset();
        preview.style.display = 'none';
        cargarProductos();
        mostrarSeccion("productos");
      } else {
        mostrarMensaje(mensaje, "danger");
      }
    } catch (err) {
      console.error("Error al guardar producto:", err);
      mostrarMensaje("Error al conectar con el servidor", "danger");
    }
  });
});

// Funciones auxiliares
function mostrarSeccion(id) {
  document.querySelectorAll("main section").forEach(sec => sec.classList.add("d-none"));
  document.getElementById(id)?.classList.remove("d-none");

  if (id === "catalogo") {
    cargarCatalogo();
  }
}

function mostrarMensaje(texto, tipo) {
  const contenedor = document.getElementById("mensajeRespuesta");
  contenedor.innerHTML = `
    <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
      ${texto}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
  `;
}

function cargarProductos() {
  fetch("/productos/mis-productos", {
    method: "GET",
    credentials: "include"
  })
  .then(res => res.json())
  .then(productos => renderProductos(productos));
}

function renderProductos(productos) {
  const tbody = document.getElementById("tablaProductos");
  tbody.innerHTML = "";

  productos.forEach(p => {
    const fila = document.createElement("tr");
    fila.innerHTML = `
      <td>${p.nombre}</td>
      <td>${p.precio}</td>
      <td>${p.cantidad}</td>
      <td>${p.tipoProducto}</td>
      <td>${p.descripcion}</td>
      <td>${p.imagen ? `<img src="${p.imagen}" style="max-width: 100px;" />` : ""}</td>
      <td>
        <button class="btn btn-sm btn-primary" onclick="editarProducto(${p.id})">Editar</button>
        <button class="btn btn-sm btn-danger" onclick="eliminarProducto(${p.id})">Eliminar</button>
      </td>
    `;
    tbody.appendChild(fila);
  });
}

function cargarCatalogo() {
  fetch("/productos", {
    method: "GET"
  })
  .then(res => res.json())
  .then(productos => renderCatalogo(productos));
}

function renderCatalogo(productos) {
  const contenedor = document.getElementById("catalogoProductos");
  contenedor.innerHTML = "";

  productos.forEach(p => {
    const tarjeta = document.createElement("div");
    tarjeta.className = "col";

    tarjeta.innerHTML = `
      <div class="card h-100">
        <img src="${p.imagen}" class="card-img-top" alt="${p.nombre}" style="max-height: 200px; object-fit: cover;" />
        <div class="card-body">
          <h5 class="card-title">${p.nombre}</h5>
          <p class="card-text">${p.descripcion}</p>
          <p class="card-text"><strong>Precio:</strong> $${p.precio}</p>
          <p class="card-text"><strong>Tipo:</strong> ${p.tipoProducto}</p>
        </div>
      </div>
    `;

    contenedor.appendChild(tarjeta);
  });
}

function eliminarProducto(id) {
  if (confirm('¿Estás seguro de eliminar este producto?')) {
    fetch(`/productos/eliminar/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    .then(res => {
      if (res.ok) {
        mostrarMensaje("Producto eliminado", "success");
        cargarProductos();
      } else {
        mostrarMensaje("Error al eliminar el producto", "danger");
      }
    });
  }
}

function editarProducto(id) {
  fetch(`/productos/${id}`, {
    method: 'GET',
    credentials: 'include'
  })
  .then(res => res.json())
  .then(producto => {
    document.getElementById('nombre').value = producto.nombre;
    document.getElementById('precio').value = producto.precio;
    document.getElementById('cantidad').value = producto.cantidad;
    document.getElementById('descripcion').value = producto.descripcion;
    document.getElementById('tipoProducto').value = producto.tipoProducto;
    document.getElementById('previewImagen').src = producto.imagen;
    document.getElementById('previewImagen').style.display = 'block';
    document.getElementById('imagen').required = false;

    mostrarSeccion('nuevoProducto');

    const form = document.getElementById('formCrearProducto');
    form.onsubmit = async function (e) {
      e.preventDefault();

      const formData = new FormData();
      formData.append("nombre", document.getElementById('nombre').value.trim());
      formData.append("precio", document.getElementById('precio').value);
      formData.append("cantidad", document.getElementById('cantidad').value.trim());
      formData.append("descripcion", document.getElementById('descripcion').value.trim());
      formData.append("tipoProducto", document.getElementById('tipoProducto').value);

      const file = imagenInput.files[0];
      if (file) {
        formData.append("imagen", file);
      }

      try {
        const res = await fetch(`/productos/editar/${id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            nombre: formData.get("nombre"),
            precio: parseFloat(formData.get("precio")),
            cantidad: formData.get("cantidad"),
            descripcion: formData.get("descripcion"),
            tipoProducto: formData.get("tipoProducto"),
            imagen: producto.imagen
          }),
          credentials: 'include'
        });

        const mensaje = await res.text();
        if (res.ok) {
          mostrarMensaje(mensaje, "success");
          cargarProductos();
          mostrarSeccion("productos");
        } else {
          mostrarMensaje(mensaje, "danger");
        }
      } catch (err) {
        console.error("Error al actualizar producto:", err);
        mostrarMensaje("Error al conectar con el servidor", "danger");
      }
    };
  });
}
