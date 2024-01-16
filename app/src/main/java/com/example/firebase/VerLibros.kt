package com.example.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VerLibros : AppCompatActivity() {
    private lateinit var volver: Button
    private lateinit var recycler: RecyclerView
    private  lateinit var lista:MutableList<Libro>
    private lateinit var adaptador: LibroAdaptador
    private lateinit var db_ref: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_libros)
        volver = findViewById(R.id.volver_inicio)
        lista = mutableListOf()
        db_ref = FirebaseDatabase.getInstance().getReference()
        db_ref.child("libreria")
            .child("libro")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach{hijo:DataSnapshot?
                        ->
                        val pojo_libro = hijo?.getValue(Libro::class.java)
                        lista.add(pojo_libro!!)
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        adaptador = LibroAdaptador(lista)
        recycler = findViewById(R.id.lista_libros)
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationContext)
        recycler.setHasFixedSize(true)

        volver.setOnClickListener {
            val activity = Intent(applicationContext, MainActivity::class.java)
            startActivity(activity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_libreria,menu)
        val item = menu?.findItem(R.id.search)
        val searhView = item?.actionView as SearchView

        searhView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adaptador.filter.filter((newText))
                return true
            }
        })

        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return  true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                adaptador.filter.filter("")
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun ordenarLibrosPorTituloAscendente() {
        lista.sortBy { it.nombre }
        adaptador.notifyDataSetChanged()
    }

    private fun ordenarLibrosPorTituloDescendente() {
        lista.sortByDescending { it.nombre }
        adaptador.notifyDataSetChanged()
    }

    private fun ordenarLibrosPorRatingAscendente() {
        lista.sortBy { it.rating }
        adaptador.notifyDataSetChanged()
    }

    private fun ordenarLibrosPorRatingDescendente() {
        lista.sortByDescending { it.rating }
        adaptador.notifyDataSetChanged()
    }

    private fun ordenarLibrosPorFechaAscendente() {
        lista.sortBy { it.fecha }
        adaptador.notifyDataSetChanged()
    }

    private fun ordenarLibrosPorFechaDescendente() {
        lista.sortByDescending { it.fecha }
        adaptador.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_titulo_ascendente -> ordenarLibrosPorTituloAscendente()
            R.id.sort_titulo_descendente -> ordenarLibrosPorTituloDescendente()
            R.id.sort_rating_ascendente -> ordenarLibrosPorRatingAscendente()
            R.id.sort_rating_descendente -> ordenarLibrosPorRatingDescendente()
            R.id.sort_fecha_ascendente -> ordenarLibrosPorFechaAscendente()
            R.id.sort_fecha_descendente -> ordenarLibrosPorFechaDescendente()
        }
        return super.onOptionsItemSelected(item)
    }
}