// --------------------- CARRITO ---------------------
let cart = [];

// Función para asignar eventos a los botones "Agregar al carrito"
function asignarEventosAgregarAlCarrito() {
  document.querySelectorAll('.add-to-cart').forEach(button => {
    button.addEventListener('click', function () {
      const productCard = this.closest('.product-card');
      const id = productCard.dataset.id;
      const title = productCard.querySelector('.product-title').innerText;
      const priceText = productCard.querySelector('.product-price').innerText;
      const price = parseInt(priceText.replace(/[^0-9]/g, ''));
      const imgSrc = productCard.querySelector('img').src;
      const storeName = productCard.closest('.producto').dataset.store || "Tienda Desconocida";
      const storeWhatsapp = productCard.closest('.producto').dataset.store || "573159143399";

      // Verifica si ya existe el mismo producto con mismo id
      const item = cart.find(p => p.id === id);
      if (item) {
        item.quantity += 1;
      } else {
        cart.push({ id, title, price, quantity: 1, imgSrc, storeName, storeWhatsapp });
      }

      updateCart();

      // Animación carrito sobre el botón
      if (!this.classList.contains('cart-animating')) {
        this.classList.add('cart-animating');
        const icon = document.createElement('span');
        icon.className = 'cart-fly-anim';
        icon.innerHTML = '<i class="fas fa-shopping-cart"></i>';
        this.appendChild(icon);
        setTimeout(() => {
          icon.remove();
          this.classList.remove('cart-animating');
        }, 900);
      }
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

  let html = '<ul class="cart-list-group">';
  let total = 0;
  cart.forEach((item, index) => {
    total += item.price * item.quantity;
    const encodedTitle = encodeURIComponent(item.title);
    const whatsappLink = `https://wa.me/${item.storeWhatsapp}?text=Hola,%20quiero%20comprar%20este%20producto%20"${encodedTitle}"%20por%20$${item.price.toLocaleString()}.%20Producto:%20${item.imgSrc}`;
    html += `
      <li class="cart-list-group-item">
        <img src="${item.imgSrc}" alt="${item.title}" class="cart-list-img">
        <div class="cart-list-info">
          <div class="cart-product-title">${item.title}</div>
          <div class="cart-product-price">$${item.price.toLocaleString()} <span class="cart-badge">x${item.quantity}</span></div>
        </div>
        <div class="cart-list-actions">
          <button class="cart-remove-btn" onclick="removeItem(${index})"><i class="fas fa-trash"></i></button>
          <a href="${whatsappLink}" target="_blank" class="btn btn-success">WhatsApp</a>
          <a href="/reclamos/reclamo/${item.id}" class="btn btn-warning">Reclamo</a>
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
  const API_BASE = (window.API_BASE && String(window.API_BASE).replace(/\/+$/, '')) || '';
  const productosUrl = API_BASE ? `${API_BASE}/productos` : '/productos';
  console.log('Cargando productos desde:', productosUrl);

  fetch(productosUrl)
    .then(response => response.json())
    .then(data => {
      console.log("Productos obtenidos del backend:", data);

      const container = document.getElementById("productContainer");
      container.innerHTML = "";

      data.forEach(producto => {
        // Estructura visual avanzada para la card con badge sobre la imagen
        const card = document.createElement("div");
        card.className = "producto";
        card.setAttribute("data-category", producto.tipoProducto.toLowerCase());

        card.innerHTML = `
          <div class="product-card" data-id="${producto.id}">
            <div style="position:relative;">
              <img src="${producto.imagen || './img/default.jpg'}" class="product-img" alt="${producto.nombre}">
              <span class="product-badge">${producto.tipoProducto}</span>
            </div>
            <div class="product-body">
              <div class="product-title">${producto.nombre}</div>
              <div class="product-info">${producto.descripcion}</div>
              <div class="product-price">$${producto.precio.toLocaleString()}</div>
              <button class="btn-adopt add-to-cart">Agregar al carrito</button>
              <a href="/reclamos/reclamo/${producto.id}" class="btn btn-sm btn-warning mt-2 w-100">Reclamo</a>
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
