// ✅ Función global para mostrar secciones
function mostrarSeccion(id) {
  const sections = document.querySelectorAll("main section");
  sections.forEach(section => section.classList.add("d-none"));
  const target = document.getElementById(id);
  if (target) target.classList.remove("d-none");
}

// Función para mostrar mensajes
function mostrarMensaje(texto, tipo) {
  const contenedor = document.getElementById("mensajeRespuesta");
  contenedor.innerHTML = `
    <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
      ${texto}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
  `;
}

document.addEventListener("DOMContentLoaded", () => {
  const links = document.querySelectorAll("aside a");
  const sections = document.querySelectorAll("main section");

  // Navegación entre secciones
  links.forEach(link => {
    link.addEventListener("click", e => {
      e.preventDefault();
      const target = link.getAttribute("data-target");

      sections.forEach(section => {
        section.classList.toggle("d-none", section.id !== target);
      });

      links.forEach(l => l.classList.remove("active"));
      link.classList.add("active");

      if (target === "animales") cargarAnimales();
    });
  });

  // ✅ Mostrar sección inicial y cargar animales
  mostrarSeccion("animales");
  cargarAnimales();

  // ✅ Activar botón “Subir Animal”
  document.getElementById("btnNuevoAnimal")?.addEventListener("click", () => {
    // limpiar formulario al crear nuevo
    limpiarFormulario();
    mostrarSeccion("nuevoAnimal");
  });

  // Previsualización de imagen
  const imagenAnimalInput = document.getElementById('imagenAnimal');
  const previewAnimal = document.getElementById('previewAnimal');

  imagenAnimalInput?.addEventListener('change', function () {
    const file = imagenAnimalInput.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function (e) {
        previewAnimal.src = e.target.result;
        previewAnimal.style.display = 'block';
      };
      reader.readAsDataURL(file);
    } else {
      previewAnimal.src = '#';
      previewAnimal.style.display = 'none';
    }
  });

  // Crear o editar animal
  document.getElementById('formCrearAnimal')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    const idEditar = this.dataset.editando;

    // Validar imagen solo si estamos CREANDO
  const file = imagenAnimalInput.files[0];
  if (!idEditar && !file) {
    alert("Debes subir una imagen para el animal.");
    return;
  }

    // Si estamos EDITANDO -> usar PUT con JSON
    if (idEditar) {
      // construir payload JSON
      const payload = {
        nombre: document.getElementById('nombreAnimal').value,
        edad: Number(document.getElementById('edadAnimal').value),
        raza: document.getElementById('razaAnimal').value,
        tipo_animal: document.getElementById('tipoAnimal').value,
        imagen: this.dataset.existingImagen || null // valor por defecto
      };

      const file = imagenAnimalInput.files[0];
      if (file) {
        // leer archivo como DataURL y poner en payload.imagen
        try {
          payload.imagen = await fileToDataURL(file);
        } catch (err) {
          console.error("Error leyendo archivo:", err);
          alert("Error al leer la imagen seleccionada.");
          return;
        }
      }

      try {
        const res = await fetch(`/animal/editar/${idEditar}`, {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        });

        const texto = await res.text();
        if (res.ok) {
          mostrarMensaje(texto || "Animal actualizado correctamente", "success");
          
          // Auto-cerrar el mensaje después de 8 segundos
          setTimeout(() => {
            const alert = document.querySelector('#mensajeRespuesta .alert');
            if (alert) {
              const bsAlert = new bootstrap.Alert(alert);
              bsAlert.close();
            }
          }, 8000);
          
          this.reset();
          delete this.dataset.editando;
          delete this.dataset.existingImagen;
          previewAnimal.style.display = 'none';
          cargarAnimales();
          mostrarSeccion("animales");
        } else {
          mostrarMensaje("Error al actualizar: " + texto, "danger");
        }
      } catch (err) {
        console.error("Error al actualizar animal:", err);
        alert("Error al conectar con el servidor");
      }

    } else {
      // Si estamos CREANDO -> mantener el flujo existente (multipart/form-data)
      const formData = new FormData();
      formData.append("nombre", document.getElementById('nombreAnimal').value);
      formData.append("edad", document.getElementById('edadAnimal').value);
      formData.append("raza", document.getElementById('razaAnimal').value);
      formData.append("tipo_animal", document.getElementById('tipoAnimal').value);

      const file = imagenAnimalInput.files[0];
      if (file) formData.append("imagen", file);

      try {
        const res = await fetch('/animal/crear', {
          method: 'POST',
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
          
          this.reset();
          previewAnimal.style.display = 'none';
          cargarAnimales();
          mostrarSeccion("animales");
        } else {
          mostrarMensaje("Error: " + mensaje, "danger");
        }
      } catch (err) {
        console.error("Error al guardar animal:", err);
        alert("Error al conectar con el servidor");
      }
    }
  });

  // Helper: convertir file -> DataURL (promesa)
  function fileToDataURL(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onerror = () => reject(new Error("No se pudo leer el archivo"));
      reader.onload = () => resolve(reader.result);
      reader.readAsDataURL(file);
    });
  }

  // Cargar animales (ahora SOLO ACTIVO)
  function cargarAnimales() {
    // pedimos sólo los ACTIVO (endpoint que ya tienes)
    fetch("/animal/mis-animales-estado?estados=ACTIVO", { credentials: 'include' })
      .then(res => {
        if (!res.ok) throw new Error("Error al obtener animales: " + res.status);
        return res.json();
      })
      .then(animales => {
        console.log("Animales recibidos:", animales); // ✅ Log para depurar

        const tableBody = document.getElementById("animalTableBody");
        tableBody.innerHTML = "";

        if (!animales || animales.length === 0) {
          tableBody.innerHTML = `
            <tr>
              <td colspan="6" class="text-center text-muted">No hay animales registrados.</td>
            </tr>`;
          return;
        }

        animales.forEach(animal => {
          const row = document.createElement("tr");
          // Si imagen viene en base64 o como ruta, funciona como src
          const imgSrc = animal.imagen || './img/animalDefault.jpg';

          row.innerHTML = `
            <td>${escapeHtml(animal.nombre)}</td>
            <td>${animal.edad} años</td>
            <td>${escapeHtml(animal.raza)}</td>
            <td>${escapeHtml(animal.tipo_animal)}</td>
            <td><img src="${imgSrc}" alt="${escapeHtml(animal.nombre)}" style="width: 60px; height: 60px; object-fit: cover;"></td>
            <td>
              <button onclick="editarAnimal(${animal.id})" class="btn btn-primary btn-sm me-1">Editar</button>
              <button onclick="eliminarAnimal(${animal.id})" class="btn btn-danger btn-sm">Eliminar</button>
            </td>
          `;
          tableBody.appendChild(row);
        });
      })
      .catch(err => {
        console.error("Error al cargar animales:", err);
      });
  }

  // Escape simple para prevenir XSS si los datos vienen del servidor
  function escapeHtml(unsafe) {
    if (unsafe == null) return '';
    return unsafe
      .toString()
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  // Eliminar animal
  window.eliminarAnimal = function (id) {
    // Guardar el ID del animal a eliminar
    const btnConfirmar = document.getElementById('btnConfirmarEliminarAnimal');
    
    // Mostrar el modal
    const modalEliminar = new bootstrap.Modal(document.getElementById('modalEliminarAnimal'));
    modalEliminar.show();
    
    // Remover listeners anteriores para evitar duplicados
    const nuevoBtn = btnConfirmar.cloneNode(true);
    btnConfirmar.replaceWith(nuevoBtn);
    
    // Agregar el evento de confirmación
    nuevoBtn.addEventListener('click', function() {
      fetch(`/animal/eliminar/${id}`, {
        method: 'DELETE',
        credentials: 'include'
      })
        .then(async res => {
          if (res.ok) {
            mostrarMensaje("Animal eliminado", "danger");
            // Auto-cerrar el mensaje después de 6 segundos
            setTimeout(() => {
              const alert = document.querySelector('#mensajeRespuesta .alert');
              if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
              }
            }, 6000);
            cargarAnimales();
          } else {
            const texto = await res.text();
            mostrarMensaje(texto, "danger");
          }
        })
        .catch(err => {
          console.error("Error al eliminar animal:", err);
          mostrarMensaje("Error al conectar con el servidor", "danger");
        });
      
      // Cerrar el modal
      modalEliminar.hide();
    });
  };

  // Editar animal
  window.editarAnimal = function (id) {
    fetch(`/animal/editar/${id}`, { credentials: 'include' })
      .then(res => {
        if (!res.ok) throw new Error("No se pudo obtener el animal");
        return res.json();
      })
      .then(animal => {
        // llenar formulario con todos los campos
        document.getElementById('nombreAnimal').value = animal.nombre || '';
        document.getElementById('edadAnimal').value = animal.edad || '';
        document.getElementById('razaAnimal').value = animal.raza || '';
        document.getElementById('tipoAnimal').value = animal.tipo_animal || '';
        // guardar imagen existente en dataset para reusarla si no suben nueva
        const form = document.getElementById('formCrearAnimal');
        form.dataset.editando = id;
        // almacenar la imagen actual (ruta o base64)
        form.dataset.existingImagen = animal.imagen || '';
        mostrarSeccion('nuevoAnimal');

        if (animal.imagen) {
          previewAnimal.src = animal.imagen;
          previewAnimal.style.display = 'block';
        } else {
          previewAnimal.src = '#';
          previewAnimal.style.display = 'none';
        }
      })
      .catch(err => {
        console.error("Error al obtener animal:", err);
        alert("Error al obtener los datos del animal.");
      });
  };

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

  // Subida por CSV
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
        
        cargarAnimales();
        mostrarSeccion("animales");

      } catch (error) {
        console.error("Error en fetch:", error);
        mostrarMensaje("Error inesperado al subir el archivo", "danger");
      }
    });
  }

  // limpia formulario al crear nuevo
  function limpiarFormulario() {
    const form = document.getElementById('formCrearAnimal');
    form.reset();
    delete form.dataset.editando;
    delete form.dataset.existingImagen;
    previewAnimal.style.display = 'none';
    previewAnimal.src = '#';
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
    
    // Mostrar el total de animales
    document.getElementById('totalAnimales').textContent = dataLines.length;
    
    // Mostrar la sección de previsualización
    document.getElementById('csvPreview').classList.remove('d-none');
    
    // Habilitar el botón de subir
    document.getElementById('btnSubirCsv').disabled = false;
  }
});
