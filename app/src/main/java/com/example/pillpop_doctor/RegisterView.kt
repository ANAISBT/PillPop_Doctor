package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterView: AppCompatActivity() {
    private lateinit var btnRegistrar: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_view)

        val spinnerGenero : Spinner = findViewById(R.id.generosDrop)
        val spinnerEspecialidad : Spinner = findViewById(R.id.EspecialidadDrop)

        val generosList= resources.getStringArray(R.array.gender_array)
        val especialidadList = resources.getStringArray(R.array.Especialidad_array)

        val adapterGeneros = ArrayAdapter(this,android.R.layout.simple_spinner_item,generosList)
        val adapterEspecialidad = ArrayAdapter(this,android.R.layout.simple_spinner_item,especialidadList)

        spinnerGenero.adapter = adapterGeneros
        spinnerEspecialidad.adapter = adapterEspecialidad

        btnRegistrar = findViewById(R.id.Registrarse_btn)

        btnRegistrar.setOnClickListener {

            val intent = Intent(this, BienvenidoView::class.java)
            startActivity(intent)
        }


    }
}