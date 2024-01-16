package com.example.firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EditarLibro : AppCompatActivity(), CoroutineScope {

    private lateinit var nombre : EditText
    private lateinit var descripcion : EditText
    private lateinit var portada: ImageView
    private lateinit var fecha: EditText
    private lateinit var rating: RatingBar
    private lateinit var modificar: Button
    private lateinit var volver: Button

    private var url_portada: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var pojo_libros: Libro
    private lateinit var lista_libros: MutableList<Libro>
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_libro)

        val this_activity = this
        job = Job()

        pojo_libros = intent.getParcelableExtra<Libro>("libro")!!

        nombre = findViewById(R.id.nombre)
        descripcion = findViewById(R.id.descripcion)
        portada = findViewById(R.id.portada)
        fecha = findViewById(R.id.fecha)
        rating = findViewById(R.id.rating)
        modificar = findViewById(R.id.modificar)
        volver = findViewById(R.id.volver)

        nombre.setText(pojo_libros.nombre)
        descripcion.setText(pojo_libros.descripcion)
        fecha.setText(pojo_libros.fecha)
        rating.rating = pojo_libros.rating?.toFloat() ?: 0.0f

        Glide.with(applicationContext)
            .load(pojo_libros.portada)
            .apply(Utilidades.opcionesGlide(applicationContext))
            .transition(Utilidades.transicion)
            .into(portada)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()
        lista_libros = Utilidades.obtenerListaLibros(db_ref)

        modificar.setOnClickListener {
            if (nombre.text.toString().trim().isEmpty() || descripcion.text.toString().trim().isEmpty() || fecha.text.toString().trim().isEmpty() || rating.rating == 0.0f) {
                Toast.makeText(applicationContext, "Faltan datos en el formulario", Toast.LENGTH_SHORT).show()
            } else {
                launch {
                    // Verificar si el nombre ha cambiado
                    if (pojo_libros.nombre != nombre.text.toString().trim() && Utilidades.existeLibro(lista_libros, nombre.text.toString().trim())) {
                        Toast.makeText(applicationContext, "Ese Libro ya existe", Toast.LENGTH_SHORT).show()
                    } else {
                        var url_portada_firebase = String()

                        // Verificar si se seleccionó una nueva portada
                        if (url_portada != null) {
                            url_portada_firebase = Utilidades.guardarPortada(st_ref, pojo_libros.id!!, url_portada!!)
                        } else {
                            // Si no se seleccionó una nueva portada, usar la portada original
                            url_portada_firebase = pojo_libros.portada ?: ""
                        }

                        // Realizar la lógica de guardado y edición aquí
                        Utilidades.escribirLibro(
                            db_ref, pojo_libros.id!!,
                            nombre.text.toString().trim(),
                            descripcion.text.toString().trim(),
                            fecha.text.toString().trim(),
                            url_portada_firebase,
                            rating
                        )

                        Utilidades.tostadaCorrutina(this_activity, applicationContext, "Libro modificado con éxito")
                        val activity = Intent(applicationContext, MainActivity::class.java)
                        startActivity(activity)
                    }
                }
            }
        }

        volver.setOnClickListener {
            val activity = Intent(applicationContext, VerLibros::class.java)
            startActivity(activity)
        }

        portada.setOnClickListener {
            mostrarDialogoSeleccion()
        }
    }

    private fun mostrarDialogoSeleccion() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Elige una opción")
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> tomarFoto()
                1 -> seleccionarDeGaleria()
            }
        }
        builder.show()
    }

    private fun tomarFoto() {
        intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivity(intent)
    }

    private fun seleccionarDeGaleria() {
        accesoGaleria.launch("image/*")
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    {uri: Uri ->
        if(uri!=null){
            url_portada = uri
            portada.setImageURI(uri)
        }
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
}