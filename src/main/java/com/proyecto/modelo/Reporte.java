package com.proyecto.modelo;

import java.time.LocalDate;
import java.util.List;

public class Reporte {

    private final String    tipoReporte;
    private final LocalDate fechaGenerado;
    private String    contenido;

    public Reporte(String tipoReporte) {
        this.tipoReporte   = tipoReporte;
        this.fechaGenerado = LocalDate.now();
        this.contenido     = "";
    }

    public void generarReporteInventario(List<MateriaPrima> inventario) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE INVENTARIO === ").append(fechaGenerado).append("\n");
        sb.append(String.format("%-25s %-12s %-10s %-12s%n",
            "Nombre", "Cantidad", "Unidad", "Vencimiento"));
        sb.append("-".repeat(62)).append("\n");
        for (MateriaPrima mp : inventario) {
            sb.append(String.format("%-25s %-12.2f %-10s %-12s%n",
                mp.getNombre(), mp.getCantidadDisponible(),
                mp.getUnidadMedida(), mp.getFechaVencimiento()));
            if (mp.verificarVencimiento()) {
                sb.append("  *** VENCIDO ***\n");
            }
        }
        this.contenido = sb.toString();
        System.out.println(contenido);
    }

    public void generarReporteCostos(List<ValoracionProducto> valoraciones) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE COSTOS === ").append(fechaGenerado).append("\n");
        sb.append(String.format("%-20s %-10s %-12s %-12s %-10s%n",
            "Producto", "Unidades", "Costo Unit.", "Ganancia", "Margen %"));
        sb.append("-".repeat(68)).append("\n");
        for (ValoracionProducto vp : valoraciones) {
            sb.append(String.format("%-20s %-10d %-12.2f %-12.2f %-10.1f%n",
                vp.getNombreProducto(), vp.getCantidadHecha(),
                vp.calcularCostoUnitario(), vp.calcularGanancia(),
                vp.calcularMargen()));
        }
        this.contenido = sb.toString();
        System.out.println(contenido);
    }

    public String exportar() {
        return contenido;
    }

    public String    getTipoReporte()   { return tipoReporte; }
    public LocalDate getFechaGenerado() { return fechaGenerado; }
    public String    getContenido()     { return contenido; }
}