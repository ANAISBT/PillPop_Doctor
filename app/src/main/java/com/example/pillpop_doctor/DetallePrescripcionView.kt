package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetallePrescripcionView : AppCompatActivity() {

    private lateinit var listPastillas: RecyclerView
    private lateinit var adapterpastilla: PastillaAdapter
    private val pastillasList: MutableList<Pastilla> = mutableListOf()
    private lateinit var agregarPrescripcionButton: Button
    private lateinit var dniInput: EditText
    private lateinit var nombreCompletoInput: EditText
    private lateinit var diagnosticoInput: EditText

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
        // Inicializa las vistas
        dniInput = findViewById(R.id.DNIInput)
        nombreCompletoInput = findViewById(R.id.NombreCompletoInput)
        diagnosticoInput = findViewById(R.id.DiagnosticoInput)
        agregarPrescripcionButton.setOnClickListener {
            obtenerDatos()
        }
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

    private fun obtenerDatos() {
        // Obtén los datos ingresados
        val dni = dniInput.text.toString()
        val nombreCompleto = nombreCompletoInput.text.toString()
        val diagnostico = diagnosticoInput.text.toString()

        // Llama al método para agregar la prescripción
        agregarPrescripcion(dni, nombreCompleto, diagnostico)

    }

    private fun agregarPrescripcion(dni: String, nombreCompleto: String, diagnostico: String) {
        val url = "https://pillpop-backend.onrender.com/addPrescripcion"  // Cambia a la URL de tu servidor

        // Crear un objeto JSON para la solicitud
        val jsonObject = JSONObject().apply {
            put("p_dni", dni)
            put("p_doctor_id", doctorId)  // Asegúrate de tener este método implementado
            put("p_diagnostico", diagnostico)
            put("p_fecha", obtenerFechaActual())  // Asegúrate de que esta función esté implementada
        }

        // Crear la solicitud POST
        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                // Manejar la respuesta del servidor
                val jsonResponse = JSONObject(response)
                val mensaje = jsonResponse.getString("mensaje")
                val prescripcionId = jsonResponse.getInt("id")

                Log.d("Success", "$mensaje, ID: $prescripcionId")
                // Llamar al método para insertar las pastillas
                insertarPastillas(prescripcionId)
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al agregar prescripción: ${error.message}")
            }) {
            override fun getBody(): ByteArray {
                return jsonObject.toString().toByteArray(Charsets.UTF_8)
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Agregar la solicitud a la cola de solicitudes de Volley
        Volley.newRequestQueue(this).add(stringRequest)
    }
    private fun insertarPastillas(prescripcionId: Int) {
        for (pastilla in pastillasList) {
            val url = "https://pillpop-backend.onrender.com/insertarPastillas"  // Cambia a la URL de tu servidor

            // Crear un objeto JSON para la solicitud
            val jsonObject = JSONObject().apply {
                put("nombre", pastilla.pastilla_nombre)
                put("cantidad", pastilla.cantidad)
                put("dosis", pastilla.dosis)
                put("cantidad_sobrante", pastilla.cantidad)
                put("frecuencia_id", pastilla.FrecuenciaId)
                // Fusionar fechaInicio y hora_dosis
                val fechaPartes = pastilla.fechaInicio.split("/") // Split por "/"
                val fechaFormateada = "${fechaPartes[2]}-${fechaPartes[1]}-${fechaPartes[0]} ${pastilla.hora}:00:00" // Formato "yyyy-MM-dd HH:mm:ss"
                put("fecha_inicio", fechaFormateada)
                put("observaciones", pastilla.observaciones)
                put("prescripcion_id", prescripcionId)
            }

            // Crear la solicitud POST
            val stringRequest = object : StringRequest(Method.POST, url,
                Response.Listener<String> { response ->
                    Log.d("Success", "Pastilla insertada correctamente: $response")
                                          },
                Response.ErrorListener { error ->
                    Log.e("Error", "Error al insertar pastilla: ${error.message}")
                }) {
                override fun getBody(): ByteArray {
                    return jsonObject.toString().toByteArray(Charsets.UTF_8)
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            // Agregar la solicitud a la cola de solicitudes de Volley
            Volley.newRequestQueue(this).add(stringRequest)
        }

        val intent = Intent(this, HomeView::class.java)
        startActivity(intent)
    }
    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}

