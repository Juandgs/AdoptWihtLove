package com.app.adoptwithlove.Dto;

public class AnimalDashboardDTO {
    private Long id;
    private String nombre;
    private Integer edad;
    private String raza;
    private String tipoAnimal;
    private String imagen;
    private Long estadoId;
    private String nombreEstado;
    private Long fundacionId;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getTipoAnimal() { return tipoAnimal; }
    public void setTipoAnimal(String tipoAnimal) { this.tipoAnimal = tipoAnimal; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Long getEstadoId() { return estadoId; }
    public void setEstadoId(Long estadoId) { this.estadoId = estadoId; }
    public String getNombreEstado() { return nombreEstado; }
    public void setNombreEstado(String nombreEstado) { this.nombreEstado = nombreEstado; }
    public Long getFundacionId() { return fundacionId; }
    public void setFundacionId(Long fundacionId) { this.fundacionId = fundacionId; }
}
