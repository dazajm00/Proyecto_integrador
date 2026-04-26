package com.proyecto.controlador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.modelo.Administracion;
import com.proyecto.modelo.KardexMovimiento;
import com.proyecto.modelo.MateriaPrima;
import com.proyecto.modelo.MovimientoAjuste;
import com.proyecto.modelo.MovimientoEntrada;
import com.proyecto.modelo.MovimientoSalida;
import com.proyecto.modelo.ProductoIngresado;
import com.proyecto.modelo.Reporte;
import com.proyecto.modelo.Usuario;
import com.proyecto.modelo.ValoracionProducto;

public class SistemaControlador {

    private final List<MateriaPrima>      inventario   = new ArrayList<>();
    private final List<ProductoIngresado> proveedores  = new ArrayList<>();
    private final List<KardexMovimiento>  movimientos  = new ArrayList<>();
    private final List<ValoracionProducto> valoraciones = new ArrayList<>();
    private final List<Usuario>           usuarios     = new ArrayList<>();
    private Usuario                 usuarioActual;

    //Autenticación

    public boolean login(String nombre, String contrasena) {
        for (Usuario u : usuarios) {
            if (u.getNombre().equals(nombre) && u.iniciarSesion(contrasena)) {
                this.usuarioActual = u;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        if (usuarioActual != null) {
            usuarioActual.cerrarSesion();
            usuarioActual = null;
        }
    }

    public Usuario getUsuarioActual() { return usuarioActual; }

    public void agregarUsuario(Usuario u) { usuarios.add(u); }

    // Materia Prima 

    public void agregarMateriaPrima(String nombre, double cantidad,
                                    LocalDate vencimiento, String unidad) {
        for (MateriaPrima mp : inventario) {
            if (mp.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe un insumo con ese nombre.");
            }
        }
        inventario.add(new MateriaPrima(nombre, cantidad, vencimiento, unidad));
    }

    public List<MateriaPrima> getInventario() { return inventario; }

    public List<MateriaPrima> getInsumosVencidos() {
        List<MateriaPrima> vencidos = new ArrayList<>();
        for (MateriaPrima mp : inventario) {
            if (mp.verificarVencimiento()) vencidos.add(mp);
        }
        return vencidos;
    }

    public List<MateriaPrima> getInsumosBajoStock(double stockMinimo) {
        List<MateriaPrima> bajos = new ArrayList<>();
        for (MateriaPrima mp : inventario) {
            if (mp.esBajoStock(stockMinimo)) bajos.add(mp);
        }
        return bajos;
    }

    // Proveedores

    public void agregarProveedor(String nombre, String telefono, String correo) {
        proveedores.add(new ProductoIngresado(nombre, telefono, correo));
    }

    public List<ProductoIngresado> getProveedores() { return proveedores; }

    //  Movimientos

    public void registrarEntrada(MateriaPrima insumo, ProductoIngresado proveedor,
                                 double cantidad, double precio,
                                 LocalDate fechaVenc, String responsable) {
        if (usuarioActual == null || !usuarioActual.tienePermiso("REGISTRAR_ENTRADA")) {
            throw new SecurityException("Sin permiso para registrar entradas.");
        }
        MovimientoEntrada me = new MovimientoEntrada(
            cantidad, responsable, insumo, proveedor, precio, fechaVenc);
        if (me.validar()) {
            me.registrar();
            movimientos.add(me);
        } else {
            throw new IllegalArgumentException("Movimiento de entrada inválido.");
        }
    }

    public void registrarSalida(MateriaPrima insumo, double cantidad,
                                int idLote, double stockMin, String responsable) {
        if (usuarioActual == null || !usuarioActual.tienePermiso("REGISTRAR_SALIDA")) {
            throw new SecurityException("Sin permiso para registrar salidas.");
        }
        MovimientoSalida ms = new MovimientoSalida(cantidad, responsable, insumo, idLote, stockMin);
        if (ms.validar()) {
            ms.registrar();
            movimientos.add(ms);
        } else {
            throw new IllegalArgumentException("Movimiento de salida inválido.");
        }
    }

    public void registrarAjuste(MateriaPrima insumo, double cantidad,
                                String motivo, String tipo, String responsable) {
        if (usuarioActual == null || !usuarioActual.tienePermiso("REGISTRAR_AJUSTE")) {
            throw new SecurityException("Sin permiso para registrar ajustes.");
        }
        MovimientoAjuste ma = new MovimientoAjuste(cantidad, responsable, insumo, motivo, tipo);
        if (ma.validar()) {
            ma.registrar();
            movimientos.add(ma);
        } else {
            throw new IllegalArgumentException("Ajuste inválido.");
        }
    }

    public List<KardexMovimiento> getMovimientos() { return movimientos; }

    // Valoración de productos

    public void agregarValoracion(String producto, int cantidad,
                                  double precioVenta, double costoInsumos) {
        valoraciones.add(new ValoracionProducto(producto, cantidad, precioVenta, costoInsumos));
    }

    public List<ValoracionProducto> getValoraciones() { return valoraciones; }

    // Reportes

    public Reporte generarReporte(String tipo) {
        if (usuarioActual == null || !usuarioActual.tienePermiso("GENERAR_REPORTE")) {
            throw new SecurityException("Sin permiso para generar reportes.");
        }
        if (!(usuarioActual instanceof Administracion)) {
            throw new SecurityException("Solo el administrador puede generar reportes.");
        }
        Administracion admin = (Administracion) usuarioActual;
        return admin.generarReportes(tipo, inventario, valoraciones);
    }
}