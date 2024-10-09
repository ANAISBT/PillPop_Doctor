package com.example.pillpop_doctor

import android.os.Parcel
import android.os.Parcelable

data class Pastilla(
 val pastillla_id: Int,
 val pastilla_nombre: String,
 val cantidad: Int,
 val dosis: Int,
 val FrecuenciaId:Int,
 val Frecuencia: String,
 val fechaInicio: String,
 val hora: String,
 val observaciones: String
) : Parcelable {
 constructor(parcel: Parcel) : this(
  parcel.readInt(),
  parcel.readString() ?: "",
  parcel.readInt(),
  parcel.readInt(),
  parcel.readInt(),
  parcel.readString() ?: "",
  parcel.readString() ?: "",
  parcel.readString() ?: "",
  parcel.readString() ?: ""
 )

 override fun writeToParcel(parcel: Parcel, flags: Int) {
  parcel.writeInt(pastillla_id)
  parcel.writeString(pastilla_nombre)
  parcel.writeInt(cantidad)
  parcel.writeInt(dosis)
  parcel.writeInt(FrecuenciaId)
  parcel.writeString(Frecuencia)
  parcel.writeString(fechaInicio)
  parcel.writeString(hora)
  parcel.writeString(observaciones)
 }

 override fun describeContents(): Int {
  return 0
 }

 companion object CREATOR : Parcelable.Creator<Pastilla> {
  override fun createFromParcel(parcel: Parcel): Pastilla {
   return Pastilla(parcel)
  }

  override fun newArray(size: Int): Array<Pastilla?> {
   return arrayOfNulls(size)
  }
 }
}
