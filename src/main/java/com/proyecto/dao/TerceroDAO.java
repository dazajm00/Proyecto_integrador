package com.proyecto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;

/**
 * DAO para la tabla `tercero`.
 * Es el supertipo compartido por EMPLEADO y PROVEEDOR.
 * Este DAO gestiona la fila base; los subtipos
 * (EmpleadoDAO, ProveedorDAO) insertan en sus propias tablas
 * usando el id_tercero generado aquí.
 *
 * Columnas: id_tercero, nombre, telefono, direccion, correo,
 *           tipo ENUM('empleado','proveedor'), activo
 */
public class TerceroDAO {

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Inserta la fila base en `tercero` y devuelve el id generado.
     * Debe llamarse ANTES de insertar en `empleado` o `proveedor`.
     * @return id_tercero generado, o -1 si falla
     */
    public int guardar(String nombre, String telefono,
                       String direccion, String correo, String tipo) {
        String sql = "INSERT INTO tercero (nombre, telefono, direccion, correo, tipo) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            ps.setString(4, correo);
            ps.setString(5, tipo.toLowerCase()); // 'empleado' | 'proveedor'
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error al guardar tercero: " + e.getMessage());
        }
        return -1;
    }

    // ── READ ────────────────────────────────────────────────────

    public ResultSet buscarPorId(int idTercero) throws SQLException {
        String sql = "SELECT * FROM tercero WHERE id_tercero = ?";
        Connection con = ConexionBD.conectar();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idTercero);
        return ps.executeQuery();
    }

    public List<Integer> listarIdsPorTipo(String tipo) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_tercero FROM tercero WHERE tipo = ? AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipo.toLowerCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("id_tercero"));

        } catch (SQLException e) {
            System.out.println("Error al listar terceros: " + e.getMessage());
        }
        return ids;
    }

    // ── UPDATE ──────────────────────────────────────────────────

    public boolean actualizar(int idTercero, String nombre, String telefono,
                               String direccion, String correo) {
        String sql = "UPDATE tercero SET nombre = ?, telefono = ?, "
                   + "direccion = ?, correo = ? WHERE id_tercero = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            ps.setString(4, correo);
            ps.setInt(5, idTercero);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar tercero: " + e.getMessage());
            return false;
        }
    }

    public boolean desactivar(int idTercero) {
        String sql = "UPDATE tercero SET activo = FALSE WHERE id_tercero = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTercero);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al desactivar tercero: " + e.getMessage());
            return false;
        }
    }
}