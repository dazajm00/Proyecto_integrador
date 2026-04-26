package com.proyecto.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.proyecto.controlador.SistemaControlador;
import com.proyecto.modelo.MateriaPrima;
import com.proyecto.modelo.ProductoIngresado;
import com.proyecto.modelo.Reporte;
import com.proyecto.modelo.Usuario;
import com.proyecto.modelo.ValoracionProducto;

public class MenuPrincipalVista extends JFrame {

    private final SistemaControlador controlador;
    private JTabbedPane tabs;

    public MenuPrincipalVista(SistemaControlador controlador) {
        this.controlador = controlador;
        initUI();
    }

    private void initUI() {
        Usuario u = controlador.getUsuarioActual();
        setTitle("Sistema Taty's – " + u.getNombre() + " [" + u.getRol() + "]");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabs = new JTabbedPane();
        tabs.addTab("Inventario",   buildInventarioPanel());
        tabs.addTab("Entradas",     buildEntradaPanel());
        tabs.addTab("Salidas",      buildSalidaPanel());

        // Solo admin ve ajustes, proveedores y reportes
        if (u.tienePermiso("REGISTRAR_AJUSTE")) {
            tabs.addTab("Ajustes",      buildAjustePanel());
            tabs.addTab("Proveedores",  buildProveedoresPanel());
            tabs.addTab("Valoración",   buildValoracionPanel());
            tabs.addTab("Reportes",     buildReportesPanel());
        }

        // Barra superior con usuario y cerrar sesión
        JPanel top = new JPanel(new BorderLayout());
        JLabel lblUser = new JLabel("  Bienvenido: " + u.getNombre());
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton btnLogout = new JButton("Cerrar sesión");
        btnLogout.addActionListener(e -> {
            controlador.logout();
            dispose();
            new LoginVista(controlador).setVisible(true);
        });
        top.add(lblUser, BorderLayout.WEST);
        top.add(btnLogout, BorderLayout.EAST);
        top.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    // ── TAB INVENTARIO ─────────────────────────────────────────

    private JPanel buildInventarioPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Nombre", "Cantidad", "Unidad", "Vencimiento", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(model);
        JScrollPane scroll = new JScrollPane(tabla);

        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> cargarInventario(model));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Formulario para agregar insumo
        JTextField fNombre   = new JTextField(10);
        JTextField fCantidad = new JTextField(5);
        JTextField fUnidad   = new JTextField(5);
        JTextField fVence    = new JTextField(8); // YYYY-MM-DD
        JButton    btnAgregar = new JButton("Agregar Insumo");

        botones.add(new JLabel("Nombre:")); botones.add(fNombre);
        botones.add(new JLabel("Cantidad:")); botones.add(fCantidad);
        botones.add(new JLabel("Unidad:")); botones.add(fUnidad);
        botones.add(new JLabel("Vence (AAAA-MM-DD):")); botones.add(fVence);
        botones.add(btnAgregar);
        botones.add(btnRefresh);

