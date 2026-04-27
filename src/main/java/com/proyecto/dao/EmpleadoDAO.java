package com.proyecto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;
import com.proyecto.modelo.Administracion;
import com.proyecto.modelo.Empleado;
import com.proyecto.modelo.Usuario;

/**
 * DAO para las tablas `tercero` + `empleado` (subtipo 1:1).
 *
 * Columnas de empleado: id_empleado (FK→tercero), cargo,
 *   usuario, contrasena, rol ENUM('administrador','auxiliar_operativo')
 *
 * Mapeo al modelo Java:
 *   rol 'administrador'       → Administracion
 *   rol 'auxiliar_operativo'  → Empleado
 */
public class EmpleadoDAO {

    private final TerceroDAO terceroDAO = new TerceroDAO();

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Inserta en `tercero` y luego en `empleado` dentro de la misma transacción.
     * @return id generado o -1 si falla
     */
    public int guardar(String nombre, String telefono, String direccion,
                       String correo, String cargo, String usuario,
                       String contrasena, String rol) {

        Connection con = null;
        try {
            con = ConexionBD.conectar();
            con.setAutoCommit(false);

            // 1) Fila base en tercero
            String sqlTercero = "INSERT INTO tercero (nombre, telefono, direccion, correo, tipo) "
                              + "VALUES (?, ?, ?, ?, 'empleado')";
            int idTercero;
            try (PreparedStatement ps = con.prepareStatement(
                         sqlTercero, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, direccion);
                ps.setString(4, correo);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No se generó id para tercero.");
                idTercero = keys.getInt(1);
            }

            // 2) Subtipo empleado
            String sqlEmp = "INSERT INTO empleado (id_empleado, cargo, usuario, contrasena, rol) "
                          + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlEmp)) {
                ps.setInt(1,    idTercero);
                ps.setString(2, cargo);
                ps.setString(3, usuario);
                ps.setString(4, contrasena);
                ps.setString(5, rol.toLowerCase());
                ps.executeUpdate();
            }

            con.commit();
            return idTercero;

        } catch (SQLException e) {
            System.out.println("Error al guardar empleado: " + e.getMessage());
            if (con != null) try { con.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
        }
    }

    // ── READ ────────────────────────────────────────────────────

    /**
     * Busca por nombre de usuario (campo login) y devuelve el objeto
     * correcto del modelo Java según el rol.
     */
    public Usuario buscarPorUsuario(String usuario) {
        String sql = "SELECT t.nombre, e.usuario, e.contrasena, e.rol "
                   + "FROM empleado e JOIN tercero t ON e.id_empleado = t.id_tercero "
                   + "WHERE e.usuario = ? AND t.activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construir(rs);

        } catch (SQLException e) {
            System.out.println("Error al buscar empleado: " + e.getMessage());
        }
        return null;
    }

    /** Verifica credenciales directamente en BD. */
    public boolean autenticar(String usuario, String contrasena) {
        String sql = "SELECT COUNT(*) FROM empleado e "
                   + "JOIN tercero t ON e.id_empleado = t.id_tercero "
                   + "WHERE e.usuario = ? AND e.contrasena = ? AND t.activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasena); // En producción comparar hash bcrypt
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.out.println("Error al autenticar: " + e.getMessage());
        }
        return false;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT t.nombre, e.usuario, e.contrasena, e.rol "
                   + "FROM empleado e JOIN tercero t ON e.id_empleado = t.id_tercero "
                   + "WHERE t.activo = TRUE ORDER BY t.nombre";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar empleados: " + e.getMessage());
        }
        return lista;
    }

    public int obtenerIdPorUsuario(String usuario) {
        String sql = "SELECT id_empleado FROM empleado WHERE usuario = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_empleado");

        } catch (SQLException e) {
            System.out.println("Error al obtener id de empleado: " + e.getMessage());
        }
        return -1;
    }

    // ── UPDATE ──────────────────────────────────────────────────

    public boolean actualizarContrasena(String usuario, String nuevaContrasena) {
        String sql = "UPDATE empleado SET contrasena = ? WHERE usuario = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaContrasena);
            ps.setString(2, usuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE (lógico) ─────────────────────────────────────────

    public boolean desactivar(String usuario) {
        int id = obtenerIdPorUsuario(usuario);
        if (id == -1) return false;
        return terceroDAO.desactivar(id);
    }

    // ── HELPER ──────────────────────────────────────────────────

    private Usuario construir(ResultSet rs) throws SQLException {
        String nombre     = rs.getString("nombre");
        String contrasena = rs.getString("contrasena");
        String rol        = rs.getString("rol");

        return switch (rol.toLowerCase()) {
            case "administrador"      -> new Administracion(nombre, contrasena);
            case "auxiliar_operativo" -> new Empleado(nombre, contrasena);
            default -> throw new IllegalArgumentException("Rol desconocido: " + rol);
        };
    }
}