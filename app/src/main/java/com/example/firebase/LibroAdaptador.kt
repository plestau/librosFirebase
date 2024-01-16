package com.example.firebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class LibroAdaptador(private val lista_libro: MutableList<Libro>):
    RecyclerView.Adapter<LibroAdaptador.LibroViewHolder>(), Filterable{
    private lateinit var contexto:Context
    private var lista_filtrada = lista_libro


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LibroAdaptador.LibroViewHolder {
        val vista_item = LayoutInflater.from(parent.context).inflate(R.layout.item_libro,parent,false)
        contexto = parent.context
        return LibroViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: LibroAdaptador.LibroViewHolder, position: Int) {
        val item_actual = lista_filtrada[position]
        holder.nombre.text = item_actual.nombre
        holder.descripcion.text = item_actual.descripcion
        holder.fecha.text = item_actual.fecha
        holder.rating.rating = (item_actual.rating ?: 0).toFloat()
        val URL:String? = when(item_actual.portada){
            ""-> null
            else -> item_actual.portada
        }

        Glide.with(contexto)
            .load(URL)
            .apply(Utilidades.opcionesGlide(contexto))
            .transition(Utilidades.transicion)
            .into(holder.miniatura)

        holder.editar.setOnClickListener {
            val activity = Intent(contexto,EditarLibro::class.java)
            activity.putExtra("libro", item_actual)
            contexto.startActivity(activity)
        }

        holder.eliminar.setOnClickListener {
            val  db_ref = FirebaseDatabase.getInstance().getReference()
            val sto_ref = FirebaseStorage.getInstance().getReference()
            lista_filtrada.remove(item_actual)
            sto_ref.child("libreria").child("libros")
                .child("portadas").child(item_actual.id!!).delete()
            db_ref.child("libreria").child("libro")
                .child(item_actual.id!!).removeValue()

            Toast.makeText(contexto,"Libro borrado con exito", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = lista_filtrada.size

    class LibroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val miniatura: ImageView = itemView.findViewById(R.id.item_miniatura)
        val nombre: TextView = itemView.findViewById(R.id.item_nombre)
        val descripcion: TextView = itemView.findViewById(R.id.item_descripcion)
        val fecha: TextView = itemView.findViewById(R.id.item_fecha)
        val rating: RatingBar = itemView.findViewById(R.id.item_rating)
        val editar: ImageView = itemView.findViewById(R.id.item_editar)
        val eliminar: ImageView = itemView.findViewById(R.id.item_borrar)
    }

    override fun getFilter(): Filter {
        return  object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val busqueda = p0.toString().lowercase()
                if (busqueda.isEmpty()){
                    lista_filtrada = lista_libro
                }else {
                    lista_filtrada = (lista_libro.filter {
                        it.nombre.toString().lowercase().contains(busqueda)
                    }) as MutableList<Libro>
                }

                val filterResults = FilterResults()
                filterResults.values = lista_filtrada
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}