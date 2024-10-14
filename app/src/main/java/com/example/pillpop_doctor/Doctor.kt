package com.example.pillpop_doctor

data class Doctor(
    val id: Int,
    val nombreCompleto: String,
    val sexo_id: Int,
    val especialidad_id: Int,
    val dni: Int,
    val correoElectronico: String,
    val especialidad: String
)
