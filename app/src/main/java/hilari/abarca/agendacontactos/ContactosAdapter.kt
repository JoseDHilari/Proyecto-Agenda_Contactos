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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ContactosAdapter(private val contactos: MutableList<Contacto>, private val context: Context) : RecyclerView.Adapter<ContactosAdapter.ContactoViewHolder>() {

    var contactosAlternos: MutableList<Contacto> = contactos.toMutableList()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        val file = File(context.filesDir, "contacts.json")
        if (!file.exists() || file.length() == 0L) {
            // Si el archivo no existe o está vacío, cargar desde assets
            contactosAlternos = loadContactsFromAssets(context)
            saveContactsToFile()
        } else {
            // Si existe, cargar desde el archivo local
            contactosAlternos = loadContactsFromFile()
        }
    }

    private fun loadContactsFromAssets(context: Context): MutableList<Contacto> {
        val inputStream = context.assets.open("contacts.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val json = bufferedReader.use { it.readText() }

        val gson = Gson()
        val listType = object : TypeToken<MutableList<Contacto>>() {}.type
        return gson.fromJson(json, listType)
        notifyDataSetChanged()
    }

    private fun loadContactsFromFile(): MutableList<Contacto> {
        val file = File(context.filesDir, "contacts.json")
        val json = file.readText()
        val gson = Gson()
        val listType = object : TypeToken<MutableList<Contacto>>() {}.type
        return gson.fromJson(json, listType)
    }

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
            val originalPosition = contactosAlternos.indexOf(contacto)
            contactosAlternos.removeAt(originalPosition) // Remover de la lista alterna
            notifyItemRemoved(originalPosition)
            notifyItemRangeChanged(originalPosition, contactosAlternos.size)
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

    private fun saveContactsToFile() {
        val file = File(context.filesDir, "contacts.json")
        val json = Gson().toJson(contactosAlternos)
        file.writeText(json)
    }

    fun updateContacts(newContacts: List<Contacto>) {
        contactosAlternos.clear()
        contactosAlternos.addAll(newContacts) // Agregar nuevos contactos a la lista alterna
        notifyDataSetChanged()
    }
}
