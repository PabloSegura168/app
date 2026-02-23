package com.example.app.modelos;

public class Incidencia {
    private int id;
    private String titulo;
    private String descripcion;
    private int urgencia;
    private String fecha;
    private int idEvento;
    private double latitud;
    private double longitud;
    private String urlFoto;

    public Incidencia() {}

    public Incidencia(String titulo, String descripcion, int urgencia, String fecha, int idEvento, double latitud, double longitud, String urlFoto) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.urgencia = urgencia;
        this.fecha = fecha;
        this.idEvento = idEvento;
        this.latitud = latitud;
        this.longitud = longitud;
        this.urlFoto = urlFoto;
    }
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String t) { this.titulo = t; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public int getUrgencia() { return urgencia; }
    public void setUrgencia(int u) { this.urgencia = u; }
    public String getFecha() { return fecha; }
    public void setFecha(String f) { this.fecha = f; }
    public int getIdEvento() { return idEvento; }
    public void setIdEvento(int i) { this.idEvento = i; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double l) { this.latitud = l; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double l) { this.longitud = l; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String u) { this.urlFoto = u; }
}