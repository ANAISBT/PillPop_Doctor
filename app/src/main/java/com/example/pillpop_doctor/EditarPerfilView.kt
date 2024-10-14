package com.example.pillpop_doctor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class EditarPerfilView : AppCompatActivity() {
    private lateinit var btnEditar: Button
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
    private lateinit var CancelarButton: Button

    var idDoctor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_view)

        spinnerGenero = findViewById(R.id.generosEditDrop)
        spinnerEspecialidad = findViewById(R.id.EspecialidadEditDrop)
        nombreInput = findViewById(R.id.nombreCompletoEditInput)
        dniInput = findViewById(R.id.DNIEditInput)
        emailInput = findViewById(R.id.EmailAddressEditInput)

        // Inicializamos la requestQueue de Volley
        requestQueue = Volley.newRequestQueue(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        progressDialog2 = ProgressDialog(this)
        progressDialog2.setMessage("Guardando datos...")
        progressDialog2.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        CancelarButton = findViewById(R.id.CancelarEditBtn)
        CancelarButton.setOnClickListener {
            // Cerrar la actividad y regresar a la anterior
            finish()
        }
        // Cargar los datos desde las rutas usando Volley
        loadGeneros(object : DataLoadListener {
            override fun onDataLoaded() {
                loadEspecialidades(object : DataLoadListener {
                    override fun onDataLoaded() {
                        obtenerDatosDoctor() // Llama a obtenerDatosDoctor después de cargar generos y especialidades
                    }
                })
            }
        })

        btnEditar = findViewById(R.id.EditarPerfilButton)

        btnEditar.setOnClickListener {
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

                editarDoctor(nombre, idGenero, idEspecialidad, dni, email)
            }
        }
    }

    interface DataLoadListener {
        fun onDataLoaded()
    }

    private fun obtenerDatosDoctor() {
        progressDialog.show()
        val url = "https://pillpop-backend.onrender.com/obtenerDatosDoctor" // Cambia esto a tu URL base

        // Crear el objeto JSON para el cuerpo de la solicitud
        val jsonBody = JSONObject()
        jsonBody.put("id", doctorId) // Cambia el ID según sea necesario

        // Crear la solicitud POST
        val jsonRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                // Manejar la respuesta JSON
                val doctor = Doctor(
                    id = response.getInt("id"),
                    nombreCompleto = response.getString("nombreCompleto"),
                    sexo_id = response.getInt("sexo_id"),
                    especialidad_id = response.getInt("especialidad_id"),
                    dni = response.getInt("dni"),
                    correoElectronico = response.getString("correoElectronico"),
                    especialidad = response.getString("Especialidad")
                )

                // Actualizar la UI con los datos del doctor
                nombreInput.setText(doctor.nombreCompleto)
                dniInput.setText(doctor.dni.toString())
                emailInput.setText(doctor.correoElectronico)

                // Establecer el valor del Spinner de Género
                val generoSeleccionado = generoMap.entries.find { it.value == doctor.sexo_id }?.key
                generoSeleccionado?.let {
                    val posicionGenero = (spinnerGenero.adapter as ArrayAdapter<String>).getPosition(it)
                    spinnerGenero.setSelection(posicionGenero)
                }

                // Establecer el valor del Spinner de Especialidad
                val especialidadSeleccionada = especialidadMap.entries.find { it.value == doctor.especialidad_id }?.key
                especialidadSeleccionada?.let {
                    val posicionEspecialidad = (spinnerEspecialidad.adapter as ArrayAdapter<String>).getPosition(it)
                    spinnerEspecialidad.setSelection(posicionEspecialidad)
                }

                progressDialog.dismiss()
            },
            { error ->
                // Manejar el error
                Log.e("PerfilFragment", "Error: ${error.message}")
                progressDialog.dismiss()
            }
        )

        // Agregar la solicitud a la cola de Volley
        requestQueue.add(jsonRequest)
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
    private fun loadGeneros(listener: DataLoadListener) {
        progressDialog.show()
        val url = "https://pillpop-backend.onrender.com/getDataSexo"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val generosList = ArrayList<String>()
                    generosList.add("Seleccionar...")
                    generoMap["Seleccionar..."] = 0
                    for (i in 0 until response.length()) {
                        val generoObject: JSONObject = response.getJSONObject(i)
                        val id = generoObject.getInt("id")
                        val nombre = generoObject.getString("nombre")
                        generosList.add(nombre)
                        generoMap[nombre] = id
                    }
                    val adapterGeneros = ArrayAdapter(this, R.layout.spinner_item, generosList)
                    adapterGeneros.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinnerGenero.adapter = adapterGeneros
                    listener.onDataLoaded() // Notifica que los datos se han cargado
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                }
            },
            { error ->
                error.printStackTrace()
                progressDialog.dismiss()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun loadEspecialidades(listener: DataLoadListener) {
        progressDialog.show()
        val url = "https://pillpop-backend.onrender.com/getDataEspecialidades"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val especialidadList = ArrayList<String>()
                    especialidadList.add("Seleccionar...")
                    especialidadMap["Seleccionar..."] = 0
                    for (i in 0 until response.length()) {
                        val especialidadObject: JSONObject = response.getJSONObject(i)
                        val id = especialidadObject.getInt("id")
                        val nombre = especialidadObject.getString("nombre")
                        especialidadList.add(nombre)
                        especialidadMap[nombre] = id
                    }
                    val adapterEspecialidad = ArrayAdapter(this, R.layout.spinner_item, especialidadList)
                    adapterEspecialidad.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinnerEspecialidad.adapter = adapterEspecialidad
                    listener.onDataLoaded() // Notifica que los datos se han cargado
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                }
            },
            { error ->
                error.printStackTrace()
                progressDialog.dismiss()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }


    private fun editarDoctor(nombre: String, idGenero: Int?, idEspecialidad: Int?, dni: String, email: String) {
        progressDialog2.show() // Mostrar el loader antes de hacer la petición
        val url = "https://pillpop-backend.onrender.com/editarDoctor/$doctorId"  // Asegúrate de que doctorId tenga el ID correcto

        // Crea un objeto JSON con los datos del doctor
        val jsonObject = JSONObject()
        try {
            jsonObject.put("p_nombreCompleto", nombre) // Cambiar nombre de la clave según la API
            jsonObject.put("p_sexo_id", idGenero)      // Cambiar nombre de la clave según la API
            jsonObject.put("p_especialidad_id", idEspecialidad) // Cambiar nombre de la clave según la API
            jsonObject.put("p_dni", dni)                // Cambiar nombre de la clave según la API
            jsonObject.put("p_correoElectronico", email) // Cambiar nombre de la clave según la API
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Crear una petición PUT con Volley
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT, url, jsonObject,
            { response ->
                // Manejar la respuesta exitosa
                try {
                    val mensaje = response.getString("mensaje")
                    Log.d("Registro", "$mensaje con ID: $doctorId")
                    // Cerrar la actividad actual
                    setResult(Activity.RESULT_OK) // Establece el resultado de la actividad
                    finish()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    progressDialog2.dismiss() // Ocultar el loader cuando se complete la carga
                }
            },
            { error ->
                // Manejar errores
                Log.e("Registro", "Error al editar el doctor: ${error.message}")
                progressDialog2.dismiss() // Ocultar el loader en caso de error
                Toast.makeText(this, "No se pudo editar. Intente de nuevo", Toast.LENGTH_SHORT).show()
            }
        )

        // Añadir la petición a la cola de Volley
        requestQueue.add(jsonObjectRequest)
    }


}