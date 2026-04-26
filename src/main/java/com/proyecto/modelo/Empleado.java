package com.proyecto.modelo;

import java.util.Arrays;
import java.util.List;

public class Empleado extends Usuario {

    private static final List<String> PERMISOS = Arrays.asList(
        "REGISTRAR_ENTRADA",
        "REGISTRAR_SALIDA",
        "CONSULTAR_INVENTARIO"
    );

    public Empleado(String nombre, String contrasena) {
        super(nombre, contrasena, "EMPLEADO");
    }

    @Override
    public boolean tienePermiso(String accion) {
        return PERMISOS.contains(accion.toUpperCase());
    }

    public void registrarMovimiento(KardexMovimiento k) {
        if (k.validar()) {
            k.registrar();
        } else {
            throw new IllegalArgumentException("Movimiento inválido: " + k.getResumen());
        }
    }

    public void registrarEntrada(KardexMovimiento k) {
        if (k.getClass().getSimpleName().equals("MovimientoEntrada")) {
            registrarMovimiento(k);
        } else {
            throw new IllegalArgumentException("Se esperaba un MovimientoEntrada.");
        }
    }

    public void registrarSalida(KardexMovimiento k) {
        if (k.getClass().getSimpleName().equals("MovimientoSalida")) {
            registrarMovimiento(k);
        } else {
            throw new IllegalArgumentException("Se esperaba un MovimientoSalida.");
        }
    }

    public List<MateriaPrima> consultarInventario(List<MateriaPrima> inventario) {
        if (!tienePermiso("CONSULTAR_INVENTARIO")) {
            throw new SecurityException("Sin permiso para consultar inventario.");
        }
        return inventario;
    }
}