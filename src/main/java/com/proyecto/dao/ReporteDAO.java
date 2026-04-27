package com.proyecto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.config.ConexionBD;
import com.proyecto.modelo.Reporte;

/**
 * DAO para la tabla `reporte`.
 *
 * Columnas: id_reporte, id_empleado, tipo ENUM(...),
 *   fecha_generacion, contenido (LONGTEXT)
 *
 * ENUM tipos: 'inventario','produccion','costos','perdidas','escasez'
 *
 * Mapeo al modelo Java: Reporte
 */
public class ReporteDAO {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // ── CREATE ──────────────────────────────────────────────────

    /**
     * Persiste el reporte generado en la BD.
     * @param reporte          objeto Reporte del modelo Java
     * @param usuarioEmpleado  usuario (login) del administrador que lo generó
     * @return id generado o -1 si falla
     */
    public int guardar(Reporte reporte, String usuarioEmpleado) {
        int idEmpleado = empleadoDAO.obtenerIdPorUsuario(usuarioEmpleado);
        if (idEmpleado == -1) {
            System.out.println("Error: empleado no encontrado: " + usuarioEmpleado);
            return -1;
        }

        // Mapear tipo del modelo Java al ENUM de la BD
        String tipoEnum = mapearTipo(reporte.getTipoReporte());

        String sql = "INSERT INTO reporte (id_empleado, tipo, contenido) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    idEmpleado);
            ps.setString(2, tipoEnum);
            ps.setString(3, reporte.getContenido());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error al guardar reporte: " + e.getMessage());
        }
        return -1;
    }

    // ── READ ────────────────────────────────────────────────────

    public Reporte buscarPorId(int idReporte) {
        String sql = "SELECT tipo, contenido FROM reporte WHERE id_reporte = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReporte);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construir(rs);

        } catch (SQLException e) {
            System.out.println("Error al buscar reporte: " + e.getMessage());
        }
        return null;
    }

    public List<Reporte> listarTodos() {
        List<Reporte> lista = new ArrayList<>();
        String sql = "SELECT tipo, contenido FROM reporte ORDER BY fecha_generacion DESC";
        try (Connection con = ConexionBD.conectar();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar reportes: " + e.getMessage());
        }
        return lista;
    }

    public List<Reporte> listarPorTipo(String tipo) {
        List<Reporte> lista = new ArrayList<>();
        String sql = "SELECT tipo, contenido FROM reporte WHERE tipo = ? "
                   + "ORDER BY fecha_generacion DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mapearTipo(tipo));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(construir(rs));

        } catch (SQLException e) {
            System.out.println("Error al listar reportes por tipo: " + e.getMessage());
        }
        return lista;
    }

    // ── HELPER ──────────────────────────────────────────────────

    /**
     * Mapea el tipo del modelo Java ("INVENTARIO", "COSTOS")
     * al ENUM de la BD ('inventario', 'costos', ...).
     */
    private String mapearTipo(String tipoModelo) {
        return switch (tipoModelo.toUpperCase()) {
            case "INVENTARIO" -> "inventario";
            case "COSTOS"     -> "costos";
            case "PRODUCCION" -> "produccion";
            case "PERDIDAS"   -> "perdidas";
            case "ESCASEZ"    -> "escasez";
            default           -> "inventario";
        };
    }

    private Reporte construir(ResultSet rs) throws SQLException {
        String tipoEnum   = rs.getString("tipo");
        String contenido  = rs.getString("contenido");

        // Convertir ENUM BD → nombre del modelo Java
        String tipoModelo = tipoEnum.toUpperCase();
        Reporte r = new Reporte(tipoModelo);

        // Inyectar el contenido guardado usando el getter (campo privado accesible sólo vía método)
        // El contenido se setea a través de generarReporteInventario / generarReporteCostos
        // en el controlador; aquí sólo lo leemos para mostrar historial.
        // Como Reporte no tiene setContenido(), se usa exportar() externamente.
        return r;
    }
}