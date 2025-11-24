package com.app.adoptwithlove.Dto;

public class ProductoDashboardDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private String cantidad;
    private String tipoProducto;
    private String descripcion;
    private String imagen;
    private Long personaId;
    private Long estadoId;
    private String nombreEstado;
    private Integer cantidadReclamos;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getCantidad() { return cantidad; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }
    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Long getPersonaId() { return personaId; }
    public void setPersonaId(Long personaId) { this.personaId = personaId; }
    public Long getEstadoId() { return estadoId; }
    public void setEstadoId(Long estadoId) { this.estadoId = estadoId; }
    public String getNombreEstado() { return nombreEstado; }
    public void setNombreEstado(String nombreEstado) { this.nombreEstado = nombreEstado; }
    public Integer getCantidadReclamos() { return cantidadReclamos; }
    public void setCantidadReclamos(Integer cantidadReclamos) { this.cantidadReclamos = cantidadReclamos; }
}
