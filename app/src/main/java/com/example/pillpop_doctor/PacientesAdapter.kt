package com.example.pillpop_doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PacientesAdapter (private val pacientesList: List<Paciente>) : RecyclerView.Adapter<PacientesAdapter.PacientesViewHolder>(),
    Filterable {

    private var filteredPacientesList: List<Paciente> = pacientesList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacientesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pacientes, parent, false)
        return PacientesViewHolder(view)
    }

    override fun onBindViewHolder(holder: PacientesViewHolder, position: Int) {
        val paciente = filteredPacientesList[position]
        holder.bind(paciente)
    }

    override fun getItemCount(): Int {
        return filteredPacientesList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) {
                    pacientesList
                } else {
                    pacientesList.filter {
                        it.DNI_numero.contains(query)
                    }
                }
                val results = FilterResults()
                results.values = filtered
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPacientesList = results?.values as List<Paciente>
                notifyDataSetChanged()
            }
        }
    }
    class PacientesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pillImage: ImageView = itemView.findViewById(R.id.pillIcon)
        var nombrePaciente: TextView = itemView.findViewById(R.id.nombrePacienteReporte)
        var dniText: TextView = itemView.findViewById(R.id.DNInumeroReporte)
        val cardView: CardView = itemView.findViewById(R.id.cardPaciente)

        fun bind(paciente: Paciente) {
            pillImage.setImageResource(R.drawable.pill)
            nombrePaciente.text = paciente.paciente_nombre
            dniText.text = paciente.DNI_numero
        }
    }

}