package com.pratik.iiits.Timetable

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R
import java.util.*

class Adminentry : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var ugEditText: EditText
    private lateinit var branchEditText: EditText
    private lateinit var sectionEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var datePickerButton: Button
    private lateinit var dayOfWeekSpinner: Spinner
    private lateinit var timePicker: TimePicker
    private lateinit var scheduleEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var addUgButton: Button
    private lateinit var addBranchButton: Button
    private lateinit var addSectionButton: Button
    private var selectedDate: String? = null
    private var selectedDayOfWeek: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adminentry)

        firestore = FirebaseFirestore.getInstance()

        ugEditText = findViewById(R.id.ugEditText)
        branchEditText = findViewById(R.id.branchEditText)
        sectionEditText = findViewById(R.id.sectionEditText)
        subjectEditText = findViewById(R.id.subjectEditText)
        datePickerButton = findViewById(R.id.datePickerButton)
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner)
        timePicker = findViewById(R.id.timePicker)
        scheduleEditText = findViewById(R.id.scheduleEditText)
        saveButton = findViewById(R.id.saveButton)
        addUgButton = findViewById(R.id.addUgButton)
        addBranchButton = findViewById(R.id.addBranchButton)
        addSectionButton = findViewById(R.id.addSectionButton)

        // Initialize Spinner with custom layout
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.days_of_week,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        dayOfWeekSpinner.adapter = adapter

        dayOfWeekSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedDayOfWeek = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedDayOfWeek = null
            }
        }

        datePickerButton.setOnClickListener {
            showDatePicker()
        }

        saveButton.setOnClickListener {
            saveSchedule()
        }

        addUgButton.setOnClickListener {
            addUGProgram()
        }

        addBranchButton.setOnClickListener {
            addBranch()
        }

        addSectionButton.setOnClickListener {
            addSection()
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

    private fun addUGProgram() {
        val ugProgram = ugEditText.text.toString()
        if (ugProgram.isNotEmpty()) {
            firestore.collection("ugPrograms").document(ugProgram)
                .set(mapOf("name" to ugProgram))
                .addOnSuccessListener {
                    Toast.makeText(this, "UG Program Added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to Add UG Program", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please enter UG Program name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addBranch() {
        val ugProgram = ugEditText.text.toString()
        val branch = branchEditText.text.toString()
        if (ugProgram.isNotEmpty() && branch.isNotEmpty()) {
            firestore.collection("ugPrograms").document(ugProgram)
                .collection("branches").document(branch)
                .set(mapOf("name" to branch))
                .addOnSuccessListener {
                    Toast.makeText(this, "Branch Added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to Add Branch", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please enter both UG Program and Branch name", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun addSection() {
        val ugProgram = ugEditText.text.toString()
        val branch = branchEditText.text.toString()
        val section = sectionEditText.text.toString()
        if (ugProgram.isNotEmpty() && branch.isNotEmpty() && section.isNotEmpty()) {
            firestore.collection("ugPrograms").document(ugProgram)
                .collection("branches").document(branch)
                .collection("sections").document(section)
                .set(mapOf("name" to section))
                .addOnSuccessListener {
                    Toast.makeText(this, "Section Added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to Add Section", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(
                this,
                "Please enter UG Program, Branch and Section name",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveSchedule() {
        val ug = ugEditText.text.toString()
        val branch = branchEditText.text.toString()
        val section = sectionEditText.text.toString()
        val subject = subjectEditText.text.toString()
        val scheduleDetails = scheduleEditText.text.toString()

        val hour = if (Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
        val minute = if (Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
        val time = String.format("%02d:%02d", hour, minute)

        if (ug.isBlank() || branch.isBlank() || section.isBlank() || subject.isBlank() || scheduleDetails.isBlank() || (selectedDate.isNullOrBlank() && selectedDayOfWeek.isNullOrBlank())) {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val schedule = hashMapOf(
            "ug" to ug,
            "branch" to branch,
            "section" to section,
            "subject" to subject,
            "details" to scheduleDetails,
            "time" to time
        )

        // Save to Firestore
        val ugProgramRef = firestore.collection("ugPrograms").document(ug)
        val branchRef = ugProgramRef.collection("branches").document(branch)
        val sectionRef = branchRef.collection("sections").document(section)

        val schedulesRef = firestore.collection("schedules")
            .document(ug)
            .collection(branch)
            .document(section)

        if (!selectedDate.isNullOrBlank()) {
            schedule["date"] = selectedDate!!

            val specificSchedulesRef = sectionRef
                .collection("specificSchedules")
                .document(selectedDate!!)
                .collection("classes")

            specificSchedulesRef
                .add(schedule)
                .addOnSuccessListener {
                    Toast.makeText(this, "Schedule saved successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            schedulesRef
                .collection("specificSchedules")
                .document(selectedDate!!)
                .collection("classes")
                .add(schedule)
                .addOnSuccessListener {
                    Toast.makeText(this, "Schedule saved successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else if (!selectedDayOfWeek.isNullOrBlank()) {
            schedule["dayOfWeek"] = selectedDayOfWeek!!

            val weeklySchedulesRef = sectionRef
                .collection("weeklySchedules")
                .document(selectedDayOfWeek!!)
                .collection("classes")

            weeklySchedulesRef
                .add(schedule)
                .addOnSuccessListener {
                    Toast.makeText(this, "Schedule saved successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            schedulesRef
                .collection("weeklySchedules")
                .document(selectedDayOfWeek!!)
                .collection("classes")
                .add(schedule)
                .addOnSuccessListener {
                    Toast.makeText(this, "Schedule saved successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
