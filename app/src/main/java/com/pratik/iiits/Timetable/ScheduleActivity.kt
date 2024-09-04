package com.pratik.iiits.Timetable

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Space
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {


    lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var ugSpinner: Spinner
    private lateinit var branchSpinner: Spinner
    private lateinit var sectionSpinner: Spinner
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val scheduleList: MutableList<ClassSchedule> = mutableListOf()
    private val holidayDates: MutableList<String> = mutableListOf()
    private val holidayDetailsMap: MutableMap<String, String> = mutableMapOf()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        firestore = FirebaseFirestore.getInstance()

        ugSpinner = findViewById(R.id.ugSpinner)
        branchSpinner = findViewById(R.id.branchSpinner)
        sectionSpinner = findViewById(R.id.sectionSpinner)
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        scheduleAdapter = ScheduleAdapter(scheduleList)
        recyclerView.adapter = scheduleAdapter

        loadUGPrograms()
        loadHolidays()
        checkIfAdmin()

        findViewById<ImageButton>(R.id.addSchedule).setOnClickListener {
            val intent = Intent(this@ScheduleActivity, Adminentry::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.addHoliday).setOnClickListener {
            val intent = Intent(this@ScheduleActivity, HolidayActivity::class.java)
            startActivity(intent)
        }

        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val clickedDayCalendar = eventDay.calendar
                val selectedDate = String.format("%04d-%02d-%02d",
                    clickedDayCalendar.get(Calendar.YEAR),
                    clickedDayCalendar.get(Calendar.MONTH) + 1,
                    clickedDayCalendar.get(Calendar.DAY_OF_MONTH))

                showHolidayDetails(selectedDate)
                val dayOfWeek = clickedDayCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                Log.d("CalendarView", "Selected Date: $selectedDate")
                Log.d("CalendarView", "Day of Week: $dayOfWeek")

                // Fetch the schedule based on the selected date and day of the week
                fetchSchedule(dayOfWeek, selectedDate)
            }
        })
    }

    private fun loadUGPrograms() {
        firestore.collection("ugPrograms").get().addOnSuccessListener { documents ->
            val ugPrograms = documents.map { it.id }
            val ugAdapter = ArrayAdapter(this, R.layout.spinner_item, ugPrograms)
            ugAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            ugSpinner.adapter = ugAdapter

            ugSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedUG = ugPrograms[position]
                    loadBranches(selectedUG)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun loadBranches(ugProgram: String) {
        firestore.collection("ugPrograms").document(ugProgram).collection("branches").get().addOnSuccessListener { documents ->
            val branches = documents.map { it.id }
            val branchAdapter = ArrayAdapter(this, R.layout.spinner_item, branches)
            branchAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            branchSpinner.adapter = branchAdapter

            branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedBranch = branches[position]
                    loadSections(ugProgram, selectedBranch)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun loadSections(ugProgram: String, branch: String) {
        firestore.collection("ugPrograms").document(ugProgram).collection("branches").document(branch).collection("sections").get().addOnSuccessListener { documents ->
            val sections = documents.map { it.id }
            val sectionAdapter = ArrayAdapter(this, R.layout.spinner_item, sections)
            sectionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            sectionSpinner.adapter = sectionAdapter

            sectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    // No specific action needed here for now
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun checkIfAdmin() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val postInIIIT = document.getString("postinIIIT")
                        Log.e(ContentValues.TAG, postInIIIT.toString())
                        if (postInIIIT == "Admin" || postInIIIT == "Council") {
                            findViewById<ImageButton>(R.id.addHoliday).visibility=ImageButton.VISIBLE
                            findViewById<ImageButton>(R.id.addSchedule).visibility=ImageButton.VISIBLE
                        } else {
                            findViewById<ImageButton>(R.id.addHoliday).visibility=ImageButton.GONE
                            findViewById<ImageButton>(R.id.addSchedule).visibility=ImageButton.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error fetching user details: $e")
                    findViewById<ImageButton>(R.id.addHoliday).visibility=ImageButton.GONE
                    findViewById<ImageButton>(R.id.addSchedule).visibility=ImageButton.GONE
                }
        }
    }

    private fun fetchSchedule(dayOfWeek: String?, selectedDate: String?) {
        val selectedUG = ugSpinner.selectedItem?.toString()
        val selectedBranch = branchSpinner.selectedItem?.toString()
        val selectedSection = sectionSpinner.selectedItem?.toString()

        if (selectedUG.isNullOrBlank() || selectedBranch.isNullOrBlank() || selectedSection.isNullOrBlank() || dayOfWeek.isNullOrBlank()) {
            Toast.makeText(this, "Please select UG program, branch, section, and a date.", Toast.LENGTH_SHORT).show()
            return
        }

        val sectionRef = firestore.collection("ugPrograms")
            .document(selectedUG)
            .collection("branches")
            .document(selectedBranch)
            .collection("sections")
            .document(selectedSection)

        // Clear the current schedule list before fetching new data
        scheduleList.clear()
        scheduleAdapter.notifyDataSetChanged() // Ensure the adapter is updated immediately

        val specificSchedulesRef = if (selectedDate != null) {
            sectionRef.collection("specificSchedules")
                .document(selectedDate)
                .collection("classes")
        } else {
            null
        }

        val weeklySchedulesRef = sectionRef
            .collection("weeklySchedules")
            .document(dayOfWeek!!)
            .collection("classes")

        if (specificSchedulesRef != null) {
            specificSchedulesRef.get().addOnSuccessListener { documents ->
                Log.d("fetchSchedule", "Specific Schedule Documents: ${documents.size()}")
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val schedule = document.toObject(ClassSchedule::class.java)
                        scheduleList.add(schedule)
                        Toast.makeText(this, "Specific Schedule: ${schedule.subject} - ${schedule.time}", Toast.LENGTH_SHORT).show()
                    }
                    // Notify adapter about data change
                    scheduleAdapter.notifyDataSetChanged()
                }
                // Fetch weekly schedules even if specific schedules are found
                fetchWeeklySchedules(weeklySchedulesRef)
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch specific schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                // Fetch weekly schedules if fetching specific schedules fails
                fetchWeeklySchedules(weeklySchedulesRef)
            }
        } else {
            // If specificSchedulesRef is null, fetch weekly schedules directly
            fetchWeeklySchedules(weeklySchedulesRef)
        }
    }

    private fun fetchWeeklySchedules(weeklySchedulesRef: CollectionReference) {
        weeklySchedulesRef.get().addOnSuccessListener { weeklyDocuments ->
            Log.d("fetchWeeklySchedules", "Weekly Schedule Documents: ${weeklyDocuments.size()}")
            if (!weeklyDocuments.isEmpty) {
                for (document in weeklyDocuments) {
                    val schedule = document.toObject(ClassSchedule::class.java)
                    scheduleList.add(schedule)
                    Toast.makeText(this, "Weekly Schedule: ${schedule.subject} - ${schedule.time}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If no weekly schedules found, notify the user
                if (scheduleList.isEmpty()) {
                    Toast.makeText(this, "No Schedule Found", Toast.LENGTH_SHORT).show()
                }
            }
            // Notify the adapter to refresh the RecyclerView
            scheduleAdapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch weekly schedule: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedDate(): String? {
        val selectedCalendar = calendarView.selectedDates.firstOrNull()
        if (selectedCalendar != null) {
            val year = selectedCalendar.get(Calendar.YEAR)
            val month = selectedCalendar.get(Calendar.MONTH) + 1 // Month is zero-based
            val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
            return String.format("%04d-%02d-%02d", year, month, day)
        }
        return null
    }

    // Updated to load holidays from Firestore and highlight them
    private fun highlightHolidays() {
        val holidayEvents = holidayDates.map { dateString ->
            val calendar = Calendar.getInstance().apply {
                time = dateFormat.parse(dateString) ?: return@map null
            }
            Log.d("Holiday", "Highlighting holiday on: ${calendar.time}")
            EventDay(calendar, R.drawable.holiday) // Assuming you have an icon for holidays
        }.filterNotNull()

        calendarView.setEvents(holidayEvents)
    }

    private fun loadHolidays() {
        val holidayRef = firestore.collection("holidays")
        holidayRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val holidayDate = document.getString("date")
                val holidayDetails = document.getString("description")
                if (holidayDate != null && holidayDetails != null) {
                    holidayDates.add(holidayDate)
                    holidayDetailsMap[holidayDate] = holidayDetails
                    // Debug statement
                    Log.d("Holiday", "Fetched holiday: $holidayDate - $holidayDetails")
                }
            }
            highlightHolidays()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load holidays: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHolidayDetails(selectedDate: String) {
        val holidayDetails = holidayDetailsMap[selectedDate]
        if (holidayDetails != null) {
            AlertDialog.Builder(this)
                .setTitle("Holiday Details")
                .setMessage(holidayDetails)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            Toast.makeText(this, "No holiday details found for the selected date.", Toast.LENGTH_SHORT).show()
        }
    }
}
