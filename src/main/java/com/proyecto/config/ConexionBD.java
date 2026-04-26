package com.proyecto.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/panaderia_tatys";

    private static final String USUARIO = "root";

    private static final String CONTRASENA = "Kamila1234*";

    public static Connection conectar() {

        Connection conexion = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            conexion = DriverManager.getConnection(
                    URL,
                    USUARIO,
                    CONTRASENA
            );

            System.out.println("Conexión exitosa.");

        } catch (ClassNotFoundException e) {

            System.out.println("Driver no encontrado.");
            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println("Error al conectar.");
            e.printStackTrace();
        }

        return conexion;
    }

    public static void cerrar(Connection conexion) {

        try {

            if (conexion != null && !conexion.isClosed()) {

                conexion.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}