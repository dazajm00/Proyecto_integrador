package com.proyecto.modelo;

public class MovimientoAjuste extends KardexMovimiento {

    private final String motivo;
    private final String tipoAjuste; // "PERDIDA", "DANIO", "CORRECCION"

    public MovimientoAjuste(double cantidad, String responsable,
                            MateriaPrima insumo, String motivo, String tipoAjuste) {
        super(cantidad, responsable, insumo);
        this.motivo     = motivo;
        this.tipoAjuste = tipoAjuste;
    }

    @Override
    public boolean validar() {
        if (motivo == null || motivo.trim().isEmpty()) {
            System.out.println("Error: el ajuste debe tener un motivo.");
            return false;
        }
        if (tipoAjuste == null || tipoAjuste.trim().isEmpty()) {
            System.out.println("Error: debe especificarse el tipo de ajuste.");
            return false;
        }
        // Ajuste negativo: verificar que haya stock suficiente
        if (cantidad < 0 && insumo.getCantidadDisponible() < Math.abs(cantidad)) {
            System.out.println("Error: stock insuficiente para el ajuste.");
            return false;
        }
        return true;
    }

    @Override
    public void registrar() {
        // cantidad puede ser positiva (corrección al alza) o negativa (pérdida/daño)
        insumo.actualizarCantidades(cantidad);
        System.out.println("Ajuste registrado: " + getResumen()
            + " | Motivo: " + motivo + " | Tipo: " + tipoAjuste);
    }

    public boolean esPerdida()    { return "PERDIDA".equalsIgnoreCase(tipoAjuste); }
    public boolean esDanio()      { return "DANIO".equalsIgnoreCase(tipoAjuste); }
    public String  getMotivo()    { return motivo; }
    public String  getTipoAjuste(){ return tipoAjuste; }
}

