package com.example.firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CrearLibro : AppCompatActivity(), CoroutineScope {

    private lateinit var nombre : EditText
    private lateinit var descripcion : EditText
    private lateinit var portada: ImageView
    private lateinit var rating: RatingBar
    private lateinit var crear: Button
    private lateinit var volver: Button

    private var url_portada: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var lista_libros: MutableList<Libro>


    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_libro)


        val this_activity = this
        job = Job()

        nombre = findViewById(R.id.nombre)
        descripcion = findViewById(R.id.descripcion)
        portada = findViewById(R.id.portada)
        rating = findViewById(R.id.rating)
        crear = findViewById(R.id.crear)
        volver = findViewById(R.id.volver)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()
        lista_libros = Utilidades.obtenerListaLibros(db_ref)

        crear.setOnClickListener {
            if (nombre.text.toString().trim().isEmpty() || descripcion.text.toString().trim().isEmpty() || rating.rating == 0.0.toFloat()) {
                Toast.makeText(applicationContext, "Faltan datos en el formulario", Toast.LENGTH_SHORT).show()
            } else if (url_portada == null) {
                Toast.makeText(applicationContext, "Falta seleccionar la portada", Toast.LENGTH_SHORT).show()
            } else if (Utilidades.existeLibro(lista_libros, nombre.text.toString().trim())) {
                Toast.makeText(applicationContext, "Ese Libro ya existe", Toast.LENGTH_SHORT).show()
            } else {
                var id_generado: String? = db_ref.child("libreria").child("libros").push().key
                launch {
                    val url_portada_firebase = Utilidades.guardarPortada(st_ref, id_generado!!, url_portada!!)
                    Utilidades.escribirLibro(
                        db_ref, id_generado!!,
                        nombre.text.toString().trim(),
                        descripcion.text.toString().trim(),
                        Utilidades.obtenerFechaActual(),
                        url_portada_firebase,
                        rating
                    )
                    Utilidades.tostadaCorrutina(this_activity, applicationContext,"Libro creado con éxito")
                    val activity = Intent(applicationContext, MainActivity::class.java)
                    startActivity(activity)
                }
            }
        }
        volver.setOnClickListener {
            val activity = Intent(applicationContext, MainActivity::class.java)
            startActivity(activity)
        }

        portada.setOnClickListener {
            mostrarDialogoSeleccion()
        }
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
        // Lógica para iniciar la cámara y tomar una foto
        intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivity(intent)
    }

    private fun seleccionarDeGaleria() {
        accesoGaleria.launch("image/*")
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
}