        btnAgregar.addActionListener(e -> {
            try {
                String  nombre   = fNombre.getText().trim();
                double  cantidad = Double.parseDouble(fCantidad.getText().trim());
                String  unidad   = fUnidad.getText().trim();
                LocalDate vence  = LocalDate.parse(fVence.getText().trim());
                controlador.agregarMateriaPrima(nombre, cantidad, vence, unidad);
                cargarInventario(model);
                fNombre.setText(""); fCantidad.setText("");
                fUnidad.setText(""); fVence.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cargarInventario(model);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarInventario(DefaultTableModel model) {
        model.setRowCount(0);
        for (MateriaPrima mp : controlador.getInventario()) {
            String estado = mp.verificarVencimiento() ? "VENCIDO"
                          : mp.esBajoStock(5) ? "BAJO STOCK" : "OK";
            model.addRow(new Object[]{
                mp.getNombre(), mp.getCantidadDisponible(),
                mp.getUnidadMedida(), mp.getFechaVencimiento(), estado
            });
        }
    }

    // ── TAB ENTRADAS ───────────────────────────────────────────

    private JPanel buildEntradaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbInsumo    = new JComboBox<>();
        JComboBox<String> cmbProveedor = new JComboBox<>();
        JTextField fCantidad  = new JTextField(10);
        JTextField fPrecio    = new JTextField(10);
        JTextField fVence     = new JTextField(10);
        JButton    btnRegistrar = new JButton("Registrar Entrada");

        int row = 0;
        addRow(panel, g, row++, "Insumo:",      cmbInsumo);
        addRow(panel, g, row++, "Proveedor:",   cmbProveedor);
        addRow(panel, g, row++, "Cantidad:",    fCantidad);
        addRow(panel, g, row++, "Precio Unit:", fPrecio);
        addRow(panel, g, row++, "Vencimiento:", fVence);

        g.gridx = 1; g.gridy = row;
        panel.add(btnRegistrar, g);

        // Llenar combos al mostrar
        btnRegistrar.addActionListener(e -> {
            try {
                MateriaPrima mp = controlador.getInventario().stream()
                    .filter(x -> x.getNombre().equals(cmbInsumo.getSelectedItem()))
                    .findFirst().orElseThrow();
                ProductoIngresado prov = controlador.getProveedores().stream()
                    .filter(x -> x.getNombre().equals(cmbProveedor.getSelectedItem()))
                    .findFirst().orElseThrow();
                double    cant  = Double.parseDouble(fCantidad.getText().trim());
                double    precio = Double.parseDouble(fPrecio.getText().trim());
                LocalDate vence = LocalDate.parse(fVence.getText().trim());
                controlador.registrarEntrada(mp, prov, cant, precio, vence,
                    controlador.getUsuarioActual().getNombre());
                JOptionPane.showMessageDialog(this, "Entrada registrada correctamente.");
                fCantidad.setText(""); fPrecio.setText(""); fVence.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Refrescar combos cuando se muestra la pestaña
        tabs.addChangeListener(ev -> {
            cmbInsumo.removeAllItems();
            controlador.getInventario().forEach(mp -> cmbInsumo.addItem(mp.getNombre()));
            cmbProveedor.removeAllItems();
            controlador.getProveedores().forEach(p -> cmbProveedor.addItem(p.getNombre()));
        });

        return panel;
    }

    // ── TAB SALIDAS ────────────────────────────────────────────

    private JPanel buildSalidaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbInsumo = new JComboBox<>();
        JTextField fCantidad  = new JTextField(10);
        JTextField fIdLote    = new JTextField(10);
        JTextField fStockMin  = new JTextField(10);
        JButton    btnRegistrar = new JButton("Registrar Salida");

        int row = 0;
        addRow(panel, g, row++, "Insumo:",      cmbInsumo);
        addRow(panel, g, row++, "Cantidad:",    fCantidad);
        addRow(panel, g, row++, "ID Lote:",     fIdLote);
        addRow(panel, g, row++, "Stock Mínimo:", fStockMin);

        g.gridx = 1; g.gridy = row;
        panel.add(btnRegistrar, g);

        btnRegistrar.addActionListener(e -> {
            try {
                MateriaPrima mp = controlador.getInventario().stream()
                    .filter(x -> x.getNombre().equals(cmbInsumo.getSelectedItem()))
                    .findFirst().orElseThrow();
                double cant    = Double.parseDouble(fCantidad.getText().trim());
                int    idLote  = Integer.parseInt(fIdLote.getText().trim());
                double stockMin = Double.parseDouble(fStockMin.getText().trim());
                controlador.registrarSalida(mp, cant, idLote, stockMin,
                    controlador.getUsuarioActual().getNombre());
                JOptionPane.showMessageDialog(this, "Salida registrada correctamente.");
                fCantidad.setText(""); fIdLote.setText(""); fStockMin.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ── TAB AJUSTES ────────────────────────────────────────────

    private JPanel buildAjustePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbInsumo  = new JComboBox<>();
        JTextField fCantidad = new JTextField(10);
        JTextField fMotivo   = new JTextField(20);
        JComboBox<String> cmbTipo = new JComboBox<>(
            new String[]{"PERDIDA", "DANIO", "CORRECCION"});
        JButton btnRegistrar = new JButton("Registrar Ajuste");

        int row = 0;
        addRow(panel, g, row++, "Insumo:",   cmbInsumo);
        addRow(panel, g, row++, "Cantidad (+ / -):", fCantidad);
        addRow(panel, g, row++, "Motivo:",   fMotivo);
        addRow(panel, g, row++, "Tipo:",     cmbTipo);

        g.gridx = 1; g.gridy = row;
        panel.add(btnRegistrar, g);

        btnRegistrar.addActionListener(e -> {
            try {
                MateriaPrima mp = controlador.getInventario().stream()
                    .filter(x -> x.getNombre().equals(cmbInsumo.getSelectedItem()))
                    .findFirst().orElseThrow();
                double cant  = Double.parseDouble(fCantidad.getText().trim());
                String motivo = fMotivo.getText().trim();
                String tipo  = (String) cmbTipo.getSelectedItem();
                controlador.registrarAjuste(mp, cant, motivo, tipo,
                    controlador.getUsuarioActual().getNombre());
                JOptionPane.showMessageDialog(this, "Ajuste registrado correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ── TAB PROVEEDORES ────────────────────────────────────────

    private JPanel buildProveedoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Nombre", "Teléfono", "Correo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(model);

        JTextField fNombre   = new JTextField(10);
        JTextField fTelefono = new JTextField(10);
        JTextField fCorreo   = new JTextField(12);
        JButton    btnAgregar = new JButton("Agregar Proveedor");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nombre:")); form.add(fNombre);
        form.add(new JLabel("Teléfono:")); form.add(fTelefono);
        form.add(new JLabel("Correo:")); form.add(fCorreo);
        form.add(btnAgregar);

        btnAgregar.addActionListener(e -> {
            try {
                controlador.agregarProveedor(fNombre.getText().trim(),
                    fTelefono.getText().trim(), fCorreo.getText().trim());
                model.setRowCount(0);
                for (ProductoIngresado p : controlador.getProveedores()) {
                    model.addRow(new Object[]{p.getNombre(), p.getTelefono(), p.getCorreo()});
                }
                fNombre.setText(""); fTelefono.setText(""); fCorreo.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cargar existentes
        for (ProductoIngresado p : controlador.getProveedores()) {
            model.addRow(new Object[]{p.getNombre(), p.getTelefono(), p.getCorreo()});
        }

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    // ── TAB VALORACIÓN ─────────────────────────────────────────

    private JPanel buildValoracionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Producto", "Unidades", "Precio Venta", "Costo Unit.", "Ganancia", "Margen %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(model);

        JTextField fProducto  = new JTextField(10);
        JTextField fUnidades  = new JTextField(5);
        JTextField fPrecio    = new JTextField(7);
        JTextField fCostos    = new JTextField(7);
        JButton    btnAgregar = new JButton("Agregar Valoración");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Producto:")); form.add(fProducto);
        form.add(new JLabel("Unidades:")); form.add(fUnidades);
        form.add(new JLabel("Precio Venta:")); form.add(fPrecio);
        form.add(new JLabel("Costo Insumos:")); form.add(fCostos);
        form.add(btnAgregar);

        btnAgregar.addActionListener(e -> {
            try {
                String producto = fProducto.getText().trim();
                int    unidades = Integer.parseInt(fUnidades.getText().trim());
                double precio   = Double.parseDouble(fPrecio.getText().trim());
                double costos   = Double.parseDouble(fCostos.getText().trim());
                controlador.agregarValoracion(producto, unidades, precio, costos);
                model.setRowCount(0);
                for (ValoracionProducto vp : controlador.getValoraciones()) {
                    model.addRow(new Object[]{
                        vp.getNombreProducto(), vp.getCantidadHecha(),
                        String.format("%.2f", vp.getPrecioVenta()),
                        String.format("%.2f", vp.calcularCostoUnitario()),
                        String.format("%.2f", vp.calcularGanancia()),
                        String.format("%.1f%%", vp.calcularMargen())
                    });
                }
                fProducto.setText(""); fUnidades.setText("");
                fPrecio.setText(""); fCostos.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    // ── TAB REPORTES ───────────────────────────────────────────

    private JPanel buildReportesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea areaReporte = new JTextArea();
        areaReporte.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaReporte.setEditable(false);

        JButton btnInventario = new JButton("Reporte Inventario");
        JButton btnCostos     = new JButton("Reporte Costos");

        btnInventario.addActionListener(e -> {
            try {
                Reporte r = controlador.generarReporte("INVENTARIO");
                areaReporte.setText(r.getContenido());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCostos.addActionListener(e -> {
            try {
                Reporte r = controlador.generarReporte("COSTOS");
                areaReporte.setText(r.getContenido());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnInventario);
        botones.add(btnCostos);

        panel.add(botones, BorderLayout.NORTH);
        panel.add(new JScrollPane(areaReporte), BorderLayout.CENTER);
        return panel;
    }

    // ── Utilitario para filas de formulario ────────────────────

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.3;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 0.7;
        p.add(comp, g);
    }
}