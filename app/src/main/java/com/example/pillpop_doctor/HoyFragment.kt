package com.example.pillpop_doctor

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class HoyFragment : Fragment() {

    private lateinit var buscadorDNI: SearchView
    private lateinit var listPrescripcionesHoy: RecyclerView
    private lateinit var adapter: PrescripcionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hoy, container, false)

        // Initialize the RecyclerView
        listPrescripcionesHoy = view.findViewById(R.id.ListPrescripcionesHoy)
        listPrescripcionesHoy.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter with your data
        val prescripcionesList: List<Prescripcion> = listOf(
            Prescripcion(1, "Leily Bustamante", "12345678", "20/09/2024"),
            Prescripcion(1, "Bianca Romero", "87654321", "20/09/2024"),
            Prescripcion(1, "Leily Bustamante", "12345678", "20/09/2024"),
            Prescripcion(1, "Bianca Romero", "87654321", "20/09/2024"),
            Prescripcion(1, "Leily Bustamante", "12345678", "20/09/2024"),
            Prescripcion(1, "Bianca Romero", "87654321", "20/09/2024"),
            Prescripcion(1, "Leily Bustamante", "12345678", "20/09/2024"),
            Prescripcion(1, "Bianca Romero", "87654321", "20/09/2024"),
            Prescripcion(1, "Leily Bustamante", "12345678", "20/09/2024"),
            Prescripcion(1, "Bianca Romero", "87654321", "20/09/2024"),
        ) // Inicializa tu lista aquí

        adapter = PrescripcionesAdapter(prescripcionesList)
        listPrescripcionesHoy.adapter = adapter

        // Initialize the SearchView
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

        // Inflate the layout for this fragment
        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HoyFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}