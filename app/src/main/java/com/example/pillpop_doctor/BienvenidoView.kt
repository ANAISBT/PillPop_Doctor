package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BienvenidoView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenido_view)

        // Encuentra el botón por su ID
        val button = findViewById<Button>(R.id.button)

        // Agrega un listener para el botón
        button.setOnClickListener {
            // Intenta navegar a otra actividad
            val intent = Intent(this@BienvenidoView, HomeView::class.java)
            startActivity(intent)  // Inicia la nueva actividad
        }
    }
}