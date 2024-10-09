package com.example.pillpop_doctor

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtDni = findViewById(R.id.DNIInput)
        edtContrasena = findViewById(R.id.TextPasswordInput)
        btnLogin = findViewById(R.id.IniciarSesionBtn)
        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario)

        btnLogin.setOnClickListener {
            val dni = edtDni.text.toString()
            val contrasena = edtContrasena.text.toString()

            if (dni.isNotEmpty() && contrasena.isNotEmpty()) {
                iniciarSesion(dni, contrasena)
            } else {
                Toast.makeText(this, "Por favor, ingresa DNI y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
        btnRegistrarUsuario.setOnClickListener{
            val intent = Intent(this, RegisterView::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSesion(dni: String, contrasena: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://pillpop-backend.onrender.com/loginDoctor"

        // Crear los datos JSON para enviar en el body
        val jsonBody = JSONObject()
        jsonBody.put("p_dni", dni)
        jsonBody.put("p_contrasena", contrasena)

        // Crear la solicitud POST
        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    val mensaje = response.getString("mensaje")
                    if (mensaje == "Login exitoso") {
                        val doctorId = response.getInt("id")
                        Toast.makeText(this, "Login exitoso, ID: $doctorId", Toast.LENGTH_SHORT).show()

                        // Navegar a la vista de bienvenida o la siguiente pantalla
                        val intent = Intent(this, BienvenidoView::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error en la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                if (error.networkResponse != null) {
                    val statusCode = error.networkResponse.statusCode
                    val errorMsg = String(error.networkResponse.data)
                    Log.e("VolleyError", "Error code: $statusCode, Error message: $errorMsg")
                    Toast.makeText(this, "Error en el servidor: $statusCode", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("VolleyError", "Error: ${error.message}")
                    Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        // Añadir la solicitud a la cola de Volley
        queue.add(request)
    }


}