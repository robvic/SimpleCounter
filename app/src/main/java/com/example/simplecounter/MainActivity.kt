package com.example.simplecounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter
    private lateinit var recyclerView: RecyclerView
    lateinit var resultLauncher: ActivityResultLauncher<Intent>


    companion object {
        const val REQUEST_CODE = 1
    }

    private fun saveData(items: List<Item>) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(items)
        editor.putString("item list", json)
        editor.apply()
    }

    private fun loadData(): List<Item> {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("item list", null)
        val type = object : TypeToken<List<Item>>() {}.type
        return gson.fromJson(json, type) ?: listOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize your data
        val items = loadData().toMutableList()

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val delete = data?.getBooleanExtra("delete", false)
                val position = data?.getIntExtra("itemPosition", -1)
                if (delete == true && position != null && position != -1) {
                    // Remove the item from the list
                    adapter.removeItem(position)
                    saveData(adapter.getItems())
                } else {
                    val newText = data?.getStringExtra("itemText") ?: ""
                    val newNumber = data?.getIntExtra("itemCount", 0)!!
                    if (position != null && position != -1) {
                        // Update the item in the list
                        adapter.updateItem(position, Item(newText, newNumber))
                        adapter.notifyItemChanged(position)
                        saveData(adapter.getItems())
                    }
                }
            }
        }


        // Initialize the adapter with the data
        adapter = ItemAdapter()
        adapter.initializeItems(items)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerView)

        // Set the adapter on the RecyclerView
        recyclerView.adapter = adapter

        // Use a Linear Layout Manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add listener for the button
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            val editText = EditText(this@MainActivity)

            val dialog = AlertDialog.Builder(this@MainActivity)
                .setTitle("New Item")
                .setMessage("Enter item name:")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    val newItemText = editText.text.toString()
                    adapter.addItem(Item(newItemText, 0))

                    // Save data
                    saveData(adapter.getItems())
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val delete = data?.getBooleanExtra("delete", false)
                val position = data?.getIntExtra("itemPosition", -1)
                if (delete == true && position != null && position != -1) {
                    // Remove the item from the list
                    adapter.removeItem(position)
                    saveData(adapter.getItems())
                } else {
                    val newText = data?.getStringExtra("itemText") ?: ""
                    val newNumber = data?.getIntExtra("itemCount", 0)!!
                    if (position != null && position != -1) {
                        // Update the item in the list
                        adapter.updateItem(position, Item(newText, newNumber))
                        adapter.notifyItemChanged(position)
                        saveData(adapter.getItems())
                    }
                }
            }
        }
    }
}

data class Item(var text: String, var number: Int)

class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private val items: MutableList<Item> = mutableListOf()

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.item_text)
        val itemNumber: TextView = itemView.findViewById(R.id.item_number)
    }

    fun initializeItems(initialItems: List<Item>) {
        items.clear()
        items.addAll(initialItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemText.text = item.text
        holder.itemNumber.text = item.number.toString()

        // Set listener for item view
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ItemActivity::class.java).apply {
                putExtra("itemText", item.text)
                putExtra("itemCount", item.number)
                putExtra("itemPosition", position) // Pass the position of the item
            }
            if (context is MainActivity) {
                context.resultLauncher.launch(intent)
            }
        }

    }

    override fun getItemCount() = items.size

    fun addItem(item: Item) {
        items.add(item)
        notifyItemInserted(items.size-1)
    }

    fun getItems(): List<Item> {
        return items
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateItem(position: Int, newItem: Item) {
        items[position] = newItem
        notifyItemChanged(position)
    }
}
