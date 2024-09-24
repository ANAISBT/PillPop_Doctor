package com.example.pillpop_doctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView

class PrescripcionesAdapter (private val prescripcionesList: List<Prescripcion>) : RecyclerView.Adapter<PrescripcionesAdapter.PrescripcionViewHolder>(),Filterable {

    private var filteredPrescripcionesList: List<Prescripcion> = prescripcionesList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescripcionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prescripciones, parent, false)
        return PrescripcionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrescripcionViewHolder, position: Int) {
        val prescripcion = filteredPrescripcionesList[position]
        holder.bind(prescripcion)
    }

    override fun getItemCount(): Int {
        return filteredPrescripcionesList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) {
                    prescripcionesList
                } else {
                    prescripcionesList.filter {
                        it.DNI_numero.contains(query)
                    }
                }
                val results = FilterResults()
                results.values = filtered
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPrescripcionesList = results?.values as List<Prescripcion>
                notifyDataSetChanged()
            }
        }
    }
    class PrescripcionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pillImage: ImageView = itemView.findViewById(R.id.pillIcon)
        var nombrePaciente: TextView = itemView.findViewById(R.id.nombrePaciente)
        var dniText: TextView = itemView.findViewById(R.id.DNInumero)
        var fecha: TextView = itemView.findViewById(R.id.Fecha)
        val cardView: CardView = itemView.findViewById(R.id.cardPrescripcion)

        fun bind(prescripcion: Prescripcion) {
            pillImage.setImageResource(R.drawable.pill)
            nombrePaciente.text = prescripcion.paciente_nombre
            dniText.text = prescripcion.DNI_numero
            fecha.text = prescripcion.fechaIngreso
        }
    }

}