package com.example.pillpop_doctor

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
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
    private lateinit var progressDialog: ProgressDialog
    private lateinit var CancelarButton: Button

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

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando datos...")
        progressDialog.setCancelable(false) // Evitar que el usuario lo pueda cancelar

        CancelarButton = findViewById(R.id.CancelarBtn)
        CancelarButton.setOnClickListener {
            // Cerrar la actividad y regresar a la anterior
            finish()
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
            obtenerDatos()
        }
    }

    private fun obtenerDatos() {
        // Obtén los datos ingresados
        val nombrePastilla = nombrePastillaInput.text.toString().trim()
        val cantidadPastillas = cantidadPastillasInput.text.toString().toIntOrNull() ?: 0
        val dosis = dosisInput.text.toString().toIntOrNull() ?: 0
        val fechaInicio = fechaInicioInput.text.toString().trim()
        val horaDosis = horaDosisInput.text.toString().trim()
        val minutosDosis = minutosDosisInput.text.toString().trim()
        val observaciones = observacionesInput.text.toString().trim()
        val frecuenciaSeleccionada = spinnerFrecuencia.selectedItem.toString()
        val idFrecuencia = frecuenciasMap[frecuenciaSeleccionada]

        // Validaciones
        if (nombrePastilla.isEmpty()) {
            nombrePastillaInput.error = "El nombre de la pastilla es obligatorio"
            return
        }
        if (cantidadPastillas <= 0 || cantidadPastillas > 50) {
            cantidadPastillasInput.error = "La cantidad de pastillas debe ser mayor a 0 y no mayor a 50"
            return
        }
        if (dosis <= 0 || cantidadPastillas % dosis != 0 || dosis >= cantidadPastillas) {
            dosisInput.error = "La dosis debe ser mayor a 0, menor que la cantidad de pastillas y un divisor de la cantidad de pastillas"
            return
        }
        if (fechaInicio.isEmpty()) {
            fechaInicioInput.error = "La fecha de inicio es obligatoria"
            return
        }
        if (horaDosis.isEmpty() || horaDosis.toIntOrNull() == null || horaDosis.toInt() !in 0..23) {
            horaDosisInput.error = "La hora de la dosis debe ser un número entre 00 y 23"
            return
        }
        if (minutosDosis.isEmpty() || minutosDosis.toIntOrNull() == null || minutosDosis.toInt() !in 0..59) {
            minutosDosisInput.error = "Los minutos de la dosis deben ser un número entre 00 y 59"
            return
        }
        if (idFrecuencia == 0) {
            Toast.makeText(this, "Seleccione una frecuencia", Toast.LENGTH_SHORT).show()
            return
        }

        // Combinar horaDosis y minutosDosis en un solo campo
        val horaCompleta = "$horaDosis:$minutosDosis"

        // Aquí puedes hacer algo con los datos, como enviarlos a una base de datos o mostrarlos
        Log.d("DetallePrescripcion", "Nombre Pastilla: $nombrePastilla, Cantidad: $cantidadPastillas, Dosis: $dosis, Fecha: $fechaInicio, Hora: $horaDosis, Minutos: $minutosDosis, Observaciones: $observaciones")

        // Crear el objeto Pastilla
        val nuevaPastilla = idFrecuencia?.let {
            Pastilla(
                pastillla_id = 0, // Asignar un id temporal
                pastilla_nombre = nombrePastilla,
                cantidad = cantidadPastillas,
                dosis = dosis,
                FrecuenciaId = it,
                Frecuencia = frecuenciaSeleccionada,
                fechaInicio = fechaInicio,
                hora = horaCompleta,
                observaciones = observaciones
            )
        }

        // Regresar los datos a la actividad DetallePrescripcionView
        val intent = Intent()
        intent.putExtra("nueva_pastilla", nuevaPastilla)
        setResult(RESULT_OK, intent)
        finish()
    }


    private fun actualizarFecha(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDate.setText(formatoSimple.format(calendar.time))
    }

    // Función para cargar frecuencias usando Volley
    private fun cargarFrecuencias() {
        progressDialog.show() // Mostrar el loader antes de hacer la petición
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
                    frecuenciasList.add("Seleccionar...") // Agregar opción predeterminada
                    frecuenciasMap["Seleccionar..."] = 0 // Asignar id 0 a la opción predeterminada

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
                        R.layout.spinner_item,
                        frecuenciasList
                    )
                    adapterFrecuencias.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    spinnerFrecuencia.adapter = adapterFrecuencias
                    progressDialog.dismiss()

                } catch (e: Exception) {
                    e.printStackTrace() // Manejar excepción de parsing JSON
                    progressDialog.dismiss() // Ocultar el loader en caso de error
                }
            },
            { error ->
                error.printStackTrace() // Manejar el error de la solicitud
                progressDialog.dismiss() // Ocultar el loader en caso de error
            }
        )

        // Agregar la solicitud a la cola de Volley
        queue.add(jsonArrayRequest)
    }

}
