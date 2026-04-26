package com.proyecto.modelo;

import java.time.LocalDate;

public abstract class KardexMovimiento {

    protected LocalDate    fecha;
    protected double       cantidad;
    protected String       responsable;
    protected int          idInsumo;
    protected MateriaPrima insumo;

    public KardexMovimiento(double cantidad, String responsable, MateriaPrima insumo) {
        this.fecha       = LocalDate.now();
        this.cantidad    = cantidad;
        this.responsable = responsable;
        this.insumo      = insumo;
        this.idInsumo    = System.identityHashCode(insumo);
    }

    // Métodos abstractos — cada subclase los implementa distinto (polimorfismo)
    public abstract void    registrar();
    public abstract boolean validar();

    public String getResumen() {
        return String.format("[%s] Fecha: %s | Insumo: %s | Cantidad: %.2f | Responsable: %s",
            this.getClass().getSimpleName(), fecha,
            insumo.getNombre(), cantidad, responsable);
    }

    public LocalDate    getFecha()       { return fecha; }
    public double       getCantidad()    { return cantidad; }
    public String       getResponsable() { return responsable; }
    public MateriaPrima getInsumo()      { return insumo; }
}