package com.example.pillpop_doctor

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.pillpop_doctor.databinding.ActivityDetallePastillaBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetallePastillaView : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePastillaBinding
    private val frecuenciasMap = HashMap<String, Int>() // Mapa para almacenar frecuencias
    private lateinit var btnAceptar: Button
    private lateinit var spinnerFrecuencia: Spinner

    // Declara las variables para tus vistas
    private lateinit var nombrePastillaInput: EditText
    private lateinit var cantidadPastillasInput: EditText
    private lateinit var dosisInput: EditText
    private lateinit var fechaInicioInput: EditText
    private lateinit var horaDosisInput: EditText
    private lateinit var minutosDosisInput: EditText
    private lateinit var observacionesInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePastillaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendario = Calendar.getInstance()
        val fecha = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendario.set(Calendar.YEAR, year)
            calendario.set(Calendar.MONTH, month)
            calendario.set(Calendar.DAY_OF_MONTH, day)

            actualizarFecha(calendario)
        }

        binding.fechaPickBtn.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                fecha,
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = calendario.timeInMillis
            datePickerDialog.show()
        }

        // Configurar el adaptador para el Spinner
        spinnerFrecuencia= findViewById(R.id.frecuenciasDrop)

        // Llamar la función para cargar las frecuencias desde el servidor
        cargarFrecuencias()

        btnAceptar = findViewById(R.id.aceptarButton)
        // Inicializa las vistas
        nombrePastillaInput = findViewById(R.id.nombrePastillaInput)
        cantidadPastillasInput = findViewById(R.id.cantidadPastillasInput)
        dosisInput = findViewById(R.id.dosisInput)
        fechaInicioInput = findViewById(R.id.editTextDate)
        horaDosisInput = findViewById(R.id.editTexthora)
        minutosDosisInput = findViewById(R.id.editTextMinutos)
        observacionesInput = findViewById(R.id.observacionesInput)



        btnAceptar.setOnClickListener {

           /* val frecuenciaSeleccionada = spinnerFrecuencia.selectedItem.toString()

            val idFrecuencia = frecuenciasMap[frecuenciaSeleccionada]
*/
            obtenerDatos()
        }
    }

    private fun obtenerDatos() {
        // Obtén los datos ingresados
        val nombrePastilla = nombrePastillaInput.text.toString()
        val cantidadPastillas = cantidadPastillasInput.text.toString()
        val dosis = dosisInput.text.toString()
        val fechaInicio = fechaInicioInput.text.toString()
        val horaDosis = horaDosisInput.text.toString()
        val minutosDosis = minutosDosisInput.text.toString()
        val observaciones = observacionesInput.text.toString()
        val frecuenciaSeleccionada = spinnerFrecuencia.selectedItem.toString()
        val idFrecuencia = frecuenciasMap[frecuenciaSeleccionada]

        // Aquí puedes hacer algo con los datos, como enviarlos a una base de datos o mostrarlos
        Log.d("DetallePrescripcion", "Nombre Pastilla: $nombrePastilla, Cantidad: $cantidadPastillas, Dosis: $dosis, Fecha: $fechaInicio, Hora: $horaDosis, Minutos: $minutosDosis, Observaciones: $observaciones")
    }

    private fun actualizarFecha(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDate.setText(formatoSimple.format(calendar.time))
    }

    // Función para cargar frecuencias usando Volley
    private fun cargarFrecuencias() {
        // Crear una cola de solicitudes de Volley
        val queue = Volley.newRequestQueue(this)

        // URL del endpoint de frecuencias
        val url = "https://pillpop-backend.onrender.com/getDataFrecuencias"

        // Crear una solicitud de JsonArray con Volley
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response: JSONArray ->
                try {
                    val frecuenciasList = ArrayList<String>()

                    // Extraer el campo "tipo" de cada objeto en el JSONArray
                    for (i in 0 until response.length()) {
                        val frecuencia = response.getJSONObject(i)
                        val tipo = frecuencia.getString("tipo")
                        val id = frecuencia.getInt("id") // Asumiendo que "id" es un entero

                        frecuenciasList.add(tipo) // Solo agregar el tipo a la lista
                        frecuenciasMap[tipo] = id // Almacenar en el HashMap
                    }

                    val adapterFrecuencias = ArrayAdapter(
                        this@DetallePastillaView,
                        android.R.layout.simple_spinner_item,
                        frecuenciasList
                    )
                    adapterFrecuencias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerFrecuencia.adapter = adapterFrecuencias

                } catch (e: Exception) {
                    e.printStackTrace() // Manejar excepción de parsing JSON
                }
            },
            { error ->
                error.printStackTrace() // Manejar el error de la solicitud
            }
        )

        // Agregar la solicitud a la cola de Volley
        queue.add(jsonArrayRequest)
    }

}
