package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InicioView : AppCompatActivity() {
    private lateinit var btnRegistro: Button
    private lateinit var btnLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_view)

        // Enlazando el Button y TextView con sus IDs
        btnRegistro = findViewById(R.id.buttonInicio)  // Este es tu botón de registro
        btnLogin = findViewById(R.id.IniciarSesion)    // Este es tu TextView de 'Iniciar Sesión'

        // Listener para el botón de registro
        btnRegistro.setOnClickListener {
            val intent = Intent(this, RegisterView::class.java)
            startActivity(intent)
        }

        // Listener para el TextView de 'Iniciar Sesión'
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginView::class.java)
            startActivity(intent)
        }
    }
}
