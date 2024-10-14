package com.example.pillpop_doctor

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.pillpop_doctor.databinding.ActivityDetallePastillaBinding
import com.example.pillpop_doctor.databinding.FragmentProgresoBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProgresoFragment : Fragment() {
    private lateinit var buscadorDNI: SearchView
    private lateinit var binding: FragmentProgresoBinding
    private lateinit var listPacientes: RecyclerView
    private lateinit var adapter: PacientesAdapter
    private lateinit var requestQueue: RequestQueue
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProgresoBinding.inflate(inflater, container, false)
        val view = binding.root

        requestQueue = Volley.newRequestQueue(requireContext())

        // Inicializar el ProgressDialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando pacientes...")
        progressDialog.setCancelable(false)

        val spinnerFrecuenciaTiempo: Spinner = view.findViewById(R.id.FrecuenciaReporteDrop)
        val frecuenciaTiempoList = resources.getStringArray(R.array.frecuencia_reporte_array)
        val adapterFrecuenciaTiempo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frecuenciaTiempoList)
        spinnerFrecuenciaTiempo.adapter = adapterFrecuenciaTiempo

        val linearEntreFechas: LinearLayout = view.findViewById(R.id.LinearEntreFechas)
        val linearFechaUnica: LinearLayout = view.findViewById(R.id.LinearfechaUnica)

        // Establecer el listener para el Spinner
        spinnerFrecuenciaTiempo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                when (selectedItem) {
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

        val calendarioInicio = Calendar.getInstance()
        val fechaInicio = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendarioInicio.set(Calendar.YEAR, year)
            calendarioInicio.set(Calendar.MONTH, month)
            calendarioInicio.set(Calendar.DAY_OF_MONTH, day)

            actualizarFechaInicio(calendarioInicio)
        }

        binding.fechaPickBtnInicio.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                fechaInicio,
                calendarioInicio.get(Calendar.YEAR),
                calendarioInicio.get(Calendar.MONTH),
                calendarioInicio.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = calendarioInicio.timeInMillis
            datePickerDialog.show()
        }

        val calendarioFin = Calendar.getInstance()
        val fechaFin = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendarioFin.set(Calendar.YEAR, year)
            calendarioFin.set(Calendar.MONTH, month)
            calendarioFin.set(Calendar.DAY_OF_MONTH, day)

            actualizarFechaFin(calendarioFin)
        }

        binding.fechaPickBtnFin.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                fechaFin,
                calendarioFin.get(Calendar.YEAR),
                calendarioFin.get(Calendar.MONTH),
                calendarioFin.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = calendarioInicio.timeInMillis
            datePickerDialog.show()
        }

        // Initialize the RecyclerView
        listPacientes = view.findViewById(R.id.ListPacientes)
        listPacientes.layoutManager = LinearLayoutManager(context)

        // Cargar la lista de pacientes
        cargarPacientes()

        // Initialize the SearchView
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

        return view
    }

    private fun cargarPacientes() {
        progressDialog.show()
        val url = "https://pillpop-backend.onrender.com/ObtenerPacientesPorDoctor" // Cambia esto por tu URL

        // Crear el objeto JSON que se enviará como cuerpo de la solicitud
        val params = JSONObject()
        params.put("doctor_id", doctorId)

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
                    val nombre = jsonObject.getString("nombrePaciente") // Cambiado a "nombrePaciente"
                    val dni = jsonObject.getString("dniPaciente") // Cambiado a "dniPaciente"
                    listaPacientes.add(Paciente(id, nombre, dni))
                }

                // Inicializa el adaptador con los datos obtenidos
                adapter = PacientesAdapter(listaPacientes)
                listPacientes.adapter = adapter
                progressDialog.dismiss()
            },
            { error: VolleyError ->
                // Maneja el error aquí
                error.printStackTrace()
                progressDialog.dismiss()
            }
        )

        // Agregar la solicitud a la cola
        requestQueue.add(jsonObjectRequest)
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProgresoFragment().apply {
                arguments = Bundle().apply {
                    // Aquí puedes agregar los parámetros si es necesario
                }
            }
    }
}





