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
      if (target === "adopciones") cargarAdopciones();
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

  // Helper global redundante para manejar clicks desde el atributo onclick (debug)
  window._handleVerAdopcion = async function(id) {
    console.log('[DEBUG-inline] _handleVerAdopcion called with id=', id);
    if (!id) {
      mostrarMensaje('ID de adopción inválido (inline).', 'warning');
      return;
    }
    mostrarMensaje('Cargando detalle (inline)...', 'info');
    try {
      const res = await fetch(`/adopcion/detalle-json/${id}`, { credentials: 'include' });
      if (!res.ok) {
        const txt = await res.text().catch(() => '');
        throw new Error('Error: ' + res.status + ' ' + txt);
      }
      const data = await res.json();
      console.log('[DEBUG-inline] detalle recibido:', data);
      populateDetalle(data);
      mostrarSeccion('detalleAdopcion');
      mostrarMensaje('Detalle cargado (inline).', 'success');
    } catch (err) {
      console.error('[DEBUG-inline] error al cargar detalle:', err);
      mostrarMensaje('Error al cargar detalle (inline).', 'danger');
    }
  };

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
        imagen: this.dataset.existingImagen || null, // valor por defecto
        estadoNombre: document.getElementById('estadoAnimal').value || null
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
          document.getElementById('divEstadoAnimal').classList.add('d-none');
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
                <button class="btn btn-info btn-sm me-1 ver-historial-btn" data-animal-id="${animal.id}">Historial</button>
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

  // Cargar adopciones (PENDIENTE | ADOPTADO) para la fundación autenticada
  function cargarAdopciones() {
    fetch("/animal/mis-animales-estado?estados=PENDIENTE&estados=ADOPTADO", { credentials: 'include' })
      .then(res => {
        if (!res.ok) throw new Error("Error al obtener adopciones: " + res.status);
        return res.json();
      })
      .then(animales => {
        // Guardar datos completos en variable global para filtrar
        window.adopcionesCompletas = animales || [];
        renderAdopciones(animales);
      })
      .catch(err => {
        console.error("Error al cargar adopciones:", err);
      });
  }

  // Renderizar adopciones con filtro
  function renderAdopciones(animales) {
    const filtro = document.getElementById('filtroEstadoAdopcion')?.value || '';
    const animalesFiltrados = filtro 
      ? animales.filter(a => a.estadoNombre === filtro)
      : animales;

    const cont = document.getElementById("adopcionesContainer");
    cont.innerHTML = "";

    if (!animalesFiltrados || animalesFiltrados.length === 0) {
      cont.innerHTML = `
        <div class="col-12">
          <div class="d-flex flex-column justify-content-center align-items-center text-center" style="min-height: 200px;">
            <i class="fas fa-paw fa-3x text-muted mb-3"></i>
            <p class="h5 text-muted">No hay adopciones con este estado.</p>
          </div>
        </div>`;
      return;
    }

    animalesFiltrados.forEach(a => {
      const col = document.createElement("div");
      col.className = "col";
      const imgSrc = a.imagen || '/img/animalDefault.jpg';

      col.innerHTML = `
        <div class="card h-100">
          <img src="${imgSrc}" class="card-img-top" alt="${escapeHtml(a.nombre)}" style="height:200px;object-fit:cover;">
          <div class="card-body">
            <h5 class="card-title">${escapeHtml(a.nombre)}</h5>
            <p class="card-text"><strong>Edad:</strong> ${a.edad ?? '-'} años</p>
            <p class="card-text"><strong>Raza:</strong> ${escapeHtml(a.raza)}</p>
            <p class="card-text"><strong>Tipo:</strong> ${escapeHtml(a.tipo_animal)}</p>
            <p class="card-text"><strong>Estado:</strong> ${escapeHtml(a.estadoNombre || '')}</p>
            ${a.descripcion ? `<p class="card-text"><strong>Descripción:</strong> ${escapeHtml(a.descripcion)}</p>` : ''}
          </div>
            <div class="card-footer text-center">
            <button class="btn btn-primary ver-adopcion-btn" data-id="${a.id}" onclick="window._handleVerAdopcion(${a.id})">Ver adopción</button>
          </div>
        </div>`;

      cont.appendChild(col);
    });
  }

  // Delegación robusta: escuchar en el documento para capturar cualquier botón dinámico
  document.addEventListener('click', function (e) {
    const btn = e.target.closest && e.target.closest('.ver-adopcion-btn');
    if (!btn) return;

    const id = btn.getAttribute('data-id');
    console.log('[DEBUG] Ver adopción clicked, id=', id, 'element=', btn);
    if (!id) {
      console.warn('[DEBUG] botón ver-adopcion sin data-id');
      mostrarMensaje('ID de adopción no encontrado en el botón.', 'warning');
      return;
    }

    // Mostrar un indicador mientras carga
    mostrarMensaje('Cargando detalle...', 'info');

    // Siempre pedir al servidor el detalle (garantiza que venga la última adopción)
    fetch(`/adopcion/detalle-json/${id}`, { credentials: 'include' })
      .then(async res => {
        if (!res.ok) {
          const txt = await res.text().catch(() => '');
          throw new Error('Error al obtener detalle: ' + res.status + ' ' + txt);
        }
        const data = await res.json();
        console.log('[DEBUG] detalle recibido:', data);
        populateDetalle(data);
        mostrarSeccion('detalleAdopcion');
        mostrarMensaje('Detalle cargado.', 'success');
      })
      .catch(err => {
        console.error('Error al cargar detalle:', err);
        mostrarMensaje('No se pudo cargar el detalle. Revisa la consola y Network.', 'danger');
      });
  });

  // Delegación para botones 'Historial' en la tabla de animales (fundación)
  document.addEventListener('click', function (e) {
    const btn = e.target.closest && e.target.closest('.ver-historial-btn');
    if (!btn) return;
    const animalId = btn.dataset.animalId;
    if (!animalId) {
      mostrarMensaje('ID de animal no encontrado para historial.', 'warning');
      return;
    }
    mostrarHistorialFundacion(animalId);
  });

  // Mostrar historial en modal específico para la fundación
  async function mostrarHistorialFundacion(animalId) {
    try {
      const modalEl = new bootstrap.Modal(document.getElementById('modalHistorialFundacion'));
      const container = document.getElementById('historialFundacionContainer');
      const wrapper = document.getElementById('historialFundacionWrapper');
      container.innerHTML = `<div style="min-width:220px;">Cargando historial...</div>`;
      modalEl.show();

      const res = await fetch(`/adopcion/historial/${animalId}`, { credentials: 'include' });
      if (!res.ok) {
        container.innerHTML = `<div class="text-muted">Error al cargar historial (status ${res.status})</div>`;
        return;
      }
      const data = await res.json();
      if (!Array.isArray(data) || data.length === 0) {
        container.innerHTML = `<div class="text-muted">No hay solicitudes de adopción para este animal.</div>`;
        return;
      }

      container.innerHTML = data.map(d => {
        const fecha = d.fecha ? new Date(d.fecha).toLocaleString() : 'Sin fecha';
        const estado = d.estado || 'Sin estado';
        const nombre = (d.nombre || '') + ' ' + (d.apellido || '');
        return `
          <div style="min-width:260px; background:#fff; border-radius:8px; box-shadow:0 6px 18px rgba(0,0,0,0.08); padding:12px;">
            <div style="font-size:0.85rem; color:#6c757d; margin-bottom:8px;">${fecha}</div>
            <div style="font-weight:700; margin-bottom:6px;">${estado}</div>
            <div><strong>${nombre.trim() || 'Solicitante anónimo'}</strong></div>
            <div style="font-size:0.9rem; color:#444; margin-top:6px;">${d.email ? d.email + '<br/>' : ''}${d.telefono ? d.telefono + '<br/>' : ''}${d.direccion ? d.direccion : ''}</div>
          </div>
        `;
      }).join('');

      if (wrapper) wrapper.scrollLeft = 0;
    } catch (err) {
      console.error('Error mostrarHistorialFundacion', err);
      document.getElementById('historialFundacionContainer').innerHTML = `<div class="text-muted">Error al cargar historial</div>`;
    }
  }

  // Rellenar la sección de detalle con los datos recibidos
  function populateDetalle(data) {
    const adopcion = data.adopcion;
    const animal = data.animal;

    // Foto del animal (si existe), fallback a imagen por defecto
    const animalFoto = document.getElementById('animalFoto');
    if (animalFoto) {
      const imgSrc = animal && (animal.imagen || animal.foto || animal.image) ? (animal.imagen || animal.foto || animal.image) : '/img/animalDefault.jpg';
      animalFoto.src = imgSrc;
      animalFoto.alt = animal ? (animal.nombre || 'Foto animal') : 'Foto animal';
      animalFoto.style.display = 'block';
    }

    // Adoptante
    document.getElementById('adoptanteNombre').textContent = adopcion ? (adopcion.nombre + ' ' + (adopcion.apellido || '')) : '-';
    document.getElementById('adoptanteEmail').textContent = adopcion ? (adopcion.email || '-') : '-';
    document.getElementById('adoptanteTelefono').textContent = adopcion ? (adopcion.telefono || '-') : '-';
    document.getElementById('adoptanteDireccion').textContent = adopcion ? (adopcion.direccion || '-') : '-';
    document.getElementById('adopcionFecha').textContent = adopcion ? (adopcion.fecha || '-') : '-';

    // Animal
    document.getElementById('animalNombre').textContent = animal ? (animal.nombre || '-') : '-';
    document.getElementById('animalEspecie').textContent = animal ? (animal.tipo_animal || '-') : '-';
    document.getElementById('animalRaza').textContent = animal ? (animal.raza || '-') : '-';
    document.getElementById('animalEdad').textContent = animal ? (animal.edad || '-') : '-';

    // Guardar ids en botones para acciones
    const btnFinalizar = document.getElementById('btnFinalizarAdopcion');
    const btnAprobar = document.getElementById('btnAprobarAdopcion');
    btnFinalizar.dataset.adopcionId = adopcion ? adopcion.id : '';
    btnAprobar.dataset.adopcionId = adopcion ? adopcion.id : '';
    btnFinalizar.dataset.animalId = animal ? animal.id : '';
    btnAprobar.dataset.animalId = animal ? animal.id : '';

    // Mostrar/ocultar botones según estado de la adopción (más permissivo y con logs)
    try {
      console.log('[DEBUG populateDetalle] adopcion:', adopcion, 'animal:', animal);
      const adopcionEstadoRaw = adopcion && typeof adopcion.estado !== 'undefined' && adopcion.estado !== null ? String(adopcion.estado) : '';
      const animalEstadoRaw = animal && typeof animal.estado !== 'undefined' && animal.estado !== null ? String(animal.estado) : '';
      console.log('[DEBUG populateDetalle] adopcion.estado raw:', adopcionEstadoRaw, 'animal.estado raw:', animalEstadoRaw);
      const adopcionEstado = adopcionEstadoRaw.trim().toUpperCase();
      const animalEstado = animalEstadoRaw.trim().toUpperCase();

      if (!btnFinalizar || !btnAprobar) {
        console.warn('Botones de acción no encontrados en el DOM');
      } else if (!adopcion || adopcionEstado === '') {
        // No hay adopción -> ocultar ambos
        btnAprobar.classList.add('d-none');
        btnFinalizar.classList.add('d-none');
      } else if (adopcionEstado.includes('PENDIENTE') || /PENDIENTE/i.test(adopcionEstadoRaw) || animalEstado.includes('PENDIENTE') || /PENDIENTE/i.test(animalEstadoRaw)) {
        // Si la adopción o el animal están en PENDIENTE: mostrar ambos botones (Aprobar + Finalizar)
        console.log('[DEBUG populateDetalle] Estado detectado como PENDIENTE en adopcion o animal -> mostrar ambos botones');
        btnAprobar.classList.remove('d-none');
        btnFinalizar.classList.remove('d-none');
      } else if (adopcionEstado.includes('ACTIVO') || adopcionEstado.includes('ADOPTADO')) {
        // Si la adopción ya está activa/adoptada, solo mostrar Finalizar
        btnAprobar.classList.add('d-none');
        btnFinalizar.classList.remove('d-none');
      } else {
        // Por defecto mostrar ambos
        btnAprobar.classList.remove('d-none');
        btnFinalizar.classList.remove('d-none');
      }
    } catch (e) {
      console.error('Error al decidir visibilidad de botones:', e);
    }
  }

  // Botón volver a lista de adopciones
  document.getElementById('btnVolverAdopciones')?.addEventListener('click', function () {
    mostrarSeccion('adopciones');
  });

  // Acciones: finalizar y aprobar
  document.getElementById('btnFinalizarAdopcion')?.addEventListener('click', function () {
    const id = this.dataset.adopcionId;
    if (!id) { mostrarMensaje('No hay solicitud para finalizar.', 'warning'); return; }
    fetch(`/adopcion/finalizar/${id}`, { method: 'POST', credentials: 'include' })
      .then(async res => {
        const txt = await res.text();
        if (res.ok) {
          mostrarMensaje(txt || 'Adopción finalizada', 'success');
          cargarAdopciones();
          mostrarSeccion('adopciones');
        } else {
          mostrarMensaje(txt || 'Error al finalizar', 'danger');
        }
      })
      .catch(err => { console.error(err); mostrarMensaje('Error al conectar', 'danger'); });
  });

  document.getElementById('btnAprobarAdopcion')?.addEventListener('click', function () {
    const id = this.dataset.adopcionId;
    if (!id) { mostrarMensaje('No hay solicitud para aprobar.', 'warning'); return; }
    fetch(`/adopcion/aprobar/${id}`, { method: 'POST', credentials: 'include' })
      .then(async res => {
        const txt = await res.text();
        if (res.ok) {
          mostrarMensaje(txt || 'Adopción aprobada', 'success');
          cargarAdopciones();
          mostrarSeccion('adopciones');
        } else {
          mostrarMensaje(txt || 'Error al aprobar', 'danger');
        }
      })
      .catch(err => { console.error(err); mostrarMensaje('Error al conectar', 'danger'); });
  });

  // Evento cambio filtro adopciones
  document.getElementById('filtroEstadoAdopcion')?.addEventListener('change', () => {
    renderAdopciones(window.adopcionesCompletas);
  });

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
        // Cambiar título del formulario
        document.getElementById('tituloFormulario').textContent = 'Editar Animal';
        // llenar formulario con todos los campos
        document.getElementById('nombreAnimal').value = animal.nombre || '';
        document.getElementById('edadAnimal').value = animal.edad || '';
        document.getElementById('razaAnimal').value = animal.raza || '';
        document.getElementById('tipoAnimal').value = animal.tipo_animal || '';
        // Mostrar y llenar el campo de estado
        document.getElementById('divEstadoAnimal').classList.remove('d-none');
        document.getElementById('estadoAnimal').value = animal.estadoNombre || '';
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
    document.getElementById('tituloFormulario').textContent = 'Registrar Nuevo Animal';
    document.getElementById('divEstadoAnimal').classList.add('d-none');
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
