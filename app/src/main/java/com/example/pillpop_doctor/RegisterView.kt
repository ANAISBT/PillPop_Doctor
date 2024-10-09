package com.example.pillpop_doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class RegisterView: AppCompatActivity() {
    private lateinit var btnRegistrar: Button
    private lateinit var spinnerGenero: Spinner
    private lateinit var spinnerEspecialidad: Spinner
    private lateinit var requestQueue: RequestQueue

    private val generoMap = HashMap<String, Int>()  // Mapa para almacenar nombres de géneros y sus IDs
    private val especialidadMap = HashMap<String, Int>()  // Mapa para especialidades
    private lateinit var nombreInput: EditText
    private lateinit var dniInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    var idDoctor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_view)

        spinnerGenero = findViewById(R.id.generosDrop)
        spinnerEspecialidad = findViewById(R.id.EspecialidadDrop)
        nombreInput = findViewById(R.id.NombreInput)
        dniInput = findViewById(R.id.DNIInput)
        emailInput = findViewById(R.id.EmailAddressInput)
        passwordInput = findViewById(R.id.editTextTextPassword)

        // Inicializamos la requestQueue de Volley
        requestQueue = Volley.newRequestQueue(this)

        // Cargar los datos desde las rutas usando Volley
        loadGeneros()
        loadEspecialidades()

        btnRegistrar = findViewById(R.id.Registrarse_btn)

        btnRegistrar.setOnClickListener {

            // Obtén el valor seleccionado en el Spinner de Género
            val generoSeleccionado = spinnerGenero.selectedItem.toString()

            // Obtén el valor seleccionado en el Spinner de Especialidad
            val especialidadSeleccionada = spinnerEspecialidad.selectedItem.toString()

            // Obtener el ID del género y especialidad seleccionados
            val idGenero = generoMap[generoSeleccionado]
            val idEspecialidad = especialidadMap[especialidadSeleccionada]

            val nombre = nombreInput.text.toString()
            val dni = dniInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            registrarDoctor(nombre, idGenero, idEspecialidad, dni, email, password)
        }
    }
    // Función para obtener los géneros usando Volley
    private fun loadGeneros() {
        val url = "https://pillpop-backend.onrender.com/getDataSexo"  // Reemplaza con la URL correcta

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val generosList = ArrayList<String>()
                    for (i in 0 until response.length()) {
                        val generoObject: JSONObject = response.getJSONObject(i)
                        val id = generoObject.getInt("id")  // Obtener el ID del género
                        val nombre = generoObject.getString("nombre")
                        generosList.add(nombre)
                        generoMap[nombre] = id
                    }
                    // Configuramos el adaptador del Spinner
                    val adapterGeneros = ArrayAdapter(this, android.R.layout.simple_spinner_item, generosList)
                    spinnerGenero.adapter = adapterGeneros
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Manejo de errores
                error.printStackTrace()
            }
        )

        // Añadimos la petición a la cola de Volley
        requestQueue.add(jsonArrayRequest)
    }

    // Función para obtener las especialidades usando Volley
    private fun loadEspecialidades() {
        val url = "https://pillpop-backend.onrender.com/getDataEspecialidades"  // Reemplaza con la URL correcta

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val especialidadList = ArrayList<String>()
                    for (i in 0 until response.length()) {
                        val especialidadObject: JSONObject = response.getJSONObject(i)
                        val id = especialidadObject.getInt("id")
                        val nombre = especialidadObject.getString("nombre")
                        especialidadList.add(nombre)
                        especialidadMap[nombre] = id
                    }
                    // Configuramos el adaptador del Spinner
                    val adapterEspecialidad =
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, especialidadList)
                    spinnerEspecialidad.adapter = adapterEspecialidad
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Manejo de errores
                error.printStackTrace()
            }
        )

        // Añadimos la petición a la cola de Volley
        requestQueue.add(jsonArrayRequest)
    }

    private fun registrarDoctor(nombre: String, idGenero: Int?, idEspecialidad: Int?, dni: String, email: String, password: String) {
        val url = "https://pillpop-backend.onrender.com/insertarDoctor"  // Reemplaza con la URL correcta

        // Crea un objeto JSON con los datos del doctor
        val jsonObject = JSONObject()
        try {
            jsonObject.put("nombreCompleto", nombre)
            jsonObject.put("sexo_id", idGenero)
            jsonObject.put("especialidad_id", idEspecialidad)
            jsonObject.put("dni", dni)
            jsonObject.put("correoElectronico", email)
            jsonObject.put("contrasena", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Crear una petición POST con Volley
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                // Manejar la respuesta exitosa
                try {
                    val mensaje = response.getString("mensaje")
                    doctorId = response.getInt("idUsuarioDoctor")
                    Log.d("Registro", "$mensaje con ID: $idDoctor")
                    // Redirigir al usuario a la vista de bienvenida
                    val intent = Intent(this, BienvenidoView::class.java)
                    startActivity(intent)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Manejar errores
                Log.e("Registro", "Error al registrar el doctor: ${error.message}")
            }
        )

        // Añadir la petición a la cola de Volley
        requestQueue.add(jsonObjectRequest)
    }

}