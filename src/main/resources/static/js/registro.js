const tipoUsuario = document.getElementById('tipoUsuario');
const camposAdicionales = document.getElementById('camposAdicionales');
const precioSuscripcion = document.getElementById('precioSuscripcion');
const labelNit = document.getElementById('labelNit');
const labelNombre = document.getElementById('labelNombre');
const labelEncargado = document.getElementById('labelEncargado');

tipoUsuario.addEventListener('change', () => {
  let tipo = tipoUsuario.value;

  if (tipo === "tienda" || tipo === "fundacion") {
    camposAdicionales.style.display = "block";

    if (tipo === "tienda") {
      labelNit.innerText = "NIT o Cédula de la Tienda";
      labelNombre.innerText = "Nombre de la Tienda";
      labelEncargado.innerText = "Nombre del Encargado";
      precioSuscripcion.innerText = "Precio de suscripción: $30.000";
    } else {
      labelNit.innerText = "NIT o Cédula de la Fundación";
      labelNombre.innerText = "Nombre de la Fundación";
      labelEncargado.innerText = "Nombre del Representante";
      precioSuscripcion.innerText = "Precio de suscripción: $20.000";
    }
    precioSuscripcion.style.display = "block";

  } else {
    camposAdicionales.style.display = "none";
    precioSuscripcion.style.display = "none";
  }
});

document.getElementById("registroForm").addEventListener("submit", function(e) {
  e.preventDefault();

  const contrasena = document.getElementById("contrasena").value;
  const confirmar = document.getElementById("confirmarContrasena").value;
  const archivo = document.getElementById("archivo").files[0];
  const terminos = document.getElementById("terminos").checked;
  const tipoSeleccionado = tipoUsuario.value;

  // Validar contraseñas
  if (contrasena !== confirmar) {
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "Las contraseñas no coinciden"
    });
    return;
  }

  // Validar archivo
  if (!archivo) {
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "Debe subir un archivo PDF"
    });
    return;
  }

  // Validar términos y condiciones
  if (!terminos) {
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "Debe aceptar los términos y condiciones"
    });
    return;
  }

  // Registro exitoso
  Swal.fire({
    icon: "success",
    title: "¡Registro exitoso!",
    text: "Tu cuenta ha sido registrada correctamente",
    confirmButtonText: "Aceptar"
  }).then(() => {
    // Redirección según tipo de usuario
    if (tipoSeleccionado === "tienda") {
      window.location.href = "dashboard_tienda.html"; // Cambia la ruta si es necesario
    } else if (tipoSeleccionado === "fundacion") {
      window.location.href = "dashboard_fundacion.html"; // Cambia la ruta si es necesario
    }
  });
});
