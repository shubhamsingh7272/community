package com.pratik.iiits.Timetable

data class ClassSchedule(
    val ug: String = "",
    val branch: String = "",
    val section: String = "",
    val subject: String = "",
    val details: String = "",
    val time: String = "",
    val date: String? = null,
    val dayOfWeek: String? = null
)

data class DaySchedule(
    val schedules: List<ClassSchedule> = listOf()
)
