package com.proyecto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;
import com.proyecto.modelo.ProductoIngresado;

/**
 * DAO para las tablas `tercero` + `proveedor` (subtipo 1:1).
 *
 * Columnas de proveedor: id_proveedor (FK→tercero), nit, contacto
 * Datos de nombre/telefono/correo vienen de la tabla `tercero`.
 *
 * Mapeo al modelo Java: ProductoIngresado
 */
public class ProveedorDAO {

    private final TerceroDAO terceroDAO = new TerceroDAO();

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Inserta en `tercero` y luego en `proveedor` en la misma transacción.
     * @return id_proveedor generado o -1 si falla
     */
    public int guardar(ProductoIngresado p, String nit,
                       String contacto, String direccion) {
        Connection con = null;
        try {
            con = ConexionBD.conectar();
            con.setAutoCommit(false);

            // 1) Fila base en tercero
            String sqlTercero = "INSERT INTO tercero (nombre, telefono, direccion, correo, tipo) "
                              + "VALUES (?, ?, ?, ?, 'proveedor')";
            int idTercero;
            try (PreparedStatement ps = con.prepareStatement(
                         sqlTercero, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getTelefono());
                ps.setString(3, direccion);
                ps.setString(4, p.getCorreo());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No se generó id para tercero.");
                idTercero = keys.getInt(1);
            }

            // 2) Subtipo proveedor
            String sqlProv = "INSERT INTO proveedor (id_proveedor, nit, contacto) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlProv)) {
                ps.setInt(1,    idTercero);
                ps.setString(2, nit);
                ps.setString(3, contacto);
                ps.executeUpdate();
            }

            con.commit();
            return idTercero;

        } catch (SQLException e) {
            System.out.println("Error al guardar proveedor: " + e.getMessage());
            if (con != null) try { con.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
        }
    }

    // ── READ ────────────────────────────────────────────────────

    public ProductoIngresado buscarPorId(int idProveedor) {
        String sql = "SELECT t.nombre, t.telefono, t.correo "
                   + "FROM proveedor p JOIN tercero t ON p.id_proveedor = t.id_tercero "
                   + "WHERE p.id_proveedor = ? AND t.activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construir(rs);

        } catch (SQLException e) {
            System.out.println("Error al buscar proveedor: " + e.getMessage());
        }
        return null;
    }

    public ProductoIngresado buscarPorNombre(String nombre) {
        String sql = "SELECT t.nombre, t.telefono, t.correo "
                   + "FROM proveedor p JOIN tercero t ON p.id_proveedor = t.id_tercero "
                   + "WHERE t.nombre = ? AND t.activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construir(rs);

        } catch (SQLException e) {
            System.out.println("Error al buscar proveedor por nombre: " + e.getMessage());
        }
        return null;
    }

    public int obtenerIdPorNombre(String nombre) {
        String sql = "SELECT p.id_proveedor FROM proveedor p "
                   + "JOIN tercero t ON p.id_proveedor = t.id_tercero "
                   + "WHERE t.nombre = ? AND t.activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_proveedor");

        } catch (SQLException e) {
            System.out.println("Error al obtener id de proveedor: " + e.getMessage());
        }
        return -1;
    }

    public List<ProductoIngresado> listarTodos() {
        List<ProductoIngresado> lista = new ArrayList<>();
        String sql = "SELECT t.nombre, t.telefono, t.correo "
                   + "FROM proveedor p JOIN tercero t ON p.id_proveedor = t.id_tercero "
                   + "WHERE t.activo = TRUE ORDER BY t.nombre";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar proveedores: " + e.getMessage());
        }
        return lista;
    }

    // ── UPDATE ──────────────────────────────────────────────────

    public boolean actualizar(ProductoIngresado p) {
        // Actualiza los datos de tercero (nombre, teléfono, correo)
        String sql = "UPDATE tercero t "
                   + "JOIN proveedor pr ON t.id_tercero = pr.id_proveedor "
                   + "SET t.nombre = ?, t.telefono = ?, t.correo = ? "
                   + "WHERE t.nombre = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTelefono());
            ps.setString(3, p.getCorreo());
            ps.setString(4, p.getNombre());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar proveedor: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE (lógico) ─────────────────────────────────────────

    public boolean desactivar(String nombre) {
        int id = obtenerIdPorNombre(nombre);
        if (id == -1) return false;
        return terceroDAO.desactivar(id);
    }

    // ── HELPER ──────────────────────────────────────────────────

    private ProductoIngresado construir(ResultSet rs) throws SQLException {
        return new ProductoIngresado(
            rs.getString("nombre"),
            rs.getString("telefono"),
            rs.getString("correo")
        );
    }
}
