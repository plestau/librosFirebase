package com.example.firebase

import android.net.Uri
import android.os.Parcelable
import android.widget.RatingBar
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Libro(
    var id : String? = null,
    var nombre: String? = null,
    var descripcion: String? = null,
    var fecha: String? = null,
    var portada: String? = null,
    var rating: Int? = null,
):Parcelable , Comparable<Libro> {
    override fun compareTo(other: Libro): Int {
        return this.nombre!!.compareTo(other.nombre!!)
    }
}