package com.example.pillpop_doctor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
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
        holder.bind(prescripcion,
            { prescripcionId ->
                // Handle edit action with the prescripcionId
                // Example: Start an edit activity
                val intent = Intent(holder.itemView.context, EditarPrescripcionView::class.java)
                intent.putExtra("PRESCRIPCION_ID", prescripcionId)
                holder.itemView.context.startActivity(intent)
            },
            { prescripcionId ->
                // Handle delete action with the prescripcionId
                // Example: Show a confirmation dialog or delete it directly
                // deletePrescripcion(prescripcionId)
            }
        )
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
                        it.dni.toString().contains(query)
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
        var editarButton: ImageButton = itemView.findViewById(R.id.editarButton)
        var eliminarButton: ImageButton = itemView.findViewById(R.id.eliminarButton)
        fun bind(prescripcion: Prescripcion, onEditClick: (Int) -> Unit, onDeleteClick: (Int) -> Unit) {
            pillImage.setImageResource(R.drawable.pill)
            nombrePaciente.text = prescripcion.nombreCompleto
            dniText.text = prescripcion.dni.toString()
            fecha.text = prescripcion.fecha

            // Set click listener for the edit button
            editarButton.setOnClickListener {
                onEditClick(prescripcion.prescripcionId)
            }

            // Set click listener for the delete button
            eliminarButton.setOnClickListener {
                onDeleteClick(prescripcion.prescripcionId)
            }
        }
    }

}