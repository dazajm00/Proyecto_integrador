package com.proyecto.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;
import com.proyecto.modelo.KardexMovimiento;
import com.proyecto.modelo.MateriaPrima;
import com.proyecto.modelo.MovimientoAjuste;
import com.proyecto.modelo.MovimientoEntrada;
import com.proyecto.modelo.MovimientoSalida;
import com.proyecto.modelo.ProductoIngresado;

/**
 * DAO para la tabla `movimiento`.
 * Mapea al modelo Java: KardexMovimiento y sus subtipos
 * (MovimientoEntrada, MovimientoSalida, MovimientoAjuste).
 *
 * Columnas: id_movimiento, id_insumo, id_tercero, id_documento,
 *   id_lote (nullable), tipo ENUM(...), fecha, cantidad,
 *   fecha_vencimiento (nullable), observacion
 *
 * ENUM tipos:
 *   'entrada_compra'            → MovimientoEntrada
 *   'salida_produccion'         → MovimientoSalida
 *   'ajuste_perdida'            → MovimientoAjuste (PERDIDA)
 *   'ajuste_hallazgo'           → MovimientoAjuste (CORRECCION)
 *   'devolucion_proveedor'      → MovimientoSalida
 *   'entrada_sobrante_produccion' → MovimientoEntrada
 *   'inventario_inicial'        → MovimientoEntrada
 */
public class MovimientoDAO {

    private final InsumoDAO    insumoDAO    = new InsumoDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final DocumentoDAO documentoDAO = new DocumentoDAO();
    private final EmpleadoDAO  empleadoDAO  = new EmpleadoDAO();

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Persiste un MovimientoEntrada (entrada_compra).
     * Crea automáticamente el documento cabecera.
     */
    public boolean guardarEntrada(MovimientoEntrada me, String numeroFactura) {
        int idInsumo    = insumoDAO.obtenerIdPorNombre(me.getInsumo().getNombre());
        int idProveedor = proveedorDAO.obtenerIdPorNombre(me.getProveedor().getNombre());
        int idTipoDoc   = documentoDAO.idTipoOrdenCompra();

        if (idInsumo == -1 || idProveedor == -1 || idTipoDoc == -1) {
            System.out.println("Error: insumo, proveedor o tipo_doc no encontrado.");
            return false;
        }

        int idDocumento = documentoDAO.crearDocumento(
            idTipoDoc, idProveedor, numeroFactura,
            me.getFecha(), "Entrada de compra: " + me.getInsumo().getNombre()
        );
        if (idDocumento == -1) return false;

        return insertarMovimiento(
            idInsumo, idProveedor, idDocumento, null,
            "entrada_compra",
            me.getFecha(),
            me.getCantidad(),
            me.getFechaVencimiento(),
            null
        );
    }

    /**
     * Persiste un MovimientoSalida (salida_produccion).
     * Requiere el id del lote de producción asociado.
     */
    public boolean guardarSalida(MovimientoSalida ms, String usuarioResponsable, int idLote) {
        int idInsumo   = insumoDAO.obtenerIdPorNombre(ms.getInsumo().getNombre());
        int idEmpleado = empleadoDAO.obtenerIdPorUsuario(usuarioResponsable);
        int idTipoDoc  = documentoDAO.idTipoOrdenProduccion();

        if (idInsumo == -1 || idEmpleado == -1 || idTipoDoc == -1) {
            System.out.println("Error: insumo, empleado o tipo_doc no encontrado.");
            return false;
        }

        int idDocumento = documentoDAO.crearDocumento(
            idTipoDoc, idEmpleado, null,
            ms.getFecha(), "Salida producción: " + ms.getInsumo().getNombre()
        );
        if (idDocumento == -1) return false;

        return insertarMovimiento(
            idInsumo, idEmpleado, idDocumento, idLote,
            "salida_produccion",
            ms.getFecha(),
            ms.getCantidad(),
            null,
            null
        );
    }

    /**
     * Persiste un MovimientoAjuste.
     * El tipo ENUM se determina por tipoAjuste del modelo.
     */
    public boolean guardarAjuste(MovimientoAjuste ma, String usuarioResponsable) {
        int idInsumo   = insumoDAO.obtenerIdPorNombre(ma.getInsumo().getNombre());
        int idEmpleado = empleadoDAO.obtenerIdPorUsuario(usuarioResponsable);
        int idTipoDoc  = documentoDAO.idTipoAjuste();

        if (idInsumo == -1 || idEmpleado == -1 || idTipoDoc == -1) {
            System.out.println("Error: insumo, empleado o tipo_doc no encontrado.");
            return false;
        }

        // Mapear tipoAjuste del modelo al ENUM de la BD
        String tipoEnum = switch (ma.getTipoAjuste().toUpperCase()) {
            case "PERDIDA" -> "ajuste_perdida";
            case "DANIO"   -> "ajuste_perdida";     // pérdida por daño
            case "CORRECCION" -> "ajuste_hallazgo";
            default -> "ajuste_perdida";
        };

        int idDocumento = documentoDAO.crearDocumento(
            idTipoDoc, idEmpleado, null,
            ma.getFecha(), ma.getMotivo()
        );
        if (idDocumento == -1) return false;

        return insertarMovimiento(
            idInsumo, idEmpleado, idDocumento, null,
            tipoEnum,
            ma.getFecha(),
            Math.abs(ma.getCantidad()),   // siempre positivo en BD
            null,
            ma.getMotivo()
        );
    }

