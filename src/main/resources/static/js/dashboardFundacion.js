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
