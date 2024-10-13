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

class EditarPrescripcionView : AppCompatActivity() {
    private lateinit var listPastillas: RecyclerView
    private lateinit var adapterpastilla: PastillaAdapter
    private lateinit var dniInput: EditText
    private lateinit var nombreCompletoInput: EditText
    private lateinit var diagnosticoInput: EditText
    private lateinit var buscarPacienteButton: ImageButton
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_prescripcion)

        listPastillas = findViewById(R.id.ListPastillas)
        listPastillas.layoutManager = LinearLayoutManager(this)

        // Inicializar lista de pastillas con datos ajustados
        val pastillasList: List<Pastilla> = listOf(
            Pastilla(
                pastillla_id = 1,
                pastilla_nombre = "Paracetamol",
                cantidad = 50,
                dosis = 2,
                FrecuenciaId = 1,
                Frecuencia = "Cada 8 horas",
                fechaInicio = "21/09/2024",
                hora = "08:00",
                observaciones = "Tomar con alimentos"
            ),
            Pastilla(
                pastillla_id = 2,
                pastilla_nombre = "Ibuprofeno",
                cantidad = 30,
                dosis = 1,
                FrecuenciaId = 2,
                Frecuencia = "Cada 6 horas",
                fechaInicio = "22/09/2024",
                hora = "09:00",
                observaciones = "Evitar tomar en ayunas"
            ),
            Pastilla(
                pastillla_id = 3,
                pastilla_nombre = "Amoxicilina",
                cantidad = 20,
                dosis = 1,
                FrecuenciaId = 3,
                Frecuencia = "Cada 12 horas",
                fechaInicio = "23/09/2024",
                hora = "10:00",
                observaciones = "Completar el tratamiento"
            ),
            Pastilla(
                pastillla_id = 4,
                pastilla_nombre = "Aspirina",
                cantidad = 10,
                dosis = 1,
                FrecuenciaId = 4,
                Frecuencia = "Cada 24 horas",
                fechaInicio = "24/09/2024",
                hora = "08:00",
                observaciones = "Tomar en ayunas"
            )
        )

        // Inicializar el adaptador con la lista
        adapterpastilla = PastillaAdapter(pastillasList)
        listPastillas.adapter = adapterpastilla

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Buscando Paciente...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        // Inicializa el botón de búsqueda
        buscarPacienteButton = findViewById(R.id.BuscarPaciente)
        buscarPacienteButton.setOnClickListener {
            buscarPacientePorDNI()
        }

        // Inicializa las vistas
        dniInput = findViewById(R.id.DNIInput)
        nombreCompletoInput = findViewById(R.id.NombreCompletoInput)
        diagnosticoInput = findViewById(R.id.DiagnosticoInput)
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