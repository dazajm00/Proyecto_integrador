package com.proyecto.modelo;

public class ValoracionProducto {

    private String nombreProducto;
    private int    cantidadHecha;
    private double precioVenta;
    private double costosInsumos;

    public ValoracionProducto(String nombreProducto, int cantidadHecha,
                              double precioVenta, double costosInsumos) {
        this.nombreProducto = nombreProducto;
        this.cantidadHecha  = cantidadHecha;
        this.precioVenta    = precioVenta;
        this.costosInsumos  = costosInsumos;
    }

    public double calcularCostoProduccion() {
        return costosInsumos;
    }

    public double calcularCostoUnitario() {
        if (cantidadHecha == 0) throw new ArithmeticException("No se produjeron unidades.");
        return costosInsumos / cantidadHecha;
    }

    public double calcularGanancia() {
        return (precioVenta * cantidadHecha) - costosInsumos;
    }

    public double calcularMargen() {
        double ingresos = precioVenta * cantidadHecha;
        if (ingresos == 0) throw new ArithmeticException("Ingresos son 0, no se puede calcular margen.");
        return (calcularGanancia() / ingresos) * 100;
    }

    public String getNombreProducto()  { return nombreProducto; }
    public int    getCantidadHecha()   { return cantidadHecha; }
    public double getPrecioVenta()     { return precioVenta; }
    public double getCostosInsumos()   { return costosInsumos; }

    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCantidadHecha(int cantidadHecha)       { this.cantidadHecha = cantidadHecha; }
    public void setPrecioVenta(double precioVenta)         { this.precioVenta = precioVenta; }
    public void setCostosInsumos(double costosInsumos)     { this.costosInsumos = costosInsumos; }

    @Override
    public String toString() {
        return String.format("Producto: %s | Unidades: %d | Costo Unit: %.2f | Margen: %.1f%%",
            nombreProducto, cantidadHecha, calcularCostoUnitario(), calcularMargen());
    }
}