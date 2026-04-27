package com.proyecto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;
import com.proyecto.modelo.MateriaPrima;

/**
 * DAO para la tabla `insumo`.
 * Mapea al modelo Java: MateriaPrima
 *
 * Columnas: id_insumo, nombre, unidad_medida, cantidad_disponible,
 *           stock_minimo, precio_unitario,
 *           estado ENUM('disponible','agotado','vencido'), activo
 *
 * Nota: MateriaPrima no tiene campo precio_unitario ni stock_minimo
 * propios, pero la BD sí. Este DAO los gestiona directamente en SQL
 * y expone métodos auxiliares para esos campos.
 */
public class InsumoDAO {

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Inserta un insumo nuevo. Retorna el id generado o -1 si falla.
     */
    public int guardar(MateriaPrima mp, double stockMinimo, double precioUnitario) {
        String sql = "INSERT INTO insumo "
                   + "(nombre, unidad_medida, cantidad_disponible, stock_minimo, precio_unitario, estado) "
                   + "VALUES (?, ?, ?, ?, ?, 'disponible')";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, mp.getNombre());
            ps.setString(2, mp.getUnidadMedida());
            ps.setDouble(3, mp.getCantidadDisponible());
            ps.setDouble(4, stockMinimo);
            ps.setDouble(5, precioUnitario);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error al guardar insumo: " + e.getMessage());
        }
        return -1;
    }

    // ── READ ────────────────────────────────────────────────────

    public MateriaPrima buscarPorId(int idInsumo) {
        String sql = "SELECT * FROM insumo WHERE id_insumo = ? AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idInsumo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construir(rs);

        } catch (SQLException e) {
            System.out.println("Error al buscar insumo por id: " + e.getMessage());
        }
        return null;
    }

    public MateriaPrima buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM insumo WHERE nombre = ? AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construir(rs);

        } catch (SQLException e) {
            System.out.println("Error al buscar insumo por nombre: " + e.getMessage());
        }
        return null;
    }

    public int obtenerIdPorNombre(String nombre) {
        String sql = "SELECT id_insumo FROM insumo WHERE nombre = ? AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_insumo");

        } catch (SQLException e) {
            System.out.println("Error al obtener id de insumo: " + e.getMessage());
        }
        return -1;
    }

    public List<MateriaPrima> listarTodos() {
        List<MateriaPrima> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumo WHERE activo = TRUE ORDER BY nombre";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    /** Insumos cuyo estado es 'vencido' o cuya fecha_vencimiento ya pasó. */
    public List<MateriaPrima> listarVencidos() {
        List<MateriaPrima> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumo WHERE estado = 'vencido' AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar vencidos: " + e.getMessage());
        }
        return lista;
    }

    /** Insumos con cantidad_disponible ≤ stock_minimo (alerta de escasez). */
    public List<MateriaPrima> listarBajoStock() {
        List<MateriaPrima> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumo "
                   + "WHERE cantidad_disponible <= stock_minimo AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar bajo stock: " + e.getMessage());
        }
        return lista;
    }

    //  UPDATE 

    /**
     * Actualiza cantidad_disponible y estado en BD después de un movimiento.
     * Debe llamarse tras registrar cualquier movimiento.
     */
    public boolean actualizarCantidadYEstado(String nombre, double nuevaCantidad) {
        String estado = nuevaCantidad <= 0 ? "agotado" : "disponible";
        String sql = "UPDATE insumo SET cantidad_disponible = ?, estado = ? WHERE nombre = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, nuevaCantidad);
            ps.setString(2, estado);
            ps.setString(3, nombre);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar cantidad de insumo: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarPrecioUnitario(String nombre, double nuevoPrecio) {
        String sql = "UPDATE insumo SET precio_unitario = ? WHERE nombre = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, nuevoPrecio);
            ps.setString(2, nombre);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar precio de insumo: " + e.getMessage());
            return false;
        }
    }

    public boolean marcarComoVencido(String nombre) {
        String sql = "UPDATE insumo SET estado = 'vencido' WHERE nombre = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al marcar insumo como vencido: " + e.getMessage());
            return false;
        }
    }

    //  DELETE

    public boolean desactivar(String nombre) {
        String sql = "UPDATE insumo SET activo = FALSE WHERE nombre = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al desactivar insumo: " + e.getMessage());
            return false;
        }
    }

    //HELPER

    /**
     * Construye un MateriaPrima desde la fila del ResultSet.
     * La BD no guarda fecha_vencimiento en insumo (va en movimiento),
     * por eso se pasa null y se actualiza cuando llega una entrada.
     */
    private MateriaPrima construir(ResultSet rs) throws SQLException {
        return new MateriaPrima(
            rs.getString("nombre"),
            rs.getDouble("cantidad_disponible"),
            null,                          // fecha_vencimiento viene del último movimiento de entrada
            rs.getString("unidad_medida")
        );
    }
}