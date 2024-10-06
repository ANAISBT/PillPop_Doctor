package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InicioView: AppCompatActivity() {
    private lateinit var btnRegistro: Button
    private lateinit var btnLogin: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_view)
        btnRegistro = findViewById(R.id.buttonInicio)
        btnLogin = findViewById(R.id.IniciarSesion)

        btnRegistro.setOnClickListener {
            val intent = Intent(this, RegisterView::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener{
            val intent = Intent(this, LoginView::class.java)
            startActivity(intent)
        }
    }
}