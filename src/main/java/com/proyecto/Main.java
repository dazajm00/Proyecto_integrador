package com.proyecto;

import java.time.LocalDate;

import com.proyecto.modelo.MateriaPrima;
import com.proyecto.modelo.MovimientoEntrada;
import com.proyecto.modelo.ProductoIngresado;
import com.proyecto.modelo.ValoracionProducto;

public class Main {
    public static void main(String[] args) {
        // Crear una materia prima
        MateriaPrima harina = new MateriaPrima("Harina", 100.0, LocalDate.of(2024, 12, 31), "kg");
        
        // Crear un proveedor
        ProductoIngresado proveedor = new ProductoIngresado("Molino San Juan", "555-1234", "molino@example.com");
        
        // Crear un movimiento de entrada (compra)
        MovimientoEntrada entrada = proveedor.registrarCompra(
            harina, 
            50.0,           // cantidad
            200.0,          // precio
            LocalDate.of(2024, 12, 31),  // fecha vencimiento
            proveedor.getNombre()
        );
        entrada.registrar();
        
        // Crear una valoración de producto
        ValoracionProducto valoracion = new ValoracionProducto(
            "Pan",          // nombre producto
            100,            // cantidad hecha (unidades)
            5.0,            // precio venta
            150.0           // costos insumos
        );
        System.out.println(valoracion);
    }
}   
