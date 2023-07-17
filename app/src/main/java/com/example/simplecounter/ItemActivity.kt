package com.example.simplecounter

import android.app.Activity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.activity.OnBackPressedCallback

class ItemActivity: AppCompatActivity() {
    private lateinit var item: Item

    // Update values on main activity
    private fun returnResult(delete: Boolean) {
        val returnIntent = Intent().apply {
            putExtra("delete", delete)
            putExtra("itemText", item.text)
            putExtra("itemCount", item.number)
            putExtra("itemPosition", intent.getIntExtra("itemPosition", -1))
        }
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        // Get item details from intent
        val itemText = intent.getStringExtra("itemText") ?: ""
        val itemNumber = intent.getIntExtra("itemCount", 0)
        item = Item(itemText, itemNumber)

        // Find TextViews
        val itemTextView: TextView = findViewById(R.id.itemName)
        val itemNumberView: TextView = findViewById(R.id.itemCount)

        // Set values
        itemTextView.text = itemText
        itemNumberView.text = itemNumber.toString()

        // Find Buttons
        val plusButton: Button = findViewById(R.id.btnAdd)
        val minusButton: Button = findViewById(R.id.btnSub)
        val resetButton: Button = findViewById(R.id.btnReset)
        val deleteButton: Button = findViewById(R.id.btnDel)

        // Set listeners for buttons
        plusButton.setOnClickListener {
            item.number++
            itemNumberView.text = item.number.toString()
            returnResult(false)
        }

        minusButton.setOnClickListener {
            item.number--
            itemNumberView.text = item.number.toString()
            returnResult(false)
        }

        resetButton.setOnClickListener {
            item.number = 0
            itemNumberView.text = item.number.toString()
            returnResult(false)
        }

        deleteButton.setOnClickListener{
            returnResult(true)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnResult(false)
                finish()
            }
        })
    }
}