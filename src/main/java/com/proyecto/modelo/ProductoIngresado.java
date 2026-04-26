package com.proyecto.modelo;

public class ProductoIngresado {

    private String nombre;
    private String telefono;
    private String correo;

    public ProductoIngresado(String nombre, String telefono, String correo) {
        this.nombre   = nombre;
        this.telefono = telefono;
        this.correo   = correo;
    }

    public MovimientoEntrada registrarCompra(MateriaPrima insumo, double cantidad,
                                             double precio, java.time.LocalDate fechaVenc,
                                             String responsable) {
        if (insumo == null)   throw new IllegalArgumentException("Insumo no puede ser nulo.");
        if (cantidad <= 0)    throw new IllegalArgumentException("Cantidad debe ser positiva.");
        if (precio <= 0)      throw new IllegalArgumentException("Precio debe ser positivo.");

        return new MovimientoEntrada(cantidad, responsable, insumo, this, precio, fechaVenc);
    }

    public String getNombre()   { return nombre; }
    public String getTelefono() { return telefono; }
    public String getContacto() { return correo; }
    public String getCorreo()   { return correo; }

    public void setNombre(String nombre)     { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setCorreo(String correo)     { this.correo = correo; }

    @Override
    public String toString() {
        return String.format("Proveedor: %s | Tel: %s | Correo: %s", nombre, telefono, correo);
    }
}

