package com.proyecto.modelo;

import java.time.LocalDate;

public class MateriaPrima {

    private String    nombre;
    private double    cantidadDisponible;
    private LocalDate fechaVencimiento;
    private String    unidadMedida;

    public MateriaPrima(String nombre, double cantidadDisponible,
                        LocalDate fechaVencimiento, String unidadMedida) {
        this.nombre             = nombre;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaVencimiento   = fechaVencimiento;
        this.unidadMedida       = unidadMedida;
    }

    // Método protegido: solo subclases o clases del paquete pueden llamarlo directamente
    protected void actualizarCantidades(double delta) {
        double resultado = this.cantidadDisponible + delta;
        if (resultado < 0) {
            throw new IllegalStateException(
                "Stock insuficiente para '" + nombre + "'. Disponible: "
                + cantidadDisponible + ", solicitado: " + Math.abs(delta));
        }
        this.cantidadDisponible = resultado;
    }

    public boolean verificarVencimiento() {
        return fechaVencimiento != null && LocalDate.now().isAfter(fechaVencimiento);
    }

    public boolean esBajoStock(double stockMinimo) {
        return cantidadDisponible <= stockMinimo;
    }

    public void agregar(double cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva.");
        actualizarCantidades(cantidad);
    }

    public void consumir(double cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva.");
        actualizarCantidades(-cantidad);
    }

    public double    getCantidadDisponible() { return cantidadDisponible; }
    public String    getNombre()             { return nombre; }
    public LocalDate getFechaVencimiento()   { return fechaVencimiento; }
    public String    getUnidadMedida()       { return unidadMedida; }

    public void setNombre(String nombre)             { this.nombre = nombre; }
    public void setFechaVencimiento(LocalDate fecha) { this.fechaVencimiento = fecha; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    @Override
    public String toString() {
        return String.format("%s | %.2f %s | Vence: %s",
            nombre, cantidadDisponible, unidadMedida, fechaVencimiento);
    }
}
