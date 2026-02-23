package com.example.app.modelos;

public class Evento {
    private int id;
    private String nombre;
    private String ubicacion;
    private double latitud;
    private double longitud;
    private String categoria;

    public Evento(int id, String nombre, String ubicacion, double latitud, double longitud, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.categoria = categoria;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getCategoria() { return categoria; }
}