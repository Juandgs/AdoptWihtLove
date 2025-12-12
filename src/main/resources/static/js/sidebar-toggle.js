document.addEventListener('DOMContentLoaded', function () {
  const toggle = document.getElementById('sidebarToggle');
  const body = document.body;
  let overlay = document.getElementById('sidebarOverlay');

  // Crear overlay si no existe
  if (!overlay) {
    overlay = document.createElement('div');
    overlay.id = 'sidebarOverlay';
    overlay.className = 'sidebar-overlay';
    document.body.appendChild(overlay);
  }

  function openMenu() {
    body.classList.add('menu-open');
    overlay.classList.add('visible');
  }

  function closeMenu() {
    body.classList.remove('menu-open');
    overlay.classList.remove('visible');
  }

  // Toggle (mobile behavior)
  toggle?.addEventListener('click', function (e) {
    e.preventDefault();
    if (body.classList.contains('menu-open')) closeMenu();
    else openMenu();
  });

  // Cerrar al hacer click fuera
  overlay.addEventListener('click', function () {
    closeMenu();
  });

  // Cerrar con ESC
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && body.classList.contains('menu-open')) closeMenu();
  });

  // Si redimensionan a desktop, aseguramos cerrar el menu mobile
  window.addEventListener('resize', function () {
    if (window.innerWidth > 991) {
      closeMenu();
    }
  });
});
