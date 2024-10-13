package com.example.pillpop_doctor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
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

class EditarPrescripcionView : AppCompatActivity() {
    private lateinit var listPastillas: RecyclerView
    private lateinit var adapterpastilla: PastillaAdapter
    private lateinit var dniInput: EditText
    private lateinit var nombreCompletoInput: EditText
    private lateinit var diagnosticoInput: EditText
    private lateinit var buscarPacienteButton: ImageButton
    private lateinit var progressDialog: ProgressDialog
    private lateinit var progressDialog2: ProgressDialog
    private val pastillasList: MutableList<Pastilla> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_prescripcion)

        val prescripcionId = "25"
            //intent.getStringExtra("PRESCRIPCION_ID") // Recupera el valor

        listPastillas = findViewById(R.id.ListPastillas)
        listPastillas.layoutManager = LinearLayoutManager(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Buscando Paciente...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        progressDialog2 = ProgressDialog(this)
        progressDialog2.setMessage("Cargando datos...")
        progressDialog2.setCancelable(false) // Evitar que el usuario lo pueda cancelar
        // Inicializa las vistas
        dniInput = findViewById(R.id.DNIInput)
        nombreCompletoInput = findViewById(R.id.NombreCompletoInput)
        diagnosticoInput = findViewById(R.id.DiagnosticoInput)

        if (prescripcionId != null) {
            obtenerDatosPrescripcion(prescripcionId)
        }

        // Inicializa el botón de búsqueda
        buscarPacienteButton = findViewById(R.id.BuscarPaciente)
        buscarPacienteButton.setOnClickListener {
            buscarPacientePorDNI()
        }
    }

    private fun obtenerDatosPrescripcion(id:String) {
        progressDialog2.show() // Mostrar el loader antes de hacer la petición
        val id = id.toInt()

        // Crear un objeto JSON para la solicitud
        val jsonObject = JSONObject().apply {
            put("id", id)  // Asegúrate de que este campo coincida con lo que espera tu servidor
        }

        val url = "https://pillpop-backend.onrender.com/obtenerDatosPrescripcion"  // Cambia a la URL de tu servidor

        // Crear la solicitud POST
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Manejar la respuesta del servidor
                val jsonResponse = JSONObject(response)

                // Obtener los datos de la prescripción
                val prescripcion = jsonResponse.getJSONObject("prescripcion")
                val pastillas = jsonResponse.getJSONArray("pastillas")

                // Llenar los campos en la vista
                nombreCompletoInput.setText(prescripcion.getString("nombreCompleto"))
                dniInput.setText(prescripcion.getString("dni"))
                diagnosticoInput.setText(prescripcion.getString("diagnostico"))

                for (i in 0 until pastillas.length()) {
                    val pastilla = pastillas.getJSONObject(i)

                    // Convertir la fecha y la hora
                    val fechaInicioString = pastilla.getString("fecha_inicio")
                    val horaString = fechaInicioString.substring(11, 16) // Extraer la hora

                    // Formatear la fecha
                    val formattedDate = formatearFecha(fechaInicioString)
                    pastillasList.add(
                        Pastilla(
                            pastillla_id = pastilla.getInt("id"),
                            pastilla_nombre = pastilla.getString("nombre"),
                            cantidad = pastilla.getInt("cantidad"),
                            dosis = pastilla.getInt("dosis"),
                            FrecuenciaId = pastilla.getInt("frecuencia_id"),
                            Frecuencia = pastilla.getString("frecuencia_tipo"),
                            fechaInicio = formattedDate,
                            hora = horaString,
                            observaciones = pastilla.getString("observaciones")
                        )
                    )
                }

                // Actualizar el adaptador con la nueva lista de pastillas
                adapterpastilla = PastillaAdapter(pastillasList)
                listPastillas.adapter = adapterpastilla

                progressDialog2.dismiss() // Ocultar el loader cuando se complete la carga
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al obtener datos de la prescripción: ${error.message}")
                Toast.makeText(this, "Error al obtener datos de la prescripción", Toast.LENGTH_SHORT).show()
                progressDialog2.dismiss() // Ocultar el loader cuando se complete la carga
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

    private fun formatearFecha(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date: Date = inputFormat.parse(fecha) ?: Date()
            outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            fecha // Retorna la fecha original en caso de error
        }
    }

    private fun buscarPacientePorDNI() {
        progressDialog.show() // Mostrar el loader antes de hacer la petición
        val dni = dniInput.text.toString().trim() // Obtener el DNI ingresado

        // Validar que el DNI tenga exactamente 8 caracteres
        if (dni.length != 8) {
            Toast.makeText(this, "El DNI debe tener exactamente 8 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://pillpop-backend.onrender.com/obtenerDatosPacientePorDNI"  // Cambia a la URL de tu servidor

        // Crear un objeto JSON para la solicitud
        val jsonObject = JSONObject().apply {
            put("dni", dni)  // Asegúrate de que este campo coincida con lo que espera tu servidor
        }

        // Crear la solicitud POST
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Manejar la respuesta del servidor
                val jsonResponse = JSONObject(response)

                // Verificar si la respuesta contiene el campo "mensaje"
                if (jsonResponse.has("mensaje")) {
                    // Si hay un mensaje, es porque no se encontró el paciente
                    Toast.makeText(this, jsonResponse.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    nombreCompletoInput.setText("") // Limpiar el campo si no se encuentra el paciente
                } else {
                    // Si la respuesta contiene los datos del paciente
                    val nombreCompleto = jsonResponse.getString("nombreCompleto") // Extrae el campo nombreCompleto

                    // Llenar los campos en la vista
                    nombreCompletoInput.setText(nombreCompleto)
                }
                progressDialog.dismiss() // Ocultar el loader cuando se complete la carga
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al buscar paciente: ${error.message}")
                nombreCompletoInput.setText("") // Limpiar el campo en caso de error
                Toast.makeText(this, "Error al buscar paciente", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss() // Ocultar el loader cuando se complete la carga
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
}