package com.example.pillpop_doctor

data class DatosReporteResponse(
    val datosReporte: List<Reporte>,
    val tratamiento: List<Tratamiento>,
    val tomasDiarias: List<TomaDiaria>
)
