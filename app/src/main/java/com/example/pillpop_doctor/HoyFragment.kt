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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.pillpop_doctor.doctorId

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

        // Inicializa el adapter con una lista vacía
        adapter = PrescripcionesAdapter(emptyList())
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

        // Llamar a la función para obtener las prescripciones
        obtenerPrescripciones(doctorId ?: -1)
        return view
    }

    fun obtenerPrescripciones(doctorId: Int) {
        val url = "https://pillpop-backend.onrender.com/obtenerPrescripcionesXDoctorFecha"

        val queue = Volley.newRequestQueue(requireContext())
        val fechaHoy = try {
            SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).format(Calendar.getInstance().time)
        } catch (e: Exception) {
            e.printStackTrace()
            "" // Devuelve una fecha vacía si ocurre un error (manejo básico)
        }


        // Crear el objeto JSON con los parámetros
        val requestBody = mapOf("doctorId" to doctorId, "fechaHoy" to fechaHoy)

        val jsonArrayRequest = object : JsonArrayRequest(
            Request.Method.POST, url, null,
            { response ->
                val prescripcionesList = mutableListOf<Prescripcion>()
                if (response.length() == 0) {
                    Toast.makeText(context, "No hay prescripciones para hoy.", Toast.LENGTH_SHORT).show()
                } else {
                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)
                        val prescripcion = Prescripcion(
                            jsonObject.getInt("prescripcionId"),
                            jsonObject.getString("nombreCompleto"),
                            jsonObject.getInt("dni"),
                            jsonObject.getString("fecha")
                        )
                        prescripcionesList.add(prescripcion)
                    }
                    // Actualiza el adapter con la lista obtenida
                    adapter = PrescripcionesAdapter(prescripcionesList)
                    listPrescripcionesHoy.adapter = adapter
                }
            },
            { error: VolleyError ->
                // Manejo de errores
                error.printStackTrace()
                //Toast.makeText(context, "Error al obtener las prescripciones. Intente de nuevo.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBody(): ByteArray {
                return JSONObject(requestBody).toString().toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/json")
            }
        }

        // Añadir la solicitud a la cola
        queue.add(jsonArrayRequest)
    }
}
