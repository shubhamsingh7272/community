package com.pratik.iiits.Timetable

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.R

class ScheduleAdapter(private val scheduleList: List<ClassSchedule>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    // Define a list of colors for item backgrounds
    private val itemColors = listOf(
        Color.parseColor("#FFCDD2"), // Red
        Color.parseColor("#F0F4C3"), // Lime
        Color.parseColor("#BBDEFB"), // Blue
        Color.parseColor("#C8E6C9"), // Green
        Color.parseColor("#D1C4E9"), // Purple
        Color.parseColor("#FFECB3"), // Yellow
        Color.parseColor("#B2EBF2")  // Cyan
    )

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectTextView: TextView = itemView.findViewById(R.id.subjectTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val currentSchedule = scheduleList[position]
        holder.subjectTextView.text = currentSchedule.subject
        holder.timeTextView.text = currentSchedule.time
        holder.roomTextView.text = currentSchedule.details

        // Apply a background color from the predefined list, using the position modulo the list size to cycle through colors
        holder.itemView.setBackgroundColor(itemColors[position % itemColors.size])
    }

    override fun getItemCount() = scheduleList.size
}
