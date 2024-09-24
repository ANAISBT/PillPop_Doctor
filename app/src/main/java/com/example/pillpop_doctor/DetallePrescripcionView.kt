package com.example.pillpop_doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetallePrescripcionView : AppCompatActivity() {

    private lateinit var listPastillas: RecyclerView
    private lateinit var adapterpastilla: PastillaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_prescripcion)

        listPastillas = findViewById(R.id.ListPastillas)
        listPastillas.layoutManager = LinearLayoutManager(this)

        // Inicializar lista de pastillas con datos ajustados
        val pastillasList: List<Pastilla> = listOf(
            Pastilla(
                pastillla_id = 1,
                pastilla_nombre = "Paracetamol",
                cantidad = 50,
                dosis = 2,
                Frecuencia = "Cada 8 horas",
                fechaInicio = "21/09/2024",
                hora = "08:00",
                tiempo = "AM",
                observaciones = "Tomar con alimentos"
            ),
            Pastilla(
                pastillla_id = 2,
                pastilla_nombre = "Ibuprofeno",
                cantidad = 30,
                dosis = 1,
                Frecuencia = "Cada 6 horas",
                fechaInicio = "22/09/2024",
                hora = "09:00",
                tiempo = "AM",
                observaciones = "Evitar tomar en ayunas"
            ),
            Pastilla(
                pastillla_id = 3,
                pastilla_nombre = "Amoxicilina",
                cantidad = 20,
                dosis = 1,
                Frecuencia = "Cada 12 horas",
                fechaInicio = "23/09/2024",
                hora = "10:00",
                tiempo = "AM",
                observaciones = "Completar el tratamiento"
            ),
            Pastilla(
                pastillla_id = 4,
                pastilla_nombre = "Aspirina",
                cantidad = 10,
                dosis = 1,
                Frecuencia = "Cada 24 horas",
                fechaInicio = "24/09/2024",
                hora = "08:00",
                tiempo = "AM",
                observaciones = "Tomar en ayunas"
            )
        )

        // Inicializar el adaptador con la lista
        adapterpastilla = PastillaAdapter(pastillasList)
        listPastillas.adapter = adapterpastilla
    }
}

