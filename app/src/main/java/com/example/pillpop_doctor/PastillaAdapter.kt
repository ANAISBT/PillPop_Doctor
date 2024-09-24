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

class PastillaAdapter(private val pastillasList: List<Pastilla>) : RecyclerView.Adapter<PastillaAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pastillas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pastilla = pastillasList[position]
        holder.bind(pastilla)
    }

    override fun getItemCount(): Int = pastillasList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var nombrePastilla: TextView = itemView.findViewById(R.id.nombrePastilla)
        private var cantidadText: TextView = itemView.findViewById(R.id.cantidadNumero)
        private var dosisText: TextView = itemView.findViewById(R.id.DosisNumero)
        private var frecuenciaText: TextView = itemView.findViewById(R.id.Fecuencia)
        private var fechaInicioText: TextView = itemView.findViewById(R.id.fechaNumero)
        private var horaText: TextView = itemView.findViewById(R.id.horaNumero)
        private var tiempoText: TextView = itemView.findViewById(R.id.Tiempo)
        private var observacionesText: TextView = itemView.findViewById(R.id.ObservacionesText)

        fun bind(pastilla: Pastilla) {
            nombrePastilla.text = pastilla.pastilla_nombre
            cantidadText.text = pastilla.cantidad.toString()
            dosisText.text = pastilla.dosis.toString()
            frecuenciaText.text = pastilla.Frecuencia
            fechaInicioText.text = pastilla.fechaInicio
            horaText.text = pastilla.hora
            tiempoText.text = pastilla.tiempo
            observacionesText.text = pastilla.observaciones
        }
    }
}
