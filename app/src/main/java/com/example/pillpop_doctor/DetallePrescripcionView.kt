package com.example.pillpop_doctor

import android.app.ProgressDialog
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

class DetallePrescripcionView : AppCompatActivity() {

    private lateinit var listPastillas: RecyclerView
    private lateinit var adapterpastilla: PastillaAdapter
    private val pastillasList: MutableList<Pastilla> = mutableListOf()
    private lateinit var agregarPrescripcionButton: Button
    private lateinit var dniInput: EditText
    private lateinit var nombreCompletoInput: EditText
    private lateinit var diagnosticoInput: EditText
    private lateinit var buscarPacienteButton: ImageButton
    private lateinit var progressDialog: ProgressDialog
    private lateinit var CancelarButton: Button

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
        CancelarButton = findViewById(R.id.CancelarBtn)
        CancelarButton.setOnClickListener {
            // Cerrar la actividad y regresar a la anterior
            finish()
        }

        // Inicializa las vistas
        dniInput = findViewById(R.id.DNIInput)
        nombreCompletoInput = findViewById(R.id.NombreCompletoInput)
        diagnosticoInput = findViewById(R.id.DiagnosticoInput)
        agregarPrescripcionButton.setOnClickListener {
            obtenerDatos()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Buscando Paciente...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        // Inicializa el botón de búsqueda
        buscarPacienteButton = findViewById(R.id.BuscarPaciente)
        buscarPacienteButton.setOnClickListener {
            buscarPacientePorDNI()
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

    private fun buscarPacientePorDNI() {
        val dni = dniInput.text.toString().trim() // Obtener el DNI ingresado

        // Validar que el DNI tenga exactamente 8 caracteres
        if (dni.length != 8) {
            Toast.makeText(this, "El DNI debe tener exactamente 8 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Buscando Paciente")
        progressDialog.show() // Mostrar el loader antes de hacer la petición
        val url = "https://pillpop-backend.onrender.com/obtenerDatosPacientePorDNI"  // Cambia a la URL de tu servidor

        // Crear un objeto JSON para la solicitud
        val jsonObject = JSONObject().apply {
            put("dni", dni)  // Asegúrate de que este campo coincida con lo que espera tu servidor
        }

        // Crear la solicitud POST
        val stringRequest = object : StringRequest(Method.POST, url,
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
                Toast.makeText(this, "Error al buscar paciente. Intente nuevamente", Toast.LENGTH_SHORT).show()
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




    private fun obtenerDatos() {
        // Obtén los datos ingresados
        val dni = dniInput.text.toString()
        val nombreCompleto = nombreCompletoInput.text.toString()
        val diagnostico = diagnosticoInput.text.toString()

        // Validación adicional
        if (dni.isEmpty() && nombreCompleto.isEmpty()) {
            Toast.makeText(this, "Por favor busque al paciente", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que al menos se haya agregado una pastilla
        if (pastillasList.isEmpty()) {
            Toast.makeText(this, "Debes agregar al menos una pastilla", Toast.LENGTH_SHORT).show()
            return // Detener la ejecución si no hay pastillas
        }

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

        progressDialog.setMessage("Guardando Prescripción")
        progressDialog.show()
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
                progressDialog.dismiss() // Ocultar el loader
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al agregar prescripción: ${error.message}")
                Toast.makeText(this, "No se pudo agregar la prescripcion, intente de nuevo", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss() // Ocultar el loader
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
                val fechaFormateada = "${fechaPartes[2]}-${fechaPartes[1]}-${fechaPartes[0]} ${pastilla.hora}:00" // Formato "yyyy-MM-dd HH:mm:ss"
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
                    Toast.makeText(this, "No se pudo agregar la prescripcion, intente de nuevo", Toast.LENGTH_SHORT).show()
                    eliminarPrescripcionYPastillas(prescripcionId)
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

    private fun eliminarPrescripcionYPastillas(prescripcionId: Int) {
        val url = "https://pillpop-backend.onrender.com/eliminarPrescripcion"  // Cambia a la URL de tu servidor

        // Crear la solicitud para eliminar la prescripción y las pastillas
        val stringRequest = object : StringRequest(Method.DELETE, url,
            Response.Listener<String> { response ->
                Log.d("Success", "Prescripción y pastillas eliminadas correctamente: $response")
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al eliminar la prescripción y pastillas: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = prescripcionId.toString() // Enviar el ID de la prescripción a eliminar
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        // Agregar la solicitud a la cola de solicitudes de Volley
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}

