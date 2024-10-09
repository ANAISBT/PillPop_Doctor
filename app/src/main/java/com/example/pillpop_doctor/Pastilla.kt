package com.example.pillpop_doctor
import java.io.Serializable

data class Pastilla(
 val pastillla_id: Int,
 val pastilla_nombre: String,
 val cantidad: Int,
 val dosis: Int,
 val Frecuencia: String,
 val fechaInicio: String,
 val hora: String,
 val tiempo: String,
 val observaciones: String
) : Serializable
