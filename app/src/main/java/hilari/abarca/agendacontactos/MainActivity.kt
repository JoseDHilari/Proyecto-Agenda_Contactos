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
import androidx.appcompat.widget.SearchView


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactAdapter: ContactosAdapter
    private lateinit var contactList: MutableList<Contacto>
    private lateinit var searchView: androidx.appcompat.widget.SearchView

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewContactos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        contactList = loadContactsFromJson()

        contactAdapter = ContactosAdapter(contactList, this)
        recyclerView.adapter = contactAdapter

        val spinner = findViewById<Spinner>(R.id.spinnerOrdenar)

        val opciones = arrayOf("Ordenar por Nombre", "Ordenar por Número")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Log.d("Estadisticas", "Opción seleccionada: $position")
                val seleccion = parent.getItemAtPosition(position).toString()

                when (seleccion) {
                    "Ordenar por Nombre" -> {
                        quickSortWithMetrics(contactList) { it.nombre }
                        contactAdapter.updateContacts(contactList)
                    }
                    "Ordenar por Número" -> {
                        quickSortWithMetrics(contactList) { it.numero }
                        contactAdapter.updateContacts(contactList)
                    }
                }
                contactAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val addButton: ImageButton = findViewById(R.id.fabAgregar)
        addButton.setOnClickListener {
            showAddContactDialog()
        }

        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val results = linearSearch(it)
                    contactAdapter.updateContacts(results)

                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val results = linearSearch(it)
                    contactAdapter.updateContacts(results)

                }
                return false
            }
        })
    }

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
            val name = nameEditText.text.toString().trim()
            val number = numberEditText.text.toString().trim()
            if (validateContact(name, number)) {
                val newContact = Contacto(name, number)
                contactList.add(newContact)
                saveContactsToJson()
                quickSortWithMetrics(contactList) { it.nombre } // Ordenar por nombre después de agregar
                contactAdapter.updateContacts(contactList)
            } else {
                Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun validateContact(name: String, number: String): Boolean {
        return name.isNotEmpty() && number.isNotEmpty() && number.matches(Regex("\\d+"))
    }

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

    fun <T : Comparable<T>> mergeSortWithMetrics(arr: MutableList<Contacto>, selector: (Contacto) -> T) {
        val startTime = System.nanoTime()

        val resu = mergeSort(arr, selector)

        val endTime = System.nanoTime()
        val duration = (endTime - startTime)/ 1_000_000

        Log.d("Estadisticas", "Merge Sort took $duration ms")
        return resu
    }

    fun <T : Comparable<T>> quickSortWithMetrics(arr: MutableList<Contacto>, selector: (Contacto) -> T) {
        val startTime = System.nanoTime()

        val resu = quickSort(arr, selector) // Llama al algoritmo de Quick Sort

        val endTime = System.nanoTime()
        val duration = endTime - startTime

        Log.d("Estadisticas", "Quick Sort took ${duration / 1_000_000} ms")
        return resu
    }

    fun <T : Comparable<T>> insertionSortWithMetrics(arr: MutableList<Contacto>, selector: (Contacto) -> T) {
        val startTime = System.nanoTime()

        val resu = insertionSort(arr, selector) // Llama al algoritmo de Insertion Sort

        val endTime = System.nanoTime()
        val duration = endTime - startTime

        Log.d("Estadisticas", "Insertion Sort took ${duration / 1_000_000} ms")
        return resu
    }

    private fun <T : Comparable<T>> mergeSort(arr: MutableList<Contacto>, selector: (Contacto) -> T) {
        val startTime = System.nanoTime()
        if (arr.size <= 1) return

        val mid = arr.size / 2
        val left = arr.subList(0, mid).toMutableList()
        val right = arr.subList(mid, arr.size).toMutableList()

        mergeSort(left, selector)
        mergeSort(right, selector)

        var i = 0
        var j = 0
        var k = 0
        while (i < left.size && j < right.size) {
            if (selector(left[i]) <= selector(right[j])) {
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

    private fun <T : Comparable<T>> quickSort(arr: MutableList<Contacto>, selector: (Contacto) -> T) {
        if (arr.size <= 1) return

        fun partition(list: MutableList<Contacto>, low: Int, high: Int): Int {
            val pivot = selector(list[high])
            var i = low - 1
            for (j in low until high) {
                if (selector(list[j]) <= pivot) {
                    i++
                    Collections.swap(list, i, j)
                }
            }
            Collections.swap(list, i + 1, high)
            return i + 1
        }

        fun quickSortHelper(list: MutableList<Contacto>, low: Int, high: Int) {
            if (low < high) {
                val pi = partition(list, low, high)
                quickSortHelper(list, low, pi - 1)
                quickSortHelper(list, pi + 1, high)
            }
        }

        quickSortHelper(arr, 0, arr.size - 1)
    }

    private fun <T : Comparable<T>> insertionSort(arr: MutableList<Contacto>, selector: (Contacto) -> T) {
        for (i in 1 until arr.size) {
            val key = arr[i]
            var j = i - 1
            while (j >= 0 && selector(arr[j]) > selector(key)) {
                arr[j + 1] = arr[j]
                j--
            }
            arr[j + 1] = key
        }
    }

    private fun linearSearch(query: String): List<Contacto> {
        val startTime = System.nanoTime() // Inicia el temporizador

        val result = contactList.filter { it.nombre.contains(query, ignoreCase = true) }

        val endTime = System.nanoTime() // Finaliza el temporizador
        val duration = (endTime - startTime) / 1_000_000 // Convierte a milisegundos

        Log.d("Estadisticas", "Linear Search took $duration ms") // Muestra el tiempo en Logcat
        return result
    }

    private fun binarySearch(query: String): Contacto? {
        val startTime = System.nanoTime() // Inicia el temporizador

        var left = 0
        var right = contactList.size - 1

        while (left <= right) {
            val mid = left + (right - left) / 2
            val midContact = contactList[mid]

            when {
                midContact.nombre.equals(query, ignoreCase = true) -> {
                    val endTime = System.nanoTime() // Finaliza el temporizador
                    val duration = (endTime - startTime) / 1_000_000 // Convierte a milisegundos
                    Log.d("Estadisticas", "Binary Search took $duration ms") // Muestra el tiempo en Logcat
                    return midContact
                }
                midContact.nombre < query -> left = mid + 1
                else -> right = mid - 1
            }
        }

        val endTime = System.nanoTime() // Finaliza el temporizador
        val duration = (endTime - startTime) / 1_000_000 // Convierte a milisegundos
        Log.d("Estadisticas", "Binary Search took $duration ms") // Muestra el tiempo en Logcat
        return null
    }


}
