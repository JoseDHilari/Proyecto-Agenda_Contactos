package hilari.abarca.agendacontactos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ContactosAdapter(private val contactos: MutableList<Contacto>, private val context: Context) : RecyclerView.Adapter<ContactosAdapter.ContactoViewHolder>() {

    var contactosAlternos: MutableList<Contacto> = contactos.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false)
        return ContactoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contacto = contactosAlternos[position]
        holder.nombreTextView.text = contacto.nombre
        holder.numeroTextView.text = contacto.numero.toString()

        // Listener para editar contacto
        holder.editImageView.setOnClickListener {
            showEditContactDialog(contacto, position)
        }

        // Listener para eliminar contacto
        holder.deleteImageView.setOnClickListener {
            val originalPosition = contactos.indexOf(contacto)
            contactos.removeAt(originalPosition) // Remover de la lista original
            contactos.removeAt(position) // Remover de la lista alterna
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, contactosAlternos.size)
            saveContactsToFile()
        }
    }

    override fun getItemCount(): Int = contactosAlternos.size

    class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombre)
        val numeroTextView: TextView = itemView.findViewById(R.id.textViewNumero)
        val editImageView: ImageView = itemView.findViewById(R.id.iv_editar)
        val deleteImageView: ImageView = itemView.findViewById(R.id.iv_borrar)
    }

    // Método para mostrar el diálogo de edición
    @SuppressLint("MissingInflatedId")
    private fun showEditContactDialog(contacto: Contacto, position: Int) {
        // Inflar el diseño del diálogo
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_contact, null)
        val editTextNombre = dialogView.findViewById<EditText>(R.id.editTextNombre)
        val editTextNumero = dialogView.findViewById<EditText>(R.id.editTextNumero)

        // Prellenar con los datos actuales
        editTextNombre.setText(contacto.nombre)
        editTextNumero.setText(contacto.numero.toString())

        // Crear el diálogo
        val dialog = AlertDialog.Builder(context)
            .setTitle("Editar Contacto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                // Actualizar el contacto con los nuevos datos
                contacto.nombre = editTextNombre.text.toString()
                contacto.numero = editTextNumero.text.toString()

                // Notificar al adaptador que el ítem ha cambiado
                notifyItemChanged(position)

                // Guardar los cambios en el archivo JSON
                saveContactsToFile()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    // Guardar la lista de contactos actualizada en el archivo JSON
    private fun saveContactsToFile() {
        val file = File(context.filesDir, "contacts.json")
        val json = Gson().toJson(contactos)
        file.writeText(json)
    }

    // Método para actualizar la lista alterna
    fun updateContacts(newContacts: List<Contacto>) {
        contactosAlternos.clear()
        contactosAlternos.addAll(newContacts) // Agregar nuevos contactos a la lista alterna
        notifyDataSetChanged()
    }
}
