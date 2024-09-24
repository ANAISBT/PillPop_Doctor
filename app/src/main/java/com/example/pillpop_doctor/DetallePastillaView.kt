package com.example.pillpop_doctor

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.pillpop_doctor.databinding.ActivityDetallePastillaBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetallePastillaView : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePastillaBinding

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

        val spinnerFrecuencia : Spinner = findViewById(R.id.frecuenciasDrop)
        val spinnerTiempo : Spinner = findViewById(R.id.tiempoDrop)

        val frecuenciasList= resources.getStringArray(R.array.Frecuencias)
        val tiempoList = resources.getStringArray(R.array.Tiempo)

        val adapterFrecuencias = ArrayAdapter(this,android.R.layout.simple_spinner_item,frecuenciasList)
        val adapterTiempo = ArrayAdapter(this,android.R.layout.simple_spinner_item,tiempoList)

        spinnerFrecuencia.adapter = adapterFrecuencias
        spinnerTiempo.adapter = adapterTiempo
    }

    private fun actualizarFecha(calendar: Calendar) {
        val formatoFecha = "dd/MM/yyyy"
        val formatoSimple = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
        binding.editTextDate.setText(formatoSimple.format(calendar.time))
    }

}
