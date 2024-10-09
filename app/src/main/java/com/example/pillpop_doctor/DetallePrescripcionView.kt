package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetallePrescripcionView : AppCompatActivity() {

    private lateinit var listPastillas: RecyclerView
    private lateinit var adapterpastilla: PastillaAdapter
    private val pastillasList: MutableList<Pastilla> = mutableListOf()
    private lateinit var agregarPrescripcionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_prescripcion)

        // Find the button by ID
        val anadirPastillaButton: ImageButton = findViewById(R.id.AnadirPastilla)

        // Set OnClickListener to navigate to DetallePastillavIEW
        anadirPastillaButton.setOnClickListener {
            // Create an Intent to navigate to DetallePastillavIEW
            val intent = Intent(this, DetallePastillaView::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_PASTILLA)
        }

        listPastillas = findViewById(R.id.ListPastillas)
        listPastillas.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador con la lista mutable
        adapterpastilla = PastillaAdapter(pastillasList)
        listPastillas.adapter = adapterpastilla

        agregarPrescripcionButton = findViewById(R.id.aceptarButton)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_PASTILLA && resultCode == RESULT_OK) {
            // Obtener datos desde el intent
            val nuevaPastilla = data?.getParcelableExtra<Pastilla>("nueva_pastilla")
            nuevaPastilla?.let {
                // Añadir la nueva pastilla a la lista
                pastillasList.add(it)
                adapterpastilla.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_ADD_PASTILLA = 1
    }
}

