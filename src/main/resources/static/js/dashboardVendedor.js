document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion("productos");
  cargarProductos();

  const links = document.querySelectorAll("[data-target]");
  links.forEach(link => {
    link.addEventListener("click", e => {
      e.preventDefault();
      mostrarSeccion(link.getAttribute("data-target"));
    });
  });

  const uploadForm = document.getElementById("uploadForm");
if (uploadForm) {
  uploadForm.addEventListener("submit", async function (e) {
    e.preventDefault();
    const formData = new FormData(this);

    try {
      const response = await fetch("/animal/upload-csv", {
        method: "POST",
        body: formData,
        credentials: "include"
      });

      const mensaje = await response.text();
      document.getElementById("mensajeRespuesta").innerHTML =
        `<div class="alert alert-info">${mensaje}</div>`;

      const modal = bootstrap.Modal.getInstance(document.getElementById('uploadModal'));
      modal.hide();

      cargarAnimales();
      mostrarSeccion("animales");

    } catch (error) {
      console.error("Error:", error);
      document.getElementById("mensajeRespuesta").innerHTML =
        `<div class="alert alert-danger">Error al subir el archivo</div>`;
    }
  });
}

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

  document.getElementById('formCrearProducto')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    const file = imagenInput.files[0];
    const base64 = await toBase64(file);

    const data = {
      nombre: document.getElementById('nombre').value,
      precio: parseFloat(document.getElementById('precio').value),
      cantidad: document.getElementById('cantidad').value,
      descripcion: document.getElementById('descripcion').value,
      tipoProducto: document.getElementById('tipoProducto').value,
      imagen: base64,
    };

    const res = await fetch('/productos/crear', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      credentials: 'include'
    });

    if (res.ok) {
      alert('Producto guardado con éxito');
      cargarProductos();
      mostrarSeccion("productos");
    } else {
      alert('Error al guardar el producto');
    }
  });
});

function mostrarSeccion(id) {
  const sections = document.querySelectorAll("main section");
  sections.forEach(section => section.classList.add("d-none"));
  const s = document.getElementById(id);
  if (s) s.classList.remove("d-none");
}

function toBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
}

function cargarProductos() {
  fetch("/productos/mis-productos", {
    method: "GET",
    credentials: "include"
  })
  .then(res => res.json())
  .then(productos => {
    renderProductos(productos);
  });
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

function eliminarProducto(id) {
  if (confirm('¿Estás seguro de eliminar este producto?')) {
    fetch(`/productos/eliminar/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    .then(res => {
      if (res.ok) {
        alert('Producto eliminado');
        cargarProductos();
      } else {
        alert('Error al eliminar el producto');
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
      const file = imagenInput.files[0];
      const base64 = file ? await toBase64(file) : producto.imagen;

      const data = {
        nombre: document.getElementById('nombre').value,
        precio: parseFloat(document.getElementById('precio').value),
        cantidad: document.getElementById('cantidad').value,
        descripcion: document.getElementById('descripcion').value,
        tipoProducto: document.getElementById('tipoProducto').value,
        imagen: base64
      };

      const res = await fetch(`/productos/editar/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
        credentials: 'include'
      });

      if (res.ok) {
        alert('Producto actualizado');
        cargarProductos();
        mostrarSeccion("productos");
      } else {
        alert('Error al actualizar el producto');
      }
    };
  });
}
