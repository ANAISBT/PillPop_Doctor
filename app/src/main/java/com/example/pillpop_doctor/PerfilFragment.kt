package com.example.pillpop_doctor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
class PerfilFragment : Fragment() {
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileOccupation: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var editarPerfilButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicializar el ProgressDialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false)

        // Inicializa las vistas
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileOccupation = view.findViewById(R.id.tvProfileOccupation)
        editarPerfilButton = view.findViewById(R.id.editarButton)

        // Llama a la función para obtener los datos del doctor
        obtenerDatosDoctor()

        // Establecer el OnClickListener para el botón de editar perfil
        editarPerfilButton.setOnClickListener {
            val intent = Intent(requireContext(), EditarPerfilView::class.java)
            startActivityForResult(intent, EDITAR_PERFIL_REQUEST_CODE) // Cambia a startActivityForResult
        }

        // Inicializa las vistas para cambiar contraseña
        val editContrasenaView: ImageView = view.findViewById(R.id.EditContrasenaView)
        val editContrasenaViewText: TextView = view.findViewById(R.id.EditContrasenaViewText)

        val cambiarContrasenaIntent = Intent(requireContext(), CambiarComtraseñaView::class.java)

        editContrasenaView.setOnClickListener {
            startActivity(cambiarContrasenaIntent)
        }

        editContrasenaViewText.setOnClickListener {
            startActivity(cambiarContrasenaIntent)
        }

        return view
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
                tvProfileName.text = doctor.nombreCompleto
                tvProfileOccupation.text = doctor.especialidad
                progressDialog.dismiss()
            },
            { error ->
                // Manejar el error
                Log.e("PerfilFragment", "Error: ${error.message}")
                progressDialog.dismiss()
            }
        )

        // Agregar la solicitud a la cola de Volley
        Volley.newRequestQueue(requireContext()).add(jsonRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDITAR_PERFIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Llama a la función para obtener los datos del doctor nuevamente
            obtenerDatosDoctor()
        }
    }

    companion object {
        private const val EDITAR_PERFIL_REQUEST_CODE = 1 // Define el código de solicitud
    }
}