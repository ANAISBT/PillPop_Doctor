package com.example.pillpop_doctor

import android.annotation.SuppressLint
import android.app.AlertDialog
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
    private lateinit var progressDialog3: ProgressDialog
    private val pastillasList: MutableList<Pastilla> = mutableListOf()
    private lateinit var CancelarButton: Button
    private lateinit var editarPrescripcionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_prescripcion)

        val prescripcionId = intent.getStringExtra("PRESCRIPCION_ID") // Recupera el valor

        listPastillas = findViewById(R.id.ListPastillas)
        listPastillas.layoutManager = LinearLayoutManager(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Buscando Paciente...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        progressDialog2 = ProgressDialog(this)
        progressDialog2.setMessage("Cargando datos...")
        progressDialog2.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        progressDialog3 = ProgressDialog(this)
        progressDialog3.setMessage("Editando...")
        progressDialog3.setCancelable(false) // Evitar que el usuario lo pueda cancelar
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

        CancelarButton = findViewById(R.id.CancelarBtn)
        CancelarButton.setOnClickListener {
            // Cerrar la actividad y regresar a la anterior
            finish()
        }

        // Find the button by ID
        val anadirPastillaButton: ImageButton = findViewById(R.id.AnadirPastilla)

        // Set OnClickListener to navigate to DetallePastillavIEW
        anadirPastillaButton.setOnClickListener {
            // Create an Intent to navigate to DetallePastillavIEW
            val intent = Intent(this, DetallePastillaView::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_PASTILLA)
        }

        editarPrescripcionButton = findViewById(R.id.editarPrescripcionButton)
        editarPrescripcionButton.setOnClickListener {
            if (prescripcionId != null) {
                editarPrescripcion(prescripcionId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DetallePrescripcionView.REQUEST_CODE_ADD_PASTILLA && resultCode == RESULT_OK) {
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



    // Método para obtener y enviar los datos
    private fun editarPrescripcion(id:String) {
        progressDialog3.show() // Mostrar el loader antes de hacer la petición
        // Obtén los datos ingresados
        val dni = dniInput.text.toString()
        val nombreCompleto = nombreCompletoInput.text.toString()
        val diagnostico = diagnosticoInput.text.toString()

        // Validación adicional
        if (dni.isEmpty() && nombreCompleto.isEmpty()) {
            Toast.makeText(this, "Por favor busque al paciente", Toast.LENGTH_SHORT).show()
            progressDialog3.dismiss()
            return
        }

        // Validar que al menos se haya agregado una pastilla
        if (pastillasList.isEmpty()) {
            Toast.makeText(this, "Debes agregar al menos una pastilla", Toast.LENGTH_SHORT).show()
            progressDialog3.dismiss()
            return // Detener la ejecución si no hay pastillas
        }

        val prescripcionId = id.toInt() // O el ID de la prescripción que estás editando
        val jsonObject = JSONObject().apply {
            put("p_dni", dni)
            put("p_prescripcion_id", prescripcionId)
            put("p_diagnostico", diagnostico)
            put("p_fecha", obtenerFechaActual()) // Asegúrate de que `fecha` tenga el formato adecuado para el backend
        }

        val url = "https://pillpop-backend.onrender.com/editarPrescripcion" // Cambia la URL si es necesario

        // Crear la solicitud POST
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Manejar la respuesta del servidor
                val jsonResponse = JSONObject(response)
                val mensaje = jsonResponse.getString("mensaje")

                // Mostrar el mensaje
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                // Llamar al método para insertar las pastillas
                insertarPastillas(prescripcionId)
                progressDialog3.dismiss()
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al editar la prescripción: ${error.message}")
                Toast.makeText(this, "Error al editar la prescripción. Intente nuevamente", Toast.LENGTH_SHORT).show()
                progressDialog3.dismiss()
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

        val dni = dniInput.text.toString().trim() // Obtener el DNI ingresado

        // Validar que el DNI tenga exactamente 8 caracteres
        if (dni.length != 8) {
            Toast.makeText(this, "El DNI debe tener exactamente 8 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        progressDialog.show() // Mostrar el loader antes de hacer la petición
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

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}