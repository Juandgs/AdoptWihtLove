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

  // Previsualización del CSV al seleccionar archivo
  const fileInput = document.getElementById("file");
  if (fileInput) {
    fileInput.addEventListener("change", function(e) {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(event) {
          const csvContent = event.target.result;
          previewCSV(csvContent);
        };
        reader.readAsText(file);
      }
    });
  }

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
        
        // Auto-cerrar el mensaje después de 8 segundos
        setTimeout(() => {
          const alert = document.querySelector('#mensajeRespuesta .alert');
          if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
          }
        }, 8000);

        bootstrap.Modal.getInstance(document.getElementById('uploadModal')).hide();
        
        // Limpiar la previsualización
        document.getElementById('csvPreview').classList.add('d-none');
        document.getElementById('file').value = '';
        document.getElementById('btnSubirCsv').disabled = true;
        
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
        // Modificar el mensaje para quitar "con estado ACTIVO"
        const mensajeLimpio = mensaje.replace(/con estado ACTIVO/gi, '').trim();
        mostrarMensaje(mensajeLimpio, "success");
        
        // Auto-cerrar el mensaje después de 8 segundos
        setTimeout(() => {
          const alert = document.querySelector('#mensajeRespuesta .alert');
          if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
          }
        }, 8000);
        
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

  if (!productos || productos.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="text-center text-muted">No hay productos registrados.</td>
      </tr>`;
    return;
  }

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

  if (!productos || productos.length === 0) {
    contenedor.innerHTML = `
      <div class="col-12">
        <div class="d-flex flex-column justify-content-center align-items-center text-center" style="min-height: 100vh; min-width: 45vh; margin-top: -15vh; margin-left: 65vh;">
          <i class="fas fa-box-open fa-4x text-muted mb-3"></i>
          <p class="h4 text-muted">No hay productos registrados en el catálogo</p>
          <p class="text-muted">Agrega productos para que aparezcan aquí</p>
        </div>
      </div>`;
    return;
  }

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
  // Guardar el ID del producto a eliminar
  const btnConfirmar = document.getElementById('btnConfirmarEliminar');
  
  // Mostrar el modal
  const modalEliminar = new bootstrap.Modal(document.getElementById('modalEliminar'));
  modalEliminar.show();
  
  // Remover listeners anteriores para evitar duplicados
  const nuevoBtn = btnConfirmar.cloneNode(true);
  btnConfirmar.replaceWith(nuevoBtn);
  
  // Agregar el evento de confirmación
  nuevoBtn.addEventListener('click', function() {
    fetch(`/productos/eliminar/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
      .then(async res => {
        if (res.ok) {
          mostrarMensaje("Producto eliminado", "danger");
          // Auto-cerrar el mensaje después de 6 segundos
          setTimeout(() => {
            const alert = document.querySelector('#mensajeRespuesta .alert');
            if (alert) {
              const bsAlert = new bootstrap.Alert(alert);
              bsAlert.close();
            }
          }, 6000);
          cargarProductos();
        } else {
          const texto = await res.text();
          mostrarMensaje(texto, "danger");
        }
      })
      .catch(err => {
        console.error("Error al eliminar producto:", err);
        mostrarMensaje("Error al conectar con el servidor", "danger");
      });
    
    // Cerrar el modal
    modalEliminar.hide();
  });
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
            
            // Auto-cerrar el mensaje después de 8 segundos
            setTimeout(() => {
              const alert = document.querySelector('#mensajeRespuesta .alert');
              if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
              }
            }, 8000);
            
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

// Función para previsualizar el contenido del CSV
function previewCSV(csvContent) {
  const lines = csvContent.split('\n').filter(line => line.trim() !== '');
  
  if (lines.length === 0) {
    return;
  }

  // Obtener encabezados (primera línea)
  const headers = lines[0].split(',').map(h => h.trim());
  
  // Mostrar encabezados en la tabla
  const headersRow = document.getElementById('csvHeaders');
  headersRow.innerHTML = headers.map(h => `<th>${h}</th>`).join('');
  
  // Mostrar datos (resto de líneas)
  const dataBody = document.getElementById('csvData');
  dataBody.innerHTML = '';
  
  const dataLines = lines.slice(1); // Omitir la primera línea (encabezados)
  
  dataLines.forEach((line, index) => {
    if (line.trim() === '') return;
    
    const values = line.split(',').map(v => v.trim());
    const row = document.createElement('tr');
    
    values.forEach(value => {
      const cell = document.createElement('td');
      cell.textContent = value || '-';
      row.appendChild(cell);
    });
    
    dataBody.appendChild(row);
  });
  
  // Mostrar el total de productos
  document.getElementById('totalProductos').textContent = dataLines.length;
  
  // Mostrar la sección de previsualización
  document.getElementById('csvPreview').classList.remove('d-none');
  
  // Habilitar el botón de subir
  document.getElementById('btnSubirCsv').disabled = false;
}