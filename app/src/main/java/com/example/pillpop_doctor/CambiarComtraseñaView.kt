package com.example.pillpop_doctor

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CambiarComtraseñaView  : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var requestQueue: RequestQueue
    private lateinit var dniChangeInput: EditText
    private lateinit var contrasenaEditInput: EditText
    private lateinit var CancelarButton: Button
    private lateinit var EditarContrasenaButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_contrasena_view)

        // Inicializamos la requestQueue de Volley
        requestQueue = Volley.newRequestQueue(this)

        // Inicializar el ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false)

        // Obtener referencias a los elementos de la interfaz
        dniChangeInput = findViewById(R.id.DNIChangeInput)
        contrasenaEditInput = findViewById(R.id.ContrasenaEditInput)

        // Llamar a obtenerDatosDoctor para cargar los datos del doctor
        obtenerDatosDoctor()

        CancelarButton = findViewById(R.id.CancelarContrasenaEditBtn)
        CancelarButton.setOnClickListener {
            // Cerrar la actividad y regresar a la anterior
            finish()
        }

        EditarContrasenaButton= findViewById(R.id.EditarContrasenaButton)
        EditarContrasenaButton.setOnClickListener {
            val nuevaContrasena = contrasenaEditInput.text.toString()
            if (nuevaContrasena.length <= 6) {
                // Mostrar un mensaje de error, por ejemplo, usando un Toast
                Toast.makeText(this, "La nueva contraseña debe tener más de 6 caracteres", Toast.LENGTH_SHORT).show()
            } else {
                // Cerrar la actividad y regresar a la anterior
                doctorId?.let { it1 -> editarContrasenaDoctor(it1, nuevaContrasena) }
            }
        }
    }
    private fun editarContrasenaDoctor(id: Int, nuevaContrasena: String) {
        progressDialog.setMessage("Actualizando contraseña...")
        progressDialog.show()

        // La URL de tu endpoint
        val url = "https://pillpop-backend.onrender.com/editarContrasenaDoctor/$id"

        // Crear el objeto JSON para el cuerpo de la solicitud
        val jsonBody = JSONObject()
        jsonBody.put("p_contrasena", nuevaContrasena)

        // Crear la solicitud StringRequest
        val stringRequest = object : StringRequest(
            Method.PUT,
            url,
            { response ->
                // Manejar la respuesta del servidor
                Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                finish()
            },
            { error ->
                // Manejar el error
                Log.e("CambiarContrasenaView", "Error: ${error.message}")
                Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        ) {
            // Convertir el JSON a un arreglo de bytes
            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray()
            }

            // Establecer el tipo de contenido de la solicitud
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        // Agregar la solicitud a la cola de Volley
        requestQueue.add(stringRequest)
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

                // Llenar los campos de entrada con los datos del doctor
                dniChangeInput.setText(doctor.dni.toString()) // Asignar el DNI
                // Aquí puedes llenar otros campos si es necesario
                contrasenaEditInput.setText("") // Puedes dejar el campo de contraseña vacío por ahora

                progressDialog.dismiss()
            },
            { error ->
                // Manejar el error
                Log.e("CambiarContrasenaView", "Error: ${error.message}")
                progressDialog.dismiss()
            }
        )

        // Agregar la solicitud a la cola de Volley
        requestQueue.add(jsonRequest)
    }
}