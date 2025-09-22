// ✅ Función global para mostrar secciones
function mostrarSeccion(id) {
  const sections = document.querySelectorAll("main section");
  sections.forEach(section => section.classList.add("d-none"));
  const target = document.getElementById(id);
  if (target) target.classList.remove("d-none");
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

    const formData = new FormData();
    formData.append("nombre", document.getElementById('nombreAnimal').value);
    formData.append("edad", document.getElementById('edadAnimal').value);
    formData.append("raza", document.getElementById('razaAnimal').value);
    formData.append("tipo_animal", document.getElementById('tipoAnimal').value);

    const file = imagenAnimalInput.files[0];
    if (file) formData.append("imagen", file);

    const idEditar = this.dataset.editando;
    const url = idEditar ? `/animal/editar/${idEditar}` : '/animal/crear';
    const metodo = idEditar ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method: metodo,
        body: formData,
        credentials: 'include'
      });

      const mensaje = await res.text();
      if (res.ok) {
        alert(mensaje);
        this.reset();
        previewAnimal.style.display = 'none';
        delete this.dataset.editando;
        cargarAnimales();
        mostrarSeccion("animales");
      } else {
        alert("Error: " + mensaje);
      }
    } catch (err) {
      console.error("Error al guardar animal:", err);
      alert("Error al conectar con el servidor");
    }
  });

  // Cargar animales
  function cargarAnimales() {
    fetch("/animal/mis-animales")
      .then(res => res.json())
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
          row.innerHTML = `
            <td>${animal.nombre}</td>
            <td>${animal.edad} años</td>
            <td>${animal.raza}</td>
            <td>${animal.tipo_animal}</td>
            <td><img src="${animal.imagen || './img/animalDefault.jpg'}" alt="${animal.nombre}" style="width: 60px; height: 60px; object-fit: cover;"></td>
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

  // Eliminar animal
  window.eliminarAnimal = function (id) {
    if (confirm("¿Estás seguro de eliminar este animal?")) {
      fetch(`/animal/eliminar/${id}`, {
        method: "DELETE"
      })
      .then(res => {
        if (res.ok) {
          cargarAnimales();
        } else {
          alert("Error al eliminar el animal");
        }
      });
    }
  };

  // Editar animal
  window.editarAnimal = function (id) {
    fetch(`/animal/editar/${id}`)
      .then(res => res.json())
      .then(animal => {
        document.getElementById('nombreAnimal').value = animal.nombre;
        document.getElementById('edadAnimal').value = animal.edad;
        document.getElementById('razaAnimal').value = animal.raza;
        document.getElementById('tipoAnimal').value = animal.tipo_animal;
        document.getElementById('formCrearAnimal').dataset.editando = id;
        mostrarSeccion('nuevoAnimal');

        if (animal.imagen) {
          document.getElementById('previewAnimal').src = animal.imagen;
          document.getElementById('previewAnimal').style.display = 'block';
        }
      })
      .catch(err => {
        console.error("Error al obtener animal:", err);
        alert("Error al obtener los datos del animal.");
      });
  };

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
        document.getElementById("mensajeRespuesta").innerHTML =
          `<div class="alert alert-info">${mensaje}</div>`;

        bootstrap.Modal.getInstance(document.getElementById('uploadModal')).hide();
        cargarAnimales();
        mostrarSeccion("animales");

      } catch (error) {
        console.error("Error en fetch:", error);
        document.getElementById("mensajeRespuesta").innerHTML =
          `<div class="alert alert-danger">Error inesperado al subir el archivo</div>`;
      }
    });
  }
});
