package com.proyecto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;
import com.proyecto.modelo.ValoracionProducto;

/**
 * DAO para las tablas `lote_produccion` y `detalle_lote`.
 *
 * lote_produccion: id_lote, id_empleado, id_producto, fecha,
 *   cantidad_producida, costo_total, precio_por_lote
 *
 * detalle_lote: id_detalle, id_lote, id_insumo,
 *   cantidad_usada, costo_parcial
 *
 * Mapeo al modelo Java: ValoracionProducto
 *   (cubre el caso de uso de valoración de costos)
 */
public class LoteProduccionDAO {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Inserta un lote de producción y retorna el id generado.
     * Los detalles se insertan con {@link #guardarDetalle}.
     *
     * @param vp              valoración del producto
     * @param usuarioEmpleado usuario (login) del empleado responsable
     * @param idProducto      id del producto en la tabla `producto`
     * @return id_lote generado, o -1 si falla
     */
    public int guardarLote(ValoracionProducto vp,
                           String usuarioEmpleado, int idProducto) {
        int idEmpleado = empleadoDAO.obtenerIdPorUsuario(usuarioEmpleado);
        if (idEmpleado == -1) {
            System.out.println("Error: empleado no encontrado: " + usuarioEmpleado);
            return -1;
        }

        String sql = "INSERT INTO lote_produccion "
                   + "(id_empleado, id_producto, cantidad_producida, costo_total, precio_por_lote) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    idEmpleado);
            ps.setInt(2,    idProducto);
            ps.setDouble(3, vp.getCantidadHecha());
            ps.setDouble(4, vp.calcularCostoProduccion());
            ps.setDouble(5, vp.getPrecioVenta() * vp.getCantidadHecha());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error al guardar lote de producción: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Inserta un detalle (insumo consumido) para un lote.
     * Llamar una vez por cada insumo usado en el lote.
     */
    public boolean guardarDetalle(int idLote, int idInsumo,
                                   double cantidadUsada, double costoParcial) {
        String sql = "INSERT INTO detalle_lote "
                   + "(id_lote, id_insumo, cantidad_usada, costo_parcial) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1,    idLote);
            ps.setInt(2,    idInsumo);
            ps.setDouble(3, cantidadUsada);
            ps.setDouble(4, costoParcial);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al guardar detalle de lote: " + e.getMessage());
            return false;
        }
    }

    // ── READ ────────────────────────────────────────────────────

    /**
     * Recupera todos los lotes como ValoracionProducto para el reporte de costos.
     * Hace JOIN con `producto` para obtener nombre y precio_venta.
     */
    public List<ValoracionProducto> listarComoValoraciones() {
        List<ValoracionProducto> lista = new ArrayList<>();
        String sql = "SELECT p.nombre AS nombre_producto, "
                   + "       lp.cantidad_producida, "
                   + "       p.precio_venta, "
                   + "       lp.costo_total "
                   + "FROM lote_produccion lp "
                   + "JOIN producto p ON lp.id_producto = p.id_producto "
                   + "ORDER BY lp.fecha DESC";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new ValoracionProducto(
                    rs.getString("nombre_producto"),
                    (int) rs.getDouble("cantidad_producida"),
                    rs.getDouble("precio_venta"),
                    rs.getDouble("costo_total")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar valoraciones: " + e.getMessage());
        }
        return lista;
    }

    public ValoracionProducto buscarPorId(int idLote) {
        String sql = "SELECT p.nombre AS nombre_producto, lp.cantidad_producida, "
                   + "       p.precio_venta, lp.costo_total "
                   + "FROM lote_produccion lp "
                   + "JOIN producto p ON lp.id_producto = p.id_producto "
                   + "WHERE lp.id_lote = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLote);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ValoracionProducto(
                    rs.getString("nombre_producto"),
                    (int) rs.getDouble("cantidad_producida"),
                    rs.getDouble("precio_venta"),
                    rs.getDouble("costo_total")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar lote: " + e.getMessage());
        }
        return null;
    }

    /** Suma el costo total de todos los lotes de un producto. */
    public double calcularCostoTotalPorProducto(String nombreProducto) {
        String sql = "SELECT COALESCE(SUM(lp.costo_total), 0) AS total "
                   + "FROM lote_produccion lp "
                   + "JOIN producto p ON lp.id_producto = p.id_producto "
                   + "WHERE p.nombre = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreProducto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("total");

        } catch (SQLException e) {
            System.out.println("Error al calcular costo total: " + e.getMessage());
        }
        return 0;
    }

    // ── UPDATE ──────────────────────────────────────────────────

    public boolean actualizarCostoTotal(int idLote, double nuevoCosto) {
        String sql = "UPDATE lote_produccion SET costo_total = ? WHERE id_lote = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, nuevoCosto);
            ps.setInt(2,    idLote);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar costo de lote: " + e.getMessage());
            return false;
        }
    }
}