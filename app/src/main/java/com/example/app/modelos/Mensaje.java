package com.example.app.modelos;

public class Mensaje {
    private String texto;
    private boolean esMio; // true si lo envía el usuario, false si es Gemini

    public Mensaje(String texto, boolean esMio) {
        this.texto = texto;
        this.esMio = esMio;
    }

    public String getTexto() { return texto; }
    public boolean isEsMio() { return esMio; }
}