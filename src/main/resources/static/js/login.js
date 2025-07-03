// login.js

document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("form");

  form.addEventListener("submit", (e) => {
    const email = document.getElementById("correo").value.trim();
    const password = document.getElementById("contrasena").value.trim();

    if (email === "" || password === "") {
      e.preventDefault();
      Swal.fire({
        icon: "warning",
        title: "Campos vacíos",
        text: "Por favor ingresa tu correo y contraseña.",
        confirmButtonColor: "#3085d6",
      });
    }
  });
});
