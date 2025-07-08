package com.vincnx.androidsistemakademik.domain.entities

data class Enrollment(
    val course_id: String,
    val student_id: String,
    val course: Course?,
    val student: User?,
    val grade: String?,
)
