package com.proyecto.modelo;

public abstract  class Usuario {

    public String nombre;
    private String contrasena;
    private String rol;

    public Usuario(String nombre, String contrasena, String rol) {
        this.nombre     = nombre;
        this.contrasena = contrasena;
        this.rol        = rol;
    }

    public boolean iniciarSesion(String contrasena) {
        return this.contrasena.equals(contrasena);
    }

    public void cerrarSesion() {
        System.out.println("Sesión cerrada para: " + nombre);
    }

    // Cada subclase define sus propios permisos
    public abstract boolean tienePermiso(String accion);

    public String getNombre()    { return nombre; }
    public String getRol()       { return rol; }
    public void setNombre(String nombre)         { this.nombre = nombre; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public void setRol(String rol)               { this.rol = rol; }
}