package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HoyFragment : Fragment() {

    private lateinit var buscadorDNI: SearchView
    private lateinit var listPrescripcionesHoy: RecyclerView
    private lateinit var adapter: PrescripcionesAdapter
    private lateinit var agregarBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hoy, container, false)

        // Encuentra el TextView
        val textView4 = view.findViewById<TextView>(R.id.textViewFecha4)

        // Obtén la fecha actual usando Calendar
        val calendar = Calendar.getInstance()

        // Formatea la fecha
        val formatter = SimpleDateFormat("EEEE dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
        val formattedDate = formatter.format(calendar.time)

        // Asigna la fecha formateada al TextView
        textView4.text = formattedDate

        // Inicializa el RecyclerView
        listPrescripcionesHoy = view.findViewById(R.id.ListPrescripcionesHoy)
        listPrescripcionesHoy.layoutManager = LinearLayoutManager(context)

        // Inicializa el adapter con tu lista de datos
        val prescripcionesList: List<Prescripcion> = listOf(
            Prescripcion(1, "Leily Bustamante", "12345678", "20/09/2024"),
            Prescripcion(2, "Bianca Romero", "87654321", "20/09/2024")
        )

        adapter = PrescripcionesAdapter(prescripcionesList)
        listPrescripcionesHoy.adapter = adapter

        // Inicializa el SearchView
        buscadorDNI = view.findViewById(R.id.searchViewDNI)

        buscadorDNI.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        // Encuentra el botón "Agregar" por su ID
        agregarBtn = view.findViewById(R.id.AgregarBtn)

        // Agrega un listener para el botón "Agregar"
        agregarBtn.setOnClickListener {
            // Navega a otra actividad
            val intent = Intent(requireActivity(), DetallePrescripcionView::class.java)
            startActivity(intent)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HoyFragment().apply {
                arguments = Bundle().apply {
                    // Puedes pasar parámetros a través del bundle aquí
                }
            }
    }
}
