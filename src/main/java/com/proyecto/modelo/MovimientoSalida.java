package com.proyecto.modelo;

public class MovimientoSalida extends KardexMovimiento {

    private final int    idLote;
    private final double stockMinimoReferencia;

    public MovimientoSalida(double cantidad, String responsable,
                            MateriaPrima insumo, int idLote,
                            double stockMinimoReferencia) {
        super(cantidad, responsable, insumo);
        this.idLote                = idLote;
        this.stockMinimoReferencia = stockMinimoReferencia;
    }

    @Override
    public boolean validar() {
        if (cantidad <= 0) {
            System.out.println("Error: cantidad debe ser mayor a 0.");
            return false;
        }
        if (insumo.getCantidadDisponible() < cantidad) {
            System.out.println("Error: stock insuficiente. Disponible: "
                + insumo.getCantidadDisponible() + ", requerido: " + cantidad);
            return false;
        }
        if (insumo.verificarVencimiento()) {
            System.out.println("Error: insumo '" + insumo.getNombre() + "' está vencido.");
            return false;
        }
        return true;
    }

    @Override
    public void registrar() {
        insumo.consumir(cantidad);
        if (insumo.esBajoStock(stockMinimoReferencia)) {
            System.out.println("ALERTA: Stock bajo para '" + insumo.getNombre()
                + "'. Disponible: " + insumo.getCantidadDisponible());
        }
        System.out.println("Salida registrada: " + getResumen());
    }

    public int getIdLote() { return idLote; }
}
