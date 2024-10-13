package hilari.abarca.agendacontactos

import android.annotation.SuppressLint
import android.os.Bundle
import android.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactAdapter: ContactosAdapter
    private lateinit var contactList: MutableList<Contacto>
    private lateinit var spinner: Spinner

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar RecyclerView y Adapter
        recyclerView = findViewById(R.id.recyclerViewContactos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar contactos del archivo JSON
        contactList = loadContactsFromJson()

        // Inicializar el Adapter
        contactAdapter = ContactosAdapter(contactList, this)
        recyclerView.adapter = contactAdapter

        // Spinner para elegir el criterio de ordenamiento
        spinner = findViewById(R.id.spinnerOrdenar)
        val adapter = ArrayAdapter.createFromResource(this, R.array.sort_options, android.R.layout.simple_spinner_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Cambiar el método de ordenación según la selección
                when (position) {
                    0 -> {
                        mergeSort(contactList)
                    }
                    1 -> {
                        quickSort(contactList)
                    }
                    2 -> {
                        insertSort(contactList)
                    }
                }
                contactAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botón para agregar un nuevo contacto
        val addButton: Button = findViewById(R.id.fabAgregar)
        addButton.setOnClickListener {
            showAddContactDialog()
        }
    }

    // Mostrar diálogo para agregar contacto
    @SuppressLint("MissingInflatedId")
    private fun showAddContactDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_contact, null)
        val nameEditText = dialogLayout.findViewById<EditText>(R.id.editTextName)
        val numberEditText = dialogLayout.findViewById<EditText>(R.id.editTextNumber)

        builder.setView(dialogLayout)
        builder.setTitle("Agregar Contacto")
        builder.setPositiveButton("Guardar") { dialog, _ ->
            val name = nameEditText.text.toString()
            val number = numberEditText.text.toString()
            if (validateContact(name, number)) {
                val newContact = Contacto(name, number)
                contactList.add(newContact)
                saveContactsToJson()
                mergeSort(contactList) // Ordenar por nombre después de agregar
                contactAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    // Función para validar el contacto
    private fun validateContact(name: String, number: String): Boolean {
        return name.isNotEmpty() && number.isNotEmpty() && number[0] != '0'
    }

    // Cargar contactos desde archivo JSON
    private fun loadContactsFromJson(): MutableList<Contacto> {
        val gson = Gson()
        val file = File(filesDir, "contacts.json")
        return if (file.exists()) {
            try {
                FileReader(file).use { reader ->
                    val contactType = object : TypeToken<MutableList<Contacto>>() {}.type
                    gson.fromJson(reader, contactType) ?: mutableListOf() // Manejo nulo
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }
        } else {
            mutableListOf() // Si el archivo no existe
        }
    }

    // Guardar contactos en archivo JSON
    private fun saveContactsToJson() {
        val gson = Gson()
        val file = File(filesDir, "contacts.json")
        try {
            val writer = FileWriter(file)
            gson.toJson(contactList, writer)
            writer.flush()
            writer.close() // Asegúrate de cerrar el writer
        } catch (e: Exception) {
            e.printStackTrace() // Imprime el error si ocurre
        }
    }

    // Merge Sort
    private fun mergeSort(arr: MutableList<Contacto>) {
        val startTime = System.nanoTime()
        if (arr.size <= 1) return

        val mid = arr.size / 2
        val left = arr.subList(0, mid).toMutableList()
        val right = arr.subList(mid, arr.size).toMutableList()

        mergeSort(left)
        mergeSort(right)

        var i = 0
        var j = 0
        var k = 0
        while (i < left.size && j < right.size) {
            if (left[i].nombre <= right[j].nombre) {
                arr[k] = left[i]
                i++
            } else {
                arr[k] = right[j]
                j++
            }
            k++
        }
        while (i < left.size) {
            arr[k] = left[i]
            i++
            k++
        }
        while (j < right.size) {
            arr[k] = right[j]
            j++
            k++
        }

        val endTime = System.nanoTime()
        println("Merge Sort Time: ${(endTime - startTime) / 1e6} ms")
    }

    // Quick Sort
    private fun quickSort(arr: MutableList<Contacto>, low: Int = 0, high: Int = arr.size - 1) {
        val startTime = System.nanoTime()
        if (low < high) {
            val pi = partition(arr, low, high)
            quickSort(arr, low, pi - 1)
            quickSort(arr, pi + 1, high)
        }
        val endTime = System.nanoTime()
        println("Quick Sort Time: ${(endTime - startTime) / 1e6} ms")
    }

    private fun partition(arr: MutableList<Contacto>, low: Int, high: Int): Int {
        val pivot = arr[high].nombre
        var i = low - 1
        for (j in low until high) {
            if (arr[j].nombre <= pivot) {
                i++
                Collections.swap(arr, i, j)
            }
        }
        Collections.swap(arr, i + 1, high)
        return i + 1
    }

    // Insertion Sort
    private fun insertSort(arr: MutableList<Contacto>) {
        val startTime = System.nanoTime()
        for (i in 1 until arr.size) {
            val key = arr[i]
            var j = i - 1
            while (j >= 0 && arr[j].nombre > key.nombre) {
                arr[j + 1] = arr[j]
                j--
            }
            arr[j + 1] = key
        }
        val endTime = System.nanoTime()
        println("Insertion Sort Time: ${(endTime - startTime) / 1e6} ms")
    }
}
