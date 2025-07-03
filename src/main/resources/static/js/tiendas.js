// --------------------- CARRITO ---------------------
let cart = [];

// Función para asignar eventos a los botones "Agregar al carrito"
function asignarEventosAgregarAlCarrito() {
  document.querySelectorAll('.add-to-cart').forEach(button => {
    button.addEventListener('click', function () {
      const productCard = this.closest('.card');
      const title = productCard.querySelector('.card-title').innerText;
      const priceText = productCard.querySelector('.text-danger').innerText;
      const price = parseInt(priceText.replace(/[^0-9]/g, ''));
      const imgSrc = productCard.querySelector('img').src;
      const storeName = productCard.closest('.producto').dataset.store || "Tienda Desconocida";
      const storeWhatsapp = productCard.closest('.producto').dataset.store || "573159143399";

      // Verifica si ya existe el mismo producto con mismo nombre y precio
      const item = cart.find(p => p.title === title && p.price === price);
      if (item) {
        item.quantity += 1;
      } else {
        cart.push({ title, price, quantity: 1, imgSrc, storeName, storeWhatsapp });
      }

      updateCart();
    });
  });
}

function updateCart() {
  const cartItems = document.getElementById('cartItems');
  const cartCount = document.getElementById('cartCount');
  const cartTotal = document.getElementById('cartTotal');

  if (cart.length === 0) {
    cartItems.innerHTML = '<p>No hay productos en el carrito.</p>';
    cartTotal.innerText = '$0';
    cartCount.innerText = '0';
    return;
  }

  let html = '<ul class="list-group">';
  let total = 0;
  cart.forEach((item, index) => {
    total += item.price * item.quantity;
    const encodedTitle = encodeURIComponent(item.title);

    const whatsappLink = `https://wa.me/${item.storeWhatsapp}?text=Hola,%20quiero%20comprar%20este%20producto%20"${encodedTitle}"%20por%20$${item.price.toLocaleString()}.%20Producto:%20${item.imgSrc}`;

    html += `
      <li class="list-group-item d-flex justify-content-between align-items-start flex-column">
        <div class="d-flex w-100 justify-content-between">
          <img src="${item.imgSrc}" alt="${item.title}" style="width: 60px; height: auto; margin-right: 10px;">
          <strong>${item.title}</strong>
          <span class="badge bg-danger">${item.quantity} x $${item.price.toLocaleString()}</span>
        </div>
        <div class="mt-2 d-flex gap-2">
          <button class="btn btn-sm btn-outline-danger" onclick="removeItem(${index})">Eliminar</button>
          <a href="${whatsappLink}" target="_blank" class="btn btn-sm btn-success">Comprar por WhatsApp</a>
        </div>
      </li>
    `;
  });

  html += '</ul>';
  cartItems.innerHTML = html;
  cartTotal.innerText = `$${total.toLocaleString()}`;
  cartCount.innerText = cart.reduce((sum, item) => sum + item.quantity, 0);
}

function removeItem(index) {
  cart.splice(index, 1);
  updateCart();
}

// --------------------- FILTRO POR CATEGORÍA ---------------------
document.getElementById('categoryFilter').addEventListener('change', function () {
  const selected = this.value;
  document.querySelectorAll('.producto').forEach(p => {
    const cat = p.getAttribute('data-category');
    p.style.display = selected === 'todos' || selected === cat ? 'block' : 'none';
  });
});

// --------------------- BÚSQUEDA DE PRODUCTOS ---------------------
const searchInput = document.getElementById('searchInput');

searchInput.addEventListener('input', () => {
  const filter = searchInput.value.toLowerCase();
  document.querySelectorAll('.producto').forEach(product => {
    const text = product.innerText.toLowerCase();
    product.style.display = text.includes(filter) ? '' : 'none';
  });
});

// --------------------- CARGA DE PRODUCTOS ---------------------
document.addEventListener("DOMContentLoaded", function () {
  fetch("http://localhost:8085/productos")
    .then(response => response.json())
    .then(data => {
      console.log("Productos obtenidos del backend:", data); // <-- LÍNEA AÑADIDA

      const container = document.getElementById("productContainer");
      container.innerHTML = "";

      data.forEach(producto => {
        const card = document.createElement("div");
        card.className = "col-12 col-sm-6 col-md-4 col-lg-3 col-xl-2 producto";
        card.setAttribute("data-category", producto.tipoProducto.toLowerCase());

        card.innerHTML = `
          <div class="card h-100 shadow-sm">
            <img src="${producto.imagen || './img/default.jpg'}" class="card-img-top" alt="${producto.nombre}">
            <div class="card-body">
              <h5 class="card-title">${producto.nombre}</h5>
              <p class="card-text text-muted">${producto.descripcion}</p>
              <span class="badge badge-category">${producto.tipoProducto}</span>
              <p class="mt-2 fw-bold text-danger">$${producto.precio.toLocaleString()}</p>
              <button class="btn btn-outline-danger w-100 add-to-cart">Agregar al carrito</button>
            </div>
          </div>
        `;
        container.appendChild(card);
      });

      // Reasignar eventos a botones recién creados
      asignarEventosAgregarAlCarrito();
    })
    .catch(error => {
      console.error("Error al cargar productos:", error);
    });
});
