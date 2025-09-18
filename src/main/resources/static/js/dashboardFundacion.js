document.addEventListener("DOMContentLoaded", function () {
  const links = document.querySelectorAll("aside a");
  const sections = document.querySelectorAll("main section");

  const primera = document.querySelector("main section");
  if (primera) primera.classList.remove("d-none");

  links.forEach(link => {
    link.addEventListener("click", function (e) {
      e.preventDefault();
      const target = link.getAttribute("data-target");

      sections.forEach(section => {
        section.classList.toggle("d-none", section.id !== target);
      });

      links.forEach(l => l.classList.remove("active"));
      link.classList.add("active");
    });
  });
});

//

document.addEventListener("DOMContentLoaded", () => {
  fetch("/animal/mis-animales")
    .then(res => res.json())
    .then(animales => {
      const tableBody = document.getElementById("animalTableBody");
      tableBody.innerHTML = "";

      if (animales.length === 0) {
        tableBody.innerHTML = `
          <tr>
            <td colspan="7" class="text-center text-muted">No hay animales registrados.</td>
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
});


function eliminarAnimal(id) {
  if (confirm("¿Estás seguro de eliminar este animal?")) {
    fetch(`/animal/eliminar/${id}`, {
      method: "DELETE"
    })
    .then(res => {
      if (res.ok) {
        location.reload();
      } else {
        alert("Error al eliminar el animal");
      }
    });
  }
}

// -----------------------------------

// Reutiliza función global de cambiar sección
  function mostrarSeccion(id) {
    const sections = document.querySelectorAll("main section");
    sections.forEach(section => section.classList.add("d-none"));
    const target = document.getElementById(id);
    if (target) target.classList.remove("d-none");
  }

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

  document.getElementById('formCrearAnimal')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    let base64;
const file = imagenAnimalInput.files[0];

if (file) {
  base64 = await toBase64(file);
} else {
  base64 = document.getElementById('previewAnimal').src; // usa imagen ya cargada
}

    const data = {
      nombre: document.getElementById('nombreAnimal').value,
      edad: parseInt(document.getElementById('edadAnimal').value),
      raza: document.getElementById('razaAnimal').value,
      tipo_animal: document.getElementById('tipoAnimal').value,
      imagen: base64
    };

    const idEditar = this.dataset.editando;
const url = idEditar ? `/animal/editar/${idEditar}` : '/animal/crear';
const metodo = idEditar ? 'PUT' : 'POST';

fetch(url, {
  method: metodo,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data),
  credentials: 'include'
})

    .then(res => {
      if (res.ok) {
        alert('Animal registrado con éxito');
        location.reload();
      } else {
        alert('Error al registrar el animal');
      }
    });
  });

  function toBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = error => reject(error);
    });
  }

  function editarAnimal(id) {
  fetch(`/animal/editar/${id}`)
    .then(res => res.json())
    .then(animal => {
      document.getElementById('nombreAnimal').value = animal.nombre;
      document.getElementById('edadAnimal').value = animal.edad;
      document.getElementById('razaAnimal').value = animal.raza;
      document.getElementById('tipoAnimal').value = animal.tipo_animal;
      document.getElementById('formCrearAnimal').dataset.editando = id; // guardar ID
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
}


document.addEventListener("DOMContentLoaded", () => {
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

      console.log("Estado:", response.status);

      const mensaje = await response.text();
      console.log("Mensaje recibido:", mensaje);

      document.getElementById("mensajeRespuesta").innerHTML =
        `<div class="alert alert-info">${mensaje}</div>`;

      const modal = bootstrap.Modal.getInstance(document.getElementById('uploadModal'));
      modal.hide();

      cargarAnimales();
      mostrarSeccion("animales");

    } catch (error) {
      console.error("Error en fetch:", error);
      document.getElementById("mensajeRespuesta").innerHTML =
        `<div class="alert alert-danger">Error inesperado al subir el archivo</div>`;
    }
  });
}}
);

function cargarAnimales() {
  fetch("/animal/mis-animales")
    .then(res => res.json())
    .then(animales => {
      const tableBody = document.getElementById("animalTableBody");
      tableBody.innerHTML = "";

      if (animales.length === 0) {
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
