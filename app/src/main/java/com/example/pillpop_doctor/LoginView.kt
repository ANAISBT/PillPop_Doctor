package com.example.pillpop_doctor

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginView : AppCompatActivity() {

    private lateinit var edtDni: EditText
    private lateinit var edtContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistrarUsuario: TextView
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtDni = findViewById(R.id.DNIInput)
        edtContrasena = findViewById(R.id.TextPasswordInput)
        btnLogin = findViewById(R.id.IniciarSesionBtn)
        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        btnLogin.setOnClickListener {
            val dni = edtDni.text.toString()
            val contrasena = edtContrasena.text.toString()

            // Validaciones
            val validationMessage = validateInputs(dni, contrasena)
            if (validationMessage != null) {
                Toast.makeText(this, validationMessage, Toast.LENGTH_SHORT).show()
            } else {
                iniciarSesion(dni, contrasena)
            }
        }
        btnRegistrarUsuario.setOnClickListener{
            val intent = Intent(this, RegisterView::class.java)
            startActivity(intent)
        }
    }

    // Método para validar entradas
    private fun validateInputs(dni: String, contrasena: String): String? {
        if (dni.isEmpty()) {
            return "Por favor, ingresa tu DNI."
        }
        if (contrasena.isEmpty()) {
            return "Por favor, ingresa tu contraseña."
        }
        if (dni.length != 8) { // Suponiendo que el DNI debe tener 8 dígitos
            return "El DNI debe tener 8 caracteres."
        }
        if (contrasena.length < 6) { // Suponiendo que la contraseña debe tener al menos 6 caracteres
            return "La contraseña debe tener al menos 6 caracteres."
        }
        return null // Si todas las validaciones son exitosas
    }

    private fun iniciarSesion(dni: String, contrasena: String) {
        progressDialog.show()
        val queue = Volley.newRequestQueue(this)
        val url = "https://pillpop-backend.onrender.com/loginDoctor"

        // Crear los datos JSON para enviar en el body
        val jsonBody = JSONObject()
        jsonBody.put("dni", dni)
        jsonBody.put("contrasena", contrasena)

        // Crear la solicitud POST
        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    val mensaje = response.getString("mensaje")
                    if (mensaje == "Login exitoso") {
                        doctorId = response.getInt("id")
                        Toast.makeText(this, "Login exitoso, ID: $doctorId", Toast.LENGTH_SHORT).show()

                        // Navegar a la vista de bienvenida o la siguiente pantalla
                        val intent = Intent(this, BienvenidoView::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error en la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }finally {
                    progressDialog.dismiss()
                }
            },
            { error ->
                if (error.networkResponse != null) {
                    val statusCode = error.networkResponse.statusCode
                    val errorMsg = String(error.networkResponse.data)
                    Log.e("VolleyError", "Error code: $statusCode, Error message: $errorMsg")
                    // Manejar diferentes códigos de estado
                    when (statusCode) {
                        401 -> Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                        403 -> Toast.makeText(this, "Acceso prohibido", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(this, "No se pudo iniciar sesión, intente de nuevo", Toast.LENGTH_LONG).show()
                    }
                    progressDialog.dismiss()
                } else {
                    Log.e("VolleyError", "Error: ${error.message}")
                    Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()
                }
            }
        )

        // Añadir la solicitud a la cola de Volley
        queue.add(request)
    }


}