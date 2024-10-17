package com.example.pillpop_doctor

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pillpop_doctor.databinding.FragmentProgresoBinding
import com.google.gson.Gson
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ProgresoFragment : Fragment() {
    private lateinit var binding: FragmentProgresoBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var requestQueue: RequestQueue
    private lateinit var buscarPacienteButton: ImageButton
    private lateinit var dniInput: EditText
    private lateinit var nombreCompletoInput: EditText
    private lateinit var editTextDateInicio: EditText
    private lateinit var editTextDateFin: EditText
    private lateinit var editTextDateUnico: EditText
    // Declaración de variables que se pueden leer en todo el fragmento
    private var fechaInicio: String = ""
    private var fechaFin: String = ""
    private var fechaUnica: String = ""
    private var dniPaciente: String = ""
    private var nombreCompleto: String = ""
    private var frecuenciaSeleccionada: String = ""
    private lateinit var datosReporte: DatosReporteResponse



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProgresoBinding.inflate(inflater, container, false)
        val view = binding.root
        requestQueue = Volley.newRequestQueue(requireContext())

        // Inicializar el ProgressDialog
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Cargando pacientes...")
            setCancelable(false)
        }

        val spinnerFrecuenciaTiempo: Spinner = view.findViewById(R.id.FrecuenciaReporteDrop)
        val frecuenciaTiempoList = resources.getStringArray(R.array.frecuencia_reporte_array)
        val adapterFrecuenciaTiempo = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            frecuenciaTiempoList
        )
        adapterFrecuenciaTiempo.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerFrecuenciaTiempo.adapter = adapterFrecuenciaTiempo

        val linearEntreFechas: LinearLayout = view.findViewById(R.id.LinearEntreFechas)
        val linearFechaUnica: LinearLayout = view.findViewById(R.id.LinearfechaUnica)

        // Establecer el listener para el Spinner
        spinnerFrecuenciaTiempo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (parent.getItemAtPosition(position).toString()) {
                        "Seleccionar..." -> {
                            linearEntreFechas.visibility = View.GONE
                            linearFechaUnica.visibility = View.GONE
                        }

                        "Diario" -> {
                            linearEntreFechas.visibility = View.GONE
                            linearFechaUnica.visibility = View.VISIBLE
                        }

                        "Entre Fechas" -> {
                            linearEntreFechas.visibility = View.VISIBLE
                            linearFechaUnica.visibility = View.GONE
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No hacer nada
                }
            }

        val calendario = Calendar.getInstance()
        val fecha = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendario.set(Calendar.YEAR, year)
            calendario.set(Calendar.MONTH, month)
            calendario.set(Calendar.DAY_OF_MONTH, day)
            actualizarFecha(calendario)
        }

        binding.fechaPickBtn.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                fecha,
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            )
            //datePickerDialog.datePicker.minDate = calendario.timeInMillis
            datePickerDialog.datePicker.maxDate = calendario.timeInMillis
            datePickerDialog.show()
        }

        val calendarioInicio = Calendar.getInstance()
        val fechaInicio2 = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendarioInicio.set(Calendar.YEAR, year)
            calendarioInicio.set(Calendar.MONTH, month)
            calendarioInicio.set(Calendar.DAY_OF_MONTH, day)

            actualizarFechaInicio(calendarioInicio)
        }

        binding.fechaPickBtnInicio.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                fechaInicio2,
                calendarioInicio.get(Calendar.YEAR),
                calendarioInicio.get(Calendar.MONTH),
                calendarioInicio.get(Calendar.DAY_OF_MONTH)
            )
            //datePickerDialog.datePicker.minDate = calendarioInicio.timeInMillis
            datePickerDialog.datePicker.maxDate = calendario.timeInMillis
            datePickerDialog.show()
        }

        val calendarioFin = Calendar.getInstance()
        val fechaFin2 = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendarioFin.set(Calendar.YEAR, year)
            calendarioFin.set(Calendar.MONTH, month)
            calendarioFin.set(Calendar.DAY_OF_MONTH, day)

            actualizarFechaFin(calendarioFin)
        }

        binding.fechaPickBtnFin.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                fechaFin2,
                calendarioFin.get(Calendar.YEAR),
                calendarioFin.get(Calendar.MONTH),
                calendarioFin.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = calendarioInicio.timeInMillis
            datePickerDialog.datePicker.maxDate = calendario.timeInMillis
            datePickerDialog.show()
        }

        // Inicializa las vistas
        editTextDateInicio = view.findViewById(R.id.editTextDateInicio)
        editTextDateFin = view.findViewById(R.id.editTextDateFin)
        editTextDateUnico = view.findViewById(R.id.editTextDateUnico)
        dniInput = view.findViewById(R.id.DNIInput)
        nombreCompletoInput = view.findViewById(R.id.NombreCompletoInput)

        // Vincular el botón de descarga de PDF
        val descargarBtn = view.findViewById<Button>(R.id.Descargar_btn)

        descargarBtn.setOnClickListener {
            // Ejecutar las validaciones
            if (!validaciones()) return@setOnClickListener  // Salir si hay errores de validación

            // Asumiendo que frecuenciaSeleccionada es una variable que contiene el valor correspondiente
            if (frecuenciaSeleccionada == "Diario") {
                doctorId?.let { it1 ->
                    obtenerDatosReporte(this, fechaUnica, it1, dniPaciente,
                        onSuccess = { reporte ->
                            this.datosReporte = reporte

                            // Aquí puedes manejar los datos obtenidos
                            println("Nombre Completo: ${reporte.datosReporte[0].nombreCompleto}")
                            println("Nombre Mes: ${reporte.datosReporte[0].NombreMes}")

                            // Procesar los tratamientos
                            for (tratamiento in reporte.tratamiento) {
                                println("Tratamiento: ${tratamiento.nombrePastilla}, Dosis: ${tratamiento.totalDosis}, Tipo: ${tratamiento.tipo}")
                            }

                            // Procesar las tomas diarias
                            for (toma in reporte.tomasDiarias) {
                                val fechaFormateada = formatearFecha(toma.fecha_toma)
                                println("Fecha Toma: $fechaFormateada, Nombre: ${toma.nombre}, Toma: ${toma.toma}")
                            }

                            abrirSelectorDeArchivos(reporte)
                        },
                        onError = { errorMessage ->
                            // Manejar el error
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } else if (frecuenciaSeleccionada == "Entre Fechas") {
                // Suponiendo que tienes las variables fechaInicio, fechaFin y otros necesarios
                doctorId?.let { it1 ->
                    obtenerDatosReporteEntreFechas(this, fechaInicio, fechaFin, it1, dniPaciente,
                        onSuccess = { reporte ->
                            this.datosReporte = reporte

                            // Aquí puedes manejar los datos obtenidos
                            // Por ejemplo, procesar datosReporte y tratamientos como antes
                            for (reporteItem in reporte.datosReporte) {
                                println("Nombre Completo: ${reporteItem.nombreCompleto}")
                                println("Nombre Mes: ${reporteItem.NombreMes}")
                            }

                            // Procesar los tratamientos
                            for (tratamiento in reporte.tratamiento) {
                                println("Tratamiento: ${tratamiento.nombrePastilla}, Dosis: ${tratamiento.totalDosis}, Tipo: ${tratamiento.tipo}")
                            }

                            // Procesar las tomas diarias
                            for (toma in reporte.tomasDiarias) {
                                val fechaFormateada = formatearFecha(toma.fecha_toma)
                                println("Fecha Toma: $fechaFormateada, Nombre: ${toma.nombre}, Toma: ${toma.toma}")
                            }

                            abrirSelectorDeArchivos(reporte)
                        }
                    ) { errorMessage ->
                        // Manejar el error
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }


        // Inicializa el botón de búsqueda
        buscarPacienteButton = view.findViewById(R.id.BuscarPaciente)
        buscarPacienteButton.setOnClickListener {
            buscarPacientePorDNI()
        }

        return view
    }

    fun formatearFecha(fechaStr: String): String {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        return try {
            val fecha: Date = formatoEntrada.parse(fechaStr) ?: return ""
            formatoSalida.format(fecha)
        } catch (e: Exception) {
            // Manejo de errores
            e.printStackTrace()
            ""
        }
    }

    private fun validaciones(): Boolean {
        // Obtén las fechas y el DNI
        fechaInicio = editTextDateInicio.text.toString()
        fechaFin = editTextDateFin.text.toString()
        fechaUnica = editTextDateUnico.text.toString()
        dniPaciente = dniInput.text.toString()
        nombreCompleto = nombreCompletoInput.text.toString()

        val spinnerFrecuenciaTiempo: Spinner = binding.FrecuenciaReporteDrop // Asegúrate de tener acceso al spinner
        frecuenciaSeleccionada = spinnerFrecuenciaTiempo.selectedItem.toString()

        // Validaciones
        when {
            frecuenciaSeleccionada == "Seleccionar..." -> {
                Toast.makeText(requireContext(), "Necesita seleccionar un tiempo", Toast.LENGTH_SHORT).show()
                return false // Salimos de la función si no se seleccionó
            }
            frecuenciaSeleccionada == "Diario" && fechaUnica.isEmpty() -> {
                Toast.makeText(requireContext(), "Para frecuencia diaria, la fecha única no puede estar vacía", Toast.LENGTH_SHORT).show()
                return false
            }
            frecuenciaSeleccionada == "Entre Fechas" && (fechaInicio.isEmpty() || fechaFin.isEmpty()) -> {
                Toast.makeText(requireContext(), "Para frecuencia entre fechas, ambas fechas deben ser seleccionadas", Toast.LENGTH_SHORT).show()
                return false
            }
            dniPaciente.isEmpty() || nombreCompleto.isEmpty() -> {
                Toast.makeText(requireContext(), "Hay que seleccionar un paciente", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true // Todas las validaciones fueron exitosas
    }


    private fun buscarPacientePorDNI() {
        progressDialog.show() // Mostrar el loader antes de hacer la petición
        val dni = dniInput.text.toString().trim() // Obtener el DNI ingresado

        // Validar que el DNI tenga exactamente 8 caracteres
        if (dni.length != 8) {
            Toast.makeText(requireContext(), "El DNI debe tener exactamente 8 caracteres", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss() // Ocultar el loader al mostrar el mensaje
            return
        }

        val url = "https://pillpop-backend.onrender.com/obtenerDatosPacientePorDNI"  // Cambia a la URL de tu servidor

        // Crear un objeto JSON para la solicitud
        val jsonObject = JSONObject().apply {
            put("dni", dni)  // Asegúrate de que este campo coincida con lo que espera tu servidor
        }

        // Crear la solicitud POST
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Manejar la respuesta del servidor
                val jsonResponse = JSONObject(response)

                // Verificar si la respuesta contiene el campo "mensaje"
                if (jsonResponse.has("mensaje")) {
                    // Si hay un mensaje, es porque no se encontró el paciente
                    Toast.makeText(requireContext(), jsonResponse.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    nombreCompletoInput.setText("") // Limpiar el campo si no se encuentra el paciente
                } else {
                    // Si la respuesta contiene los datos del paciente
                    val nombreCompleto = jsonResponse.getString("nombreCompleto") // Extrae el campo nombreCompleto

                    // Llenar los campos en la vista
                    nombreCompletoInput.setText(nombreCompleto)
                }
                progressDialog.dismiss() // Ocultar el loader cuando se complete la carga
            },
            Response.ErrorListener { error ->
                Log.e("Error", "Error al buscar paciente: ${error.message}")
                nombreCompletoInput.setText("") // Limpiar el campo en caso de error
                Toast.makeText(requireContext(), "Error al buscar paciente", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss() // Ocultar el loader cuando se complete la carga
            }) {
            override fun getBody(): ByteArray {
                return jsonObject.toString().toByteArray(Charsets.UTF_8)
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Agregar la solicitud a la cola de solicitudes de Volley
        requestQueue.add(stringRequest)
    }

    fun obtenerDatosReporte(
        context: Fragment,
        fechaUnica: String,
        doctorId: Int,
        pacienteDni: String,
        onSuccess: (DatosReporteResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://pillpop-backend.onrender.com/reportefechaunica"

        // Convertir fechaUnica a formato yyyy-MM-dd
        val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fechaConvertida: String
        try {
            val fecha = formatoEntrada.parse(fechaUnica)
            fechaConvertida = formatoSalida.format(fecha)
        } catch (e: Exception) {
            onError("Error al formatear la fecha: ${e.message}")
            return
        }


        // Crear el objeto JSON con los parámetros que se enviarán
        val jsonObject = JSONObject().apply {
            put("fechaUnica", fechaConvertida)
            put("doctorId", doctorId)
            put("pacienteDni", pacienteDni)
        }

        // Crear la cola de solicitudes
        val queue: RequestQueue = Volley.newRequestQueue(context.requireContext()) // Usar requireContext()

        // Crear solicitud JSON a la API
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    // Convertir la respuesta en el modelo de datos
                    val datosReporte = Gson().fromJson(response.toString(), DatosReporteResponse::class.java)
                    onSuccess(datosReporte)
                } catch (e: Exception) {
                    onError("Error al procesar la respuesta: ${e.message}")
                }
            },
            { error ->
                onError("Error en la solicitud: ${error.message}")
            }
        ) {
            // Sobreescribir el método getHeaders para establecer encabezados
            override fun getHeaders(): Map<String, String> {
                return mapOf("Content-Type" to "application/json")
            }
        }

        // Añadir la solicitud a la cola
        queue.add(jsonObjectRequest)
    }

    fun obtenerDatosReporteEntreFechas(
        context: Fragment,
        fechaInicio: String,
        fechaFin: String,
        doctorId: Int,
        pacienteDni: String,
        onSuccess: (DatosReporteResponse) -> Unit, // Cambia el tipo de onSuccess a List<Reporte>
        onError: (String) -> Unit
    ) {
        val url = "https://pillpop-backend.onrender.com/reporteentrefechas"

        // Convertir las fechas a formato yyyy-MM-dd
        val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fechaInicioConvertida: String
        val fechaFinConvertida: String

        try {
            val fechaInicioParsed = formatoEntrada.parse(fechaInicio)
            val fechaFinParsed = formatoEntrada.parse(fechaFin)

            fechaInicioConvertida = formatoSalida.format(fechaInicioParsed)
            fechaFinConvertida = formatoSalida.format(fechaFinParsed)
        } catch (e: Exception) {
            onError("Error al formatear las fechas: ${e.message}")
            return
        }

        // Crear el objeto JSON con los parámetros que se enviarán
        val jsonObject = JSONObject().apply {
            put("fechaInicio", fechaInicioConvertida)
            put("fechaFin", fechaFinConvertida)
            put("doctorId", doctorId)
            put("pacienteDni", pacienteDni)
        }

        // Crear la cola de solicitudes
        val queue: RequestQueue = Volley.newRequestQueue(context.requireContext()) // Usar requireContext()

        // Crear solicitud JSON a la API
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    // Convertir la respuesta en el modelo de datos
                    val datosReporteList = response.getJSONArray("datosReporte")
                    val datosReporte = mutableListOf<Reporte>()

                    for (i in 0 until datosReporteList.length()) {
                        val item = datosReporteList.getJSONObject(i)
                        val nombreMes = "${item.getString("NombreMesInicio")} - ${item.getString("NombreMesFin")}"
                        val reporte = Reporte(
                            nombreCompleto = item.getString("nombreCompleto"),
                            NombreMes = nombreMes,
                            fecha = "" // Puedes ajustar esto según sea necesario
                        )
                        datosReporte.add(reporte)
                    }

                    // Procesar tratamiento
                    val tratamientoList = response.getJSONArray("tratamiento")
                    val tratamiento = mutableListOf<Tratamiento>()

                    for (i in 0 until tratamientoList.length()) {
                        val item = tratamientoList.getJSONObject(i)
                        val tratamientoItem = Tratamiento(
                            id = item.getInt("id"),
                            nombrePastilla = item.getString("nombrePastilla"),
                            totalDosis = item.getString("totalDosis"),
                            tipo = item.getString("tipo")
                        )
                        tratamiento.add(tratamientoItem)
                    }

                    // Procesar tomasDiarias
                    val tomasDiariasList = response.getJSONArray("tomasDiarias")
                    val tomasDiarias = mutableListOf<TomaDiaria>()

                    for (i in 0 until tomasDiariasList.length()) {
                        val item = tomasDiariasList.getJSONObject(i)
                        val tomaDiaria = TomaDiaria(
                            fecha_toma = item.getString("fecha_toma"),
                            toma = item.getInt("toma"),
                            id = item.getInt("id"),
                            nombre = item.getString("nombre")
                        )
                        tomasDiarias.add(tomaDiaria)
                    }

                    // Crear el objeto DatosReporteResponse
                    val datosReporteResponse = DatosReporteResponse(
                        datosReporte = datosReporte,
                        tratamiento = tratamiento,
                        tomasDiarias = tomasDiarias
                    )

                    onSuccess(datosReporteResponse)
                } catch (e: Exception) {
                    onError("Error al procesar la respuesta: ${e.message}")
                }
            },
            { error ->
                onError("Error en la solicitud: ${error.message}")
            }
        ) {
            // Sobreescribir el método getHeaders para establecer encabezados
            override fun getHeaders(): Map<String, String> {
                return mapOf("Content-Type" to "application/json")
            }
        }

        // Añadir la solicitud a la cola
        queue.add(jsonObjectRequest)
    }




    private fun abrirSelectorDeArchivos(datosReporte: DatosReporteResponse) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Reporte de Progreso.pdf")
        }
        startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREATE_DOCUMENT && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                generarPdf(
                    uri,
                    "Reporte de Progreso",
                    "Este documento contiene el seguimiento del tratamiento médico.",
                    datosReporte
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirFechaCorrida(fecha: String): String {
        // Definir el formato de entrada
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Parsear la fecha de entrada
        val date = LocalDate.parse(fecha, inputFormatter)

        // Definir el formato de salida
        val outputFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))

        // Formatear la fecha a la salida deseada
        return date.format(outputFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generarPdf(uri: Uri, tituloText: String, descripcionText: String, datosReporte: DatosReporteResponse) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titulo = TextPaint()
        val subtitulo = TextPaint()
        val descripcion = TextPaint()
        val tablePaint = Paint()
        val tableTextPaint = TextPaint()

        var paginaNumber = 1
        var paginaInfo: PdfDocument.PageInfo
        lateinit var pagina: PdfDocument.Page
        lateinit var canvas: Canvas

        fun startNewPage() {
            paginaInfo = PdfDocument.PageInfo.Builder(816, 1054, paginaNumber).create()
            pagina = pdfDocument.startPage(paginaInfo)
            canvas = pagina.canvas
            canvas.drawColor(Color.WHITE) // Fondo blanco
            paginaNumber++
        }

        startNewPage() // Inicializa la primera página

        // Título
        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        titulo.textSize = 24f
        paint.color = Color.BLACK
        canvas.drawText(tituloText, 10f, 50f, titulo)

        // Línea debajo del título
        paint.strokeWidth = 3f
        canvas.drawLine(10f, 70f, 806f, 70f, paint)

        // Descripción
        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        descripcion.textSize = 16f
        descripcion.color = Color.BLACK

        val arrDescripcion = descripcionText.split("\n")

        var y = 100f
        for (item in arrDescripcion) {
            // Verifica si se necesita una nueva página
            if (y > 1054 - 20) { // Si se pasa de la altura de la página menos un margen
                pdfDocument.finishPage(pagina) // Termina la página actual
                startNewPage() // Crea una nueva página
                y = 100f // Reinicia la posición vertical
            }
            canvas.drawText(item, 10f, y, descripcion)
            y += 20
        }

        // Espacio entre la descripción y la tabla
        y += 20f


        // Títulos y texto normal
        val infoBoldPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
            color = Color.BLACK
        }

        val infoNormalPaint = TextPaint().apply {
            typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            textSize = 16f
            color = Color.BLACK
        }

        // Título
        subtitulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        subtitulo.textSize = 20f

        canvas.drawText("Datos de Reporte:", 10f, y, subtitulo)
        y += 40f

        canvas.drawText("Mes:", 10f, y, infoBoldPaint)
        canvas.drawText("${datosReporte.datosReporte[0].NombreMes}", 100f, y, infoNormalPaint) // Añadiendo el valor en normal
        y += 20f
        canvas.drawText("Doctor:", 10f, y, infoBoldPaint)
        canvas.drawText("${datosReporte.datosReporte[0].nombreCompleto}", 100f, y, infoNormalPaint) // Añadiendo el valor en normal
        y += 20f
        canvas.drawText("Fecha:", 10f, y, infoBoldPaint)
        canvas.drawText("${convertirFechaCorrida(fechaUnica)}", 100f, y, infoNormalPaint) // Añadiendo el valor en normal

        // Espacio entre la información y la tabla
        y += 40f

        // Tabla de Tratamiento
        canvas.drawText("Tratamiento:", 10f, y, subtitulo)
        y += 20f

        // Inicia la tabla
        tablePaint.color = Color.LTGRAY
        canvas.drawRect(10f, y, 806f, y + 20f, tablePaint)

        // Títulos de la tabla
        canvas.drawText("Tratamiento", 20f, y + 15f, tableTextPaint)
        canvas.drawText("Dosis", 200f, y + 15f, tableTextPaint)
        canvas.drawText("Frecuencia", 440f, y + 15f, tableTextPaint)

        y += 40f
        for (tratamiento in datosReporte.tratamiento) {
            if (y > 1054 - 20) {
                pdfDocument.finishPage(pagina)
                startNewPage()
                y = 100f
            }
            drawMultilineText(canvas, tratamiento.nombrePastilla, 20f, y, tableTextPaint, 180f)
            drawMultilineText(canvas, tratamiento.totalDosis, 200f, y, tableTextPaint, 80f)
            drawMultilineText(canvas, tratamiento.tipo, 440f, y, tableTextPaint, 140f)
            y += 50f
        }

        // Espacio antes del Registro Diario
        y += 30f

        // Registro Diario
        canvas.drawText("Registro Diario:", 10f, y, subtitulo)
        y += 20f

        val registros2 = datosReporte.tomasDiarias

// Crear tabla de registro diario
// Extraer nombres de pastillas únicos
        val nombresPastillas = registros2.map { it.nombre }.distinct()

// Crear registroTitulos dinámicamente
        val registroTitulos = arrayOf("Fecha") + nombresPastillas.toTypedArray()

// Imprimir el resultado
        println(registroTitulos.joinToString(", "))

// Crear cuerpo de la tabla
        val cuerpoTabla = mutableListOf<Array<String>>()

/// Formato de entrada
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Establece la zona horaria adecuada
        }

// Formato de salida
        val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault() // Establece la zona horaria local
        }

// Agrupar tomas por fecha
        val tomasAgrupadasPorFecha = registros2.groupBy { it.fecha_toma }

        for ((fecha, tomas) in tomasAgrupadasPorFecha) {
            // Inicializar una fila para la fecha actual
            val fila = Array(nombresPastillas.size + 1) { "" }
            // Parsear la fecha y formatearla
            val parsedDate: Date? = inputFormat.parse(fecha)

            // Asegúrate de que la fecha se haya parseado correctamente
            if (parsedDate != null) {
                fila[0] = outputFormat.format(parsedDate) // Asignar fecha formateada a la primera columna
            } else {
                fila[0] = "Fecha no válida" // Manejo de error si la fecha no se parsea
            }


            // Llenar las columnas de pastillas
            for (toma in tomas) {
                val indice = nombresPastillas.indexOf(toma.nombre) + 1 // +1 para ignorar la columna de fecha
                if (indice > 0) {
                    fila[indice] = if (toma.toma == 1) "✔" else "✖"
                }
            }

            cuerpoTabla.add(fila)
        }

// Dibujar títulos de la tabla
        tablePaint.color = Color.LTGRAY
        canvas.drawRect(10f, y, 806f, y + 20f, tablePaint)

        for (i in registroTitulos.indices) {
            canvas.drawText(registroTitulos[i], 20f + i * 140f, y + 15f, tableTextPaint)
        }

// Espacio para los registros
        y += 30f

        for (registro in cuerpoTabla) {
            for (i in registro.indices) {
                if (y > 1054 - 20) {
                    pdfDocument.finishPage(pagina)
                    startNewPage()
                    y = 100f
                }
                canvas.drawText(registro[i], 20f + i * 140f, y + 20f, tableTextPaint)
            }
            y += 30f
        }

        pdfDocument.finishPage(pagina)


        // Guardar el PDF en el URI proporcionado
        try {
            val outputStream: OutputStream? = requireContext().contentResolver.openOutputStream(uri)
            outputStream?.use {
                pdfDocument.writeTo(it)
                Toast.makeText(requireContext(), "Se creó el PDF correctamente", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al crear el PDF", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()
    }

    fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: TextPaint, maxWidth: Float) {
        val words = text.split(" ")
        var line = ""
        var lineHeight = paint.descent() - paint.ascent()
        var cellY = y

        for (word in words) {
            val testLine = "$line $word".trim()
            val testWidth = paint.measureText(testLine)

            if (testWidth > maxWidth) {
                canvas.drawText(line, x, cellY, paint)
                line = word // Iniciar nueva línea con la palabra actual
                cellY += lineHeight // Aumentar la altura para la próxima línea
            } else {
                line = testLine
            }
        }
        canvas.drawText(line, x, cellY, paint) // Dibuja la última línea
    }

    // Funciones para actualizar campos de fecha
    private fun actualizarFecha(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDateUnico.setText(formatoSimple.format(calendar.time))
    }

    private fun actualizarFechaInicio(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDateInicio.setText(formatoSimple.format(calendar.time))
    }

    private fun actualizarFechaFin(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDateFin.setText(formatoSimple.format(calendar.time))
    }
    companion object {
        private const val REQUEST_CODE_CREATE_DOCUMENT = 1

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProgresoFragment().apply {
                arguments = Bundle().apply {
                    // Aquí puedes agregar los parámetros si es necesario
                }
            }
    }
}
