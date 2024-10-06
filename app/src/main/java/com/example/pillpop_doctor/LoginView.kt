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

        btnLogin.setOnClickListener {
            /*val dni = edtDni.text.toString()
            val contrasena = edtContrasena.text.toString()
            LoginTask(dni, contrasena) { perfilId ->
                if (perfilId != null) {
                    // Login exitoso, redirigir al PrincipalView
                    val intent = Intent(this, BienvenidoView::class.java)
                    intent.putExtra("perfil_id", perfilId)
                    startActivity(intent)
                } else {
                    // Mostrar mensaje de error al usuario
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }.execute()*/
            val intent = Intent(this, BienvenidoView::class.java)
            startActivity(intent)
        }
        btnRegistrarUsuario.setOnClickListener{
            val intent = Intent(this, RegisterView::class.java)
            startActivity(intent)
        }
    }

    /*private class LoginTask(
        private val dni: String,
        private val contrasena: String,
        private val onLoginComplete: (Int?) -> Unit
    ) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val urlString = "http://192.168.56.1/PillPop/API/Doctor/LoginUsuarioDoctor.php"
            var result = ""

            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.doOutput = true

                // Enviar datos
                val jsonInputString = """{"dni": $dni, "contrasena": "$contrasena"}"""
                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray()
                    os.write(input, 0, input.size)
                }

                // Leer la respuesta
                result = connection.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e("LoginTask", "Error", e)
            }

            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            try {
                val jsonResponse = JSONObject(result)
                if (jsonResponse.has("id")) {
                    val perfilId = jsonResponse.getInt("id")
                    onLoginComplete(perfilId)
                } else {
                    // Manejar el error, por ejemplo, mostrar un mensaje
                    val error = jsonResponse.getString("mensaje")
                    Log.e("LoginTask", "Login error: $error")
                    onLoginComplete(null)
                }
            } catch (e: Exception) {
                Log.e("LoginTask", "Error parsing JSON", e)
                onLoginComplete(null)
            }
        }
    }*/
}