    // ── READ ────────────────────────────────────────────────────

    public List<KardexMovimiento> listarTodos() {
        List<KardexMovimiento> lista = new ArrayList<>();
        String sql = buildSelectBase() + "ORDER BY m.fecha DESC";
        try (Connection con = ConexionBD.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                KardexMovimiento km = construir(rs);
                if (km != null) lista.add(km);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar movimientos: " + e.getMessage());
        }
        return lista;
    }

    public List<KardexMovimiento> listarPorInsumo(String nombreInsumo) {
        List<KardexMovimiento> lista = new ArrayList<>();
        String sql = buildSelectBase()
                   + "WHERE i.nombre = ? ORDER BY m.fecha DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreInsumo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                KardexMovimiento km = construir(rs);
                if (km != null) lista.add(km);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar movimientos por insumo: " + e.getMessage());
        }
        return lista;
    }

    public List<KardexMovimiento> listarPorTipo(String tipoEnum) {
        List<KardexMovimiento> lista = new ArrayList<>();
        String sql = buildSelectBase()
                   + "WHERE m.tipo = ? ORDER BY m.fecha DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipoEnum);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                KardexMovimiento km = construir(rs);
                if (km != null) lista.add(km);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar movimientos por tipo: " + e.getMessage());
        }
        return lista;
    }

    // ── HELPERS INTERNOS ────────────────────────────────────────

    private boolean insertarMovimiento(int idInsumo, int idTercero, int idDocumento,
                                        Integer idLote, String tipo,
                                        LocalDate fecha, double cantidad,
                                        LocalDate fechaVencimiento, String observacion) {
        String sql = "INSERT INTO movimiento "
                   + "(id_insumo, id_tercero, id_documento, id_lote, tipo, "
                   + " fecha, cantidad, fecha_vencimiento, observacion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1,    idInsumo);
            ps.setInt(2,    idTercero);
            ps.setInt(3,    idDocumento);
            if (idLote != null) ps.setInt(4, idLote); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, tipo);
            ps.setDate(6,   Date.valueOf(fecha));
            ps.setDouble(7, cantidad);
            if (fechaVencimiento != null) ps.setDate(8, Date.valueOf(fechaVencimiento));
            else ps.setNull(8, Types.DATE);
            ps.setString(9, observacion);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al insertar movimiento: " + e.getMessage());
            return false;
        }
    }

    private String buildSelectBase() {
        return "SELECT m.tipo, m.fecha, m.cantidad, m.observacion, "
             + "       m.fecha_vencimiento, m.id_lote, "
             + "       i.nombre AS nombre_insumo, i.cantidad_disponible, "
             + "       i.unidad_medida, "
             + "       t.nombre AS nombre_tercero, t.tipo AS tipo_tercero, "
             + "       t.telefono, t.correo "
             + "FROM movimiento m "
             + "JOIN insumo i  ON m.id_insumo  = i.id_insumo "
             + "JOIN tercero t ON m.id_tercero  = t.id_tercero ";
    }

    private KardexMovimiento construir(ResultSet rs) throws SQLException {
        MateriaPrima insumo = new MateriaPrima(
            rs.getString("nombre_insumo"),
            rs.getDouble("cantidad_disponible"),
            null,
            rs.getString("unidad_medida")
        );

        String nombreTercero = rs.getString("nombre_tercero");
        String tipoTercero   = rs.getString("tipo_tercero");
        String telefono      = rs.getString("telefono");
        String correo        = rs.getString("correo");
        String tipo          = rs.getString("tipo");
        double cantidad      = rs.getDouble("cantidad");
        String observacion   = rs.getString("observacion");

        Date fechaVencSql    = rs.getDate("fecha_vencimiento");
        LocalDate fechaVenc  = (fechaVencSql != null) ? fechaVencSql.toLocalDate() : null;

        return switch (tipo) {
            case "entrada_compra",
                 "entrada_sobrante_produccion",
                 "inventario_inicial" -> {
                ProductoIngresado prov = new ProductoIngresado(nombreTercero, telefono, correo);
                yield new MovimientoEntrada(cantidad, nombreTercero, insumo, prov, 0, fechaVenc);
            }
            case "salida_produccion",
                 "devolucion_proveedor" -> {
                int idLote = rs.getInt("id_lote");
                yield new MovimientoSalida(cantidad, nombreTercero, insumo, idLote, 0);
            }
            case "ajuste_perdida"   ->
                new MovimientoAjuste(-cantidad, nombreTercero, insumo, observacion, "PERDIDA");
            case "ajuste_hallazgo" ->
                new MovimientoAjuste(cantidad, nombreTercero, insumo, observacion, "CORRECCION");
            default -> {
                System.out.println("Tipo de movimiento desconocido: " + tipo);
                yield null;
            }
        };
    }
}