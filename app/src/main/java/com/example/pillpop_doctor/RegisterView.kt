package com.example.pillpop_doctor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var progressDialog: ProgressDialog
    private lateinit var progressDialog2: ProgressDialog

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

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        progressDialog2 = ProgressDialog(this)
        progressDialog2.setMessage("Guardando datos...")
        progressDialog2.setCancelable(false) // Evitar que el usuario lo pueda cancelar


        // Cargar los datos desde las rutas usando Volley
        loadGeneros()
        loadEspecialidades()

        btnRegistrar = findViewById(R.id.Registrarse_btn)

        btnRegistrar.setOnClickListener {
            if (validarCampos()) {
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
    }

    private fun validarCampos(): Boolean {
        val nombre = nombreInput.text.toString()
        if (nombre.isEmpty()) {
            nombreInput.error = "El nombre no puede estar vacío"
            return false
        } else if (!nombre.matches(Regex("^[a-zA-Z\\s]+$"))) {
            nombreInput.error = "El nombre solo puede contener letras"
            return false
        }

        val dni = dniInput.text.toString()
        if (dni.isEmpty()) {
            dniInput.error = "El DNI no puede estar vacío"
            return false
        } else if (!dni.matches(Regex("^\\d{8}$"))) {
            dniInput.error = "El DNI debe contener 8 dígitos numéricos"
            return false
        }

        val email = emailInput.text.toString()
        if (email.isEmpty()) {
            emailInput.error = "El correo electrónico no puede estar vacío"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Por favor ingresa un correo electrónico válido"
            return false
        }

        val password = passwordInput.text.toString()
        if (password.isEmpty()) {
            passwordInput.error = "La contraseña no puede estar vacía"
            return false
        } else if (password.length < 6) {
            passwordInput.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        val generoSeleccionado = spinnerGenero.selectedItem.toString()
        if (generoSeleccionado == "Seleccionar...") {
            Toast.makeText(this, "Por favor selecciona tu género", Toast.LENGTH_SHORT).show()
            return false
        }

        val especialidadSeleccionada = spinnerEspecialidad.selectedItem.toString()
        if (especialidadSeleccionada == "Seleccionar...") {
            Toast.makeText(this, "Por favor selecciona tu especialidad", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Función para obtener los géneros usando Volley
    private fun loadGeneros() {
        progressDialog.show() // Mostrar el loader antes de hacer la petición
        val url = "https://pillpop-backend.onrender.com/getDataSexo"  // Reemplaza con la URL correcta

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val generosList = ArrayList<String>()
                    // Agregar la opción "Seleccionar..."
                    generosList.add("Seleccionar...")  // Opción con ID 0
                    generoMap["Seleccionar..."] = 0  // Agregar el ID correspondiente
                    for (i in 0 until response.length()) {
                        val generoObject: JSONObject = response.getJSONObject(i)
                        val id = generoObject.getInt("id")  // Obtener el ID del género
                        val nombre = generoObject.getString("nombre")
                        generosList.add(nombre)
                        generoMap[nombre] = id
                    }
                    // Configuramos el adaptador del Spinner
                    val adapterGeneros = ArrayAdapter(this, R.layout.spinner_item, generosList)
                    // Configuramos el diseño para los ítems desplegables
                    adapterGeneros.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinnerGenero.adapter = adapterGeneros
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    progressDialog.dismiss() // Ocultar el loader cuando se complete la carga
                }
            },
            { error ->
                // Manejo de errores
                error.printStackTrace()
                progressDialog.dismiss() // Ocultar el loader en caso de error
            }
        )

        // Añadimos la petición a la cola de Volley
        requestQueue.add(jsonArrayRequest)
    }

    // Función para obtener las especialidades usando Volley
    private fun loadEspecialidades() {
        progressDialog.show() // Mostrar el loader antes de hacer la petición
        val url = "https://pillpop-backend.onrender.com/getDataEspecialidades"  // Reemplaza con la URL correcta

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val especialidadList = ArrayList<String>()
                    // Agregar la opción "Seleccionar..."
                    especialidadList.add("Seleccionar...")  // Opción con ID 0
                    especialidadMap["Seleccionar..."] = 0  // Agregar el ID correspondiente
                    for (i in 0 until response.length()) {
                        val especialidadObject: JSONObject = response.getJSONObject(i)
                        val id = especialidadObject.getInt("id")
                        val nombre = especialidadObject.getString("nombre")
                        especialidadList.add(nombre)
                        especialidadMap[nombre] = id
                    }
                    // Configuramos el adaptador del Spinner
                    val adapterEspecialidad =
                        ArrayAdapter(this, R.layout.spinner_item, especialidadList)
                    // Configuramos el diseño para los ítems desplegables
                    adapterEspecialidad.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinnerEspecialidad.adapter = adapterEspecialidad
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    progressDialog.dismiss() // Ocultar el loader cuando se complete la carga
                }
            },
            { error ->
                // Manejo de errores
                error.printStackTrace()
                progressDialog.dismiss() // Ocultar el loader en caso de error
            }
        )

        // Añadimos la petición a la cola de Volley
        requestQueue.add(jsonArrayRequest)
    }

    private fun registrarDoctor(nombre: String, idGenero: Int?, idEspecialidad: Int?, dni: String, email: String, password: String) {
        progressDialog2.show() // Mostrar el loader antes de hacer la petición
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
                }finally {
                    progressDialog2.dismiss() // Ocultar el loader cuando se complete la carga
                }
            },
            { error ->
                // Manejar errores
                Log.e("Registro", "Error al registrar el doctor: ${error.message}")
                progressDialog2.dismiss() // Ocultar el loader en caso de error
                Toast.makeText(this, "No se pudo registrar. Intente de nuevo", Toast.LENGTH_SHORT).show()
            }
        )

        // Añadir la petición a la cola de Volley
        requestQueue.add(jsonObjectRequest)
    }

}