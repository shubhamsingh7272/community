package com.pratik.iiits.Timetable

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R
import java.util.*

class HolidayActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var datePickerButton: Button
    private lateinit var holidayDescriptionEditText: EditText
    private lateinit var saveButton: Button
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holiday)

        firestore = FirebaseFirestore.getInstance()

        datePickerButton = findViewById(R.id.datePickerButton)
        holidayDescriptionEditText = findViewById(R.id.holidayDescriptionEditText)
        saveButton = findViewById(R.id.saveButton)

        datePickerButton.setOnClickListener {
            showDatePicker()
        }

        saveButton.setOnClickListener {
            saveHoliday()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerDialogTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                datePickerButton.text = selectedDate
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun saveHoliday() {
        val holidayDescription = holidayDescriptionEditText.text.toString()

        if (holidayDescription.isBlank() || selectedDate.isNullOrBlank()) {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val holiday = hashMapOf(
            "date" to selectedDate,
            "description" to holidayDescription
        )

        firestore.collection("holidays")
            .document(selectedDate!!)
            .set(holiday)
            .addOnSuccessListener {
                Toast.makeText(this, "Holiday saved successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save holiday: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
