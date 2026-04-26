package com.proyecto.modelo;

import java.util.Arrays;
import java.util.List;

public class Administracion extends Usuario {

    private static final List<String> PERMISOS = Arrays.asList(
        "REGISTRAR_ENTRADA",
        "REGISTRAR_SALIDA",
        "REGISTRAR_AJUSTE",
        "CONSULTAR_INVENTARIO",
        "GESTIONAR_USUARIO",
        "GESTIONAR_PROVEEDOR",
        "GENERAR_REPORTE",
        "VALORAR_PRODUCTO"
    );

    public Administracion(String nombre, String contrasena) {
        super(nombre, contrasena, "ADMINISTRADOR");
    }

    @Override
    public boolean tienePermiso(String accion) {
        return PERMISOS.contains(accion.toUpperCase());
    }

    public void gestionarUsuario(Usuario u) {
        System.out.println("Gestionando usuario: " + u.getNombre());
    }

    public void gestionarProveedor(ProductoIngresado p) {
        System.out.println("Gestionando proveedor: " + p.getNombre());
    }

    public void registrarMovimiento(KardexMovimiento k) {
        if (k.validar()) {
            k.registrar();
        } else {
            throw new IllegalArgumentException("Movimiento inválido: " + k.getResumen());
        }
    }

    public List<MateriaPrima> consultarInventario(List<MateriaPrima> inventario) {
        return inventario;
    }

    public Reporte generarReportes(String tipo, List<MateriaPrima> inventario,
                                   List<ValoracionProducto> valoraciones) {
        Reporte reporte = new Reporte(tipo);
        switch (tipo.toUpperCase()) {
            case "INVENTARIO" -> {
                reporte.generarReporteInventario(inventario);
            }
            case "COSTOS" -> {
                reporte.generarReporteCostos(valoraciones);
            }
            default -> throw new IllegalArgumentException("Tipo de reporte desconocido: " + tipo);
        }
        return reporte;
    }
}