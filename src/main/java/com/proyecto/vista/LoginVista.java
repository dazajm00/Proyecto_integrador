package com.proyecto.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.proyecto.controlador.SistemaControlador;
import com.proyecto.modelo.Usuario;

public class LoginVista extends JFrame {

    private final SistemaControlador controlador;

    private JTextField     txtNombre;
    private JPasswordField txtContrasena;
    private JButton        btnIngresar;
    private JLabel         lblMensaje;

    public LoginVista(SistemaControlador controlador) {
        this.controlador = controlador;
        initUI();
    }

    private void initUI() {
        setTitle("Sistema Taty's – Iniciar Sesión");
        setSize(380, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Usuario
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Usuario:"), gbc);
        txtNombre = new JTextField(15);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(txtNombre, gbc);

        // Contraseña
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(new JLabel("Contraseña:"), gbc);
        txtContrasena = new JPasswordField(15);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(txtContrasena, gbc);

        // Botón
        btnIngresar = new JButton("Ingresar");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        // Mensaje de error
        lblMensaje = new JLabel("", SwingConstants.CENTER);
        lblMensaje.setForeground(Color.RED);
        gbc.gridy = 4;
        panel.add(lblMensaje, gbc);

        add(panel);

        // Acción del botón
        btnIngresar.addActionListener(e -> intentarLogin());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void intentarLogin() {
        String nombre    = txtNombre.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (nombre.isEmpty() || contrasena.isEmpty()) {
            lblMensaje.setText("escriba en todos los campos.");
            return;
        }

        boolean ok = controlador.login(nombre, contrasena);
        if (ok) {
            Usuario u = controlador.getUsuarioActual();
            lblMensaje.setText("");
            dispose();
            new MenuPrincipalVista(controlador).setVisible(true);
        } else {
            lblMensaje.setText("Usuario o contraseña incorrectos.");
            txtContrasena.setText("");
        }
    }
}