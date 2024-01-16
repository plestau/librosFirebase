package com.example.firebase

import android.app.DownloadManager.Request
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CompletableFuture

class Utilidades {
    companion object{
        fun obtenerListaLibros(db_ref: DatabaseReference): MutableList<Libro> {
            val lista_libros: MutableList<Libro> = mutableListOf()

            db_ref.child("libreria").child("libro").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista_libros.clear()
                    for (libro in snapshot.children) {
                        val pojo_libro = libro.getValue(Libro::class.java)
                        lista_libros.add(pojo_libro!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("error", error.toString())
                }
            })
            return lista_libros
        }

        fun existeLibro(lista_libros:MutableList<Libro>, nombre:String):Boolean{
            var existe = false
            for (libro in lista_libros){
                if (libro.nombre == nombre){
                    existe = true
                    break
                }
            }
            return existe
        }

        fun escribirLibro(db_ref:DatabaseReference,id:String,nombre:String,descripcion:String, fecha:String, url_firebase:String, rating:RatingBar) =
            db_ref.child("libreria").child("libro").child(id).setValue(Libro(
                id,
                nombre,
                descripcion,
                fecha,
                url_firebase,
                rating.rating.toInt()
            ))

        suspend fun guardarPortada(sto_ref: StorageReference, id:String, imagen: Uri):String{
            lateinit var url_portada_firebase: Uri

            url_portada_firebase=sto_ref.child("libreria").child("portadas").child(id)
                .putFile(imagen).await().storage.downloadUrl.await()

            return url_portada_firebase.toString()
        }

        fun obtenerFechaActual():String{
            val fecha = java.util.Calendar.getInstance().time
            val formato = java.text.SimpleDateFormat("yyyy-MM-dd")
            return formato.format(fecha)
        }

        fun tostadaCorrutina(activity: AppCompatActivity, contexto: Context, texto:String){
            activity.runOnUiThread{
                Toast.makeText(
                    contexto,
                    texto,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun animacion_carga(contexto: Context): CircularProgressDrawable{
            val animacion = CircularProgressDrawable(contexto)
            animacion.strokeWidth = 5f
            animacion.centerRadius = 30f
            animacion.start()
            return animacion
        }

        val transicion = DrawableTransitionOptions.withCrossFade(500)

        fun opcionesGlide(context: Context):RequestOptions{
            val options = RequestOptions()
                .placeholder(animacion_carga(context))
                .fallback(R.drawable.portada_generica)
                .error(R.drawable.error_404)
            return options
        }
    }
}