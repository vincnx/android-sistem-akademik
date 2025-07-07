package com.vincnx.androidsistemakademik.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.vincnx.androidsistemakademik.domain.entities.Enrollment
import kotlinx.coroutines.tasks.await

class EnrollmentRepo(val db: FirebaseDatabase) {

    private val documentName = "enrollments"

    suspend fun getEnrollments(studentId: String): Result<List<Enrollment>> = runCatching {
        val snapshot = db.getReference(documentName)
            .orderByChild("student_id")
            .equalTo(studentId)
            .get()
            .await()

        if (!snapshot.exists()) {
            return@runCatching emptyList()
        }

        val enrollments = mutableListOf<Enrollment>()
        for (enrollment in snapshot.children) {
            val courseId = enrollment.child("course_id").getValue(String::class.java)
            val studentId = enrollment.child("student_id").getValue(String::class.java)

            if (courseId != null && studentId != null) {
                enrollments.add(Enrollment(course_id = courseId, student_id = studentId))
            }
        }

        enrollments
    }

    suspend fun createEnrollment(courseId: String, studentId: String): Result<Unit> = runCatching {
        val enrollment = hashMapOf(
            "course_id" to courseId,
            "student_id" to studentId
        )

        db.getReference(documentName)
            .push() // This creates a new unique key for the enrollment
            .setValue(enrollment)
            .await()
    }
}