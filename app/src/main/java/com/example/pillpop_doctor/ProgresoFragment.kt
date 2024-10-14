package com.example.pillpop_doctor

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.pillpop_doctor.databinding.FragmentProgresoBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProgresoFragment : Fragment() {
    private lateinit var buscadorDNI: SearchView
    private lateinit var binding: FragmentProgresoBinding
    private lateinit var listPacientes: RecyclerView
    private lateinit var adapter: PacientesAdapter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var requestQueue: RequestQueue

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
        val adapterFrecuenciaTiempo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frecuenciaTiempoList)
        spinnerFrecuenciaTiempo.adapter = adapterFrecuenciaTiempo

        val linearEntreFechas: LinearLayout = view.findViewById(R.id.LinearEntreFechas)
        val linearFechaUnica: LinearLayout = view.findViewById(R.id.LinearfechaUnica)

        // Establecer el listener para el Spinner
        spinnerFrecuenciaTiempo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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
            datePickerDialog.datePicker.minDate = calendario.timeInMillis
            datePickerDialog.show()
        }

        // Inicializa el RecyclerView
        listPacientes = view.findViewById(R.id.ListPacientes)
        listPacientes.layoutManager = LinearLayoutManager(context)

        // Cargar la lista de pacientes
        cargarPacientes()

        // Inicializar el SearchView
        buscadorDNI = view.findViewById(R.id.searchViewDNIReporte)

        buscadorDNI.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        // Vincular el botón de descarga de PDF
        val descargarBtn = view.findViewById<Button>(R.id.Descargar_btn)

        val tituloText = "Este es el título del documento"
        val descripcionText = "Lorem Ipsum es simplemente texto de muestra de la industria de la impresión y la composición tipográfica..."

        descargarBtn.setOnClickListener {
            abrirSelectorDeArchivos(tituloText, descripcionText)
        }

        return view
    }

    private fun abrirSelectorDeArchivos(tituloText: String, descripcionText: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Archivo.pdf")
        }
        startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREATE_DOCUMENT && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                generarPdf(uri, "Este es el título del documento", "Lorem Ipsum es simplemente texto de muestra de la industria de la impresión y la composición tipográfica...")
            }
        }
    }

    fun generarPdf(uri: Uri, tituloText: String, descripcionText: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titulo = TextPaint()
        val descripcion = TextPaint()

        val paginaInfo = PdfDocument.PageInfo.Builder(816, 1054, 1).create()
        val pagina1 = pdfDocument.startPage(paginaInfo)

        val canvas = pagina1.canvas

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo2)
        val bitmapEscala = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
        canvas.drawBitmap(bitmapEscala, 368f, 20f, paint)

        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        titulo.textSize = 20f
        canvas.drawText(tituloText, 10f, 150f, titulo)

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        descripcion.textSize = 14f

        val arrDescripcion = descripcionText.split("\n")

        var y = 200f
        for (item in arrDescripcion) {
            canvas.drawText(item, 10f, y, descripcion)
            y += 15
        }

        pdfDocument.finishPage(pagina1)

        // Guardar el PDF en el URI proporcionado
        try {
            val outputStream: OutputStream? = requireContext().contentResolver.openOutputStream(uri)
            outputStream?.use {
                pdfDocument.writeTo(it)
                Toast.makeText(requireContext(), "Se creó el PDF correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al crear el PDF", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()
    }

    private fun cargarPacientes() {
        progressDialog.show()
        val url = "https://pillpop-backend.onrender.com/ObtenerPacientesPorDoctor"

        // Crear el objeto JSON que se enviará como cuerpo de la solicitud
        val params = JSONObject().apply {
            put("doctor_id", doctorId)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            params,
            { response ->
                val listaPacientes = mutableListOf<Paciente>()

                // Aquí asumimos que la respuesta es un objeto JSON que contiene un array de pacientes
                val pacientesArray: JSONArray = response.getJSONArray("pacientes")

                for (i in 0 until pacientesArray.length()) {
                    val jsonObject: JSONObject = pacientesArray.getJSONObject(i)
                    val id = jsonObject.getInt("id")
                    val nombre = jsonObject.getString("nombrePaciente")
                    val dni = jsonObject.getString("dniPaciente")
                    listaPacientes.add(Paciente(id, nombre, dni))
                }

                // Inicializa el adaptador con los datos obtenidos
                adapter = PacientesAdapter(listaPacientes)
                listPacientes.adapter = adapter
                progressDialog.dismiss()
            },
            { error: VolleyError ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Error al cargar pacientes", Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Funciones para actualizar campos de fecha
    private fun actualizarFecha(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDateUnico.setText(formatoSimple.format(calendar.time))
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
