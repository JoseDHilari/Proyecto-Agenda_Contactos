package hilari.abarca.agendacontactos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactosAdapter(
    private val contactos: MutableList<Contacto>,
    private val context: Context
) : RecyclerView.Adapter<ContactosAdapter.ContactoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false)
        return ContactoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contacto = contactos[position]
        holder.nombreTextView.text = contacto.nombre
        holder.numeroTextView.text = contacto.numero.toString()

        // Listener para editar contacto (se implementará una lógica similar al agregar)
        holder.editImageView.setOnClickListener {
            // Aquí implementamos la lógica para editar un contacto
        }

        // Listener para eliminar contacto
        holder.deleteImageView.setOnClickListener {
            contactos.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, contactos.size)
        }
    }

    override fun getItemCount(): Int = contactos.size

    class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombre)
        val numeroTextView: TextView = itemView.findViewById(R.id.textViewNumero)
        val editImageView: ImageView = itemView.findViewById(R.id.iv_editar)
        val deleteImageView: ImageView = itemView.findViewById(R.id.iv_borrar)
    }
}
