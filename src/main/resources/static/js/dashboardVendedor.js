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

// Cargar productos del vendedor
function cargarProductos(estados = ["ACTIVO"]) {
  fetch(`/productos/filtrados?${estados.map(e => 'estados=' + e).join('&')}`, {
    method: "GET",
    credentials: "include"
  })
    .then(res => res.json())
    .then(productos => renderProductos(productos))
    .catch(err => console.error("Error cargando productos:", err));
}

// Renderizar tabla de productos
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

// Renderizar catálogo público
function cargarCatalogo() {
  fetch("/productos/filtrados?estados=ACTIVO", {
    method: "GET",
    credentials: "include"
  })
    .then(res => res.json())
    .then(productos => renderCatalogo(productos))
    .catch(err => console.error("Error cargando catálogo:", err));
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

// Eliminar producto (cambia estado a INACTIVO)
function eliminarProducto(id) {
  if (confirm('¿Estás seguro de eliminar este producto?')) {
    fetch(`/productos/eliminar/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
      .then(async res => {
        const texto = await res.text(); // primero obtén el texto
        if (res.ok) {
          mostrarMensaje(texto, "success");
          cargarProductos();
        } else {
          mostrarMensaje(texto, "danger");
        }
      })
      .catch(err => {
        console.error("Error al eliminar producto:", err);
        mostrarMensaje("Error al conectar con el servidor", "danger");
      });
  }
}

// Función para llenar el formulario con los datos del producto
function llenarFormulario(producto) {
  document.getElementById('nombre').value = producto.nombre;
  document.getElementById('precio').value = producto.precio;
  document.getElementById('cantidad').value = producto.cantidad;
  document.getElementById('descripcion').value = producto.descripcion;
  document.getElementById('tipoProducto').value = producto.tipoProducto;
  
  const preview = document.getElementById('previewImagen');
  preview.src = producto.imagen;
  preview.style.display = 'block';

  const imagenInput = document.getElementById('imagen');
  imagenInput.value = ""; // limpia el input, así al subir una nueva imagen reemplaza la anterior
  imagenInput.required = false;
}


// Editar producto
function editarProducto(id) {
  fetch(`/productos/${id}`, { method: 'GET', credentials: 'include' })
    .then(async res => {
      if (!res.ok) {
        const texto = await res.text();
        mostrarMensaje(`Error al obtener producto: ${texto}`, 'danger');
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      return res.json();
    })
    .then(producto => {
      mostrarSeccion('nuevoProducto');

      const form = document.getElementById('formCrearProducto');

      // Eliminar listeners anteriores
      const nuevoForm = form.cloneNode(true);
      form.replaceWith(nuevoForm);

      // Llenar formulario con los datos del producto
      llenarFormulario(producto);

      // Listener para la previsualización de la nueva imagen
const imagenInput = document.getElementById('imagen');
const preview = document.getElementById('previewImagen');

imagenInput.addEventListener('change', function () {
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


      // Listener para enviar los cambios
      nuevoForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const formData = new FormData(nuevoForm);
        if (document.getElementById('imagen').files[0]) {
          formData.append("imagen", document.getElementById('imagen').files[0]);
        }

        try {
          const res = await fetch(`/productos/editar/${id}`, {
            method: 'PUT',
            body: formData,
            credentials: 'include'
          });

          const mensaje = await res.text();
          if (res.ok) {
            mostrarMensaje(mensaje, "success");
            nuevoForm.reset();
            document.getElementById('previewImagen').style.display = 'none';
            cargarProductos();
            mostrarSeccion("productos");
          } else {
            mostrarMensaje(mensaje, "danger");
          }
        } catch (err) {
          console.error("Error al actualizar producto:", err);
          mostrarMensaje("Error al conectar con el servidor", "danger");
        }
      });
    })
    .catch(err => console.error("Error al editar producto:", err));
}