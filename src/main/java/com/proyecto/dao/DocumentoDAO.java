package com.proyecto.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import com.proyecto.config.ConexionBD;

/**
 * DAO para las tablas `tipo_documento` y `documento`.
 *
 * `tipo_documento` define los tipos de documento (orden de compra,
 * orden de producción, etc.) y si afectan el stock.
 *
 * `documento` es la cabecera de cada movimiento, con su tipo, tercero
 * asociado, fecha, etc. El detalle de productos se guarda en otra tabla
 * (`detalle_movimiento`) que gestiona MovimientoDAO.
 */
public class DocumentoDAO {

    //  TIPO_DOCUMENTO
    // 
    /** Busca el id de un tipo de documento por su nombre. */
    public int obtenerIdTipoDoc(String nombre) {
        String sql = "SELECT id_tipo_doc FROM tipo_documento WHERE nombre = ? AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_tipo_doc");

        } catch (SQLException e) {
            System.out.println("Error al buscar tipo de documento: " + e.getMessage());
        }
        return -1;
    }

    /** Inserta un nuevo tipo de documento y devuelve su id. */
    public int guardarTipoDoc(String nombre, String descripcion, String afectaStock) {
        String sql = "INSERT INTO tipo_documento (nombre, descripcion, afecta_stock) "
                   + "VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setString(3, afectaStock.toLowerCase()); // 'entrada'|'salida'|'ajuste'
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error al guardar tipo de documento: " + e.getMessage());
        }
        return -1;
    }

    
    //  DOCUMENTO

    /**
     * Crea un documento cabecera y lo confirma directamente.
     * @param idTipoDoc   id del tipo de documento
     * @param idTercero   id del tercero (proveedor o empleado)
     * @param numeroDoc   número externo (factura, orden...), puede ser null
     * @param fecha       fecha del documento
     * @param observaciones texto libre, puede ser null
     * @return id_documento generado, o -1 si falla
     */
    public int crearDocumento(int idTipoDoc, int idTercero,
                               String numeroDoc, LocalDate fecha,
                               String observaciones) {
        String sql = "INSERT INTO documento "
                   + "(id_tipo_doc, id_tercero, numero_doc, fecha, observaciones, estado) "
                   + "VALUES (?, ?, ?, ?, ?, 'confirmado')";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    idTipoDoc);
            ps.setInt(2,    idTercero);
            ps.setString(3, numeroDoc);
            ps.setDate(4,   Date.valueOf(fecha));
            ps.setString(5, observaciones);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error al crear documento: " + e.getMessage());
        }
        return -1;
    }

    /** Anula un documento existente. */
    public boolean anular(int idDocumento) {
        String sql = "UPDATE documento SET estado = 'anulado' WHERE id_documento = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDocumento);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al anular documento: " + e.getMessage());
            return false;
        }
    }

    // ── IDs de tipos de documento estándar ──────────────────────
    // Métodos de conveniencia para que MovimientoDAO no necesite
    // conocer los nombres de los tipos de documento.

    public int idTipoOrdenCompra()          { return obtenerIdTipoDoc("Orden de compra"); }
    public int idTipoOrdenProduccion()      { return obtenerIdTipoDoc("Orden de producción"); }
    public int idTipoDevolucionProveedor()  { return obtenerIdTipoDoc("Devolución a proveedor"); }
    public int idTipoEntradaSobrante()      { return obtenerIdTipoDoc("Entrada sobrante producción"); }
    public int idTipoAjuste()              { return obtenerIdTipoDoc("Ajuste de inventario"); }
    public int idTipoInventarioInicial()   { return obtenerIdTipoDoc("Inventario inicial"); }
}
