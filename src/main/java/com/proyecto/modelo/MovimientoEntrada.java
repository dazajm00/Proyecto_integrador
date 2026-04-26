package com.proyecto.modelo;

import java.time.LocalDate;

public class MovimientoEntrada extends KardexMovimiento {

    private final double            precioUnitario;
    private final LocalDate         fechaVencimiento;
    private final ProductoIngresado proveedor;

    public MovimientoEntrada(double cantidad, String responsable, MateriaPrima insumo,
                               ProductoIngresado proveedor, double precioUnitario,
                             LocalDate fechaVencimiento) {
        super(cantidad, responsable, insumo);
        this.proveedor        = proveedor;
        this.precioUnitario   = precioUnitario;
        this.fechaVencimiento = fechaVencimiento;
    }

    @Override
    public boolean validar() {
        if (cantidad <= 0) {
            System.out.println("Error: cantidad debe ser mayor a 0.");
            return false;
        }
        if (precioUnitario <= 0) {
            System.out.println("Error: precio unitario debe ser mayor a 0.");
            return false;
        }
        if (proveedor == null) {
            System.out.println("Error: debe asociarse un proveedor.");
            return false;
        }
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now())) {
            System.out.println("Error: fecha de vencimiento ya expiró.");
            return false;
        }
        return true;
    }

    @Override
    public void registrar() {
        insumo.agregar(cantidad);
        insumo.setFechaVencimiento(fechaVencimiento);
        System.out.println("Entrada registrada: " + getResumen());
    }

    public double calcularValorTotal() {
        return cantidad * precioUnitario;
    }

    public ProductoIngresado getProveedor()        { return proveedor; }
    public double            getPrecioUnitario()   { return precioUnitario; }
    public LocalDate         getFechaVencimiento() { return fechaVencimiento; }
}