package com.vincnx.androidsistemakademik.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.vincnx.androidsistemakademik.domain.entities.Enrollment
import com.vincnx.androidsistemakademik.domain.entities.User
import com.vincnx.androidsistemakademik.domain.entities.Course
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
        for (enrollmentSnapshot in snapshot.children) {
            val courseId = enrollmentSnapshot.child("course_id").getValue(String::class.java)
            val studentId = enrollmentSnapshot.child("student_id").getValue(String::class.java)
            val grade = enrollmentSnapshot.child("grade").getValue(String::class.java)

            if (courseId != null && studentId != null) {
                enrollments.add(
                    Enrollment(
                        course_id = courseId,
                        student_id = studentId,
                        course = null,
                        student = null,
                        grade = grade
                    )
                )
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
            .push()
            .setValue(enrollment)
            .await()
    }

    suspend fun getEnrollmentsByCourse(courseId: String): Result<List<Enrollment>> = runCatching {
        val snapshot = db.getReference(documentName)
            .orderByChild("course_id")
            .equalTo(courseId)
            .get()
            .await()

        if (!snapshot.exists()) {
            return@runCatching emptyList()
        }

        val enrollments = mutableListOf<Enrollment>()
        for (enrollment in snapshot.children) {
            val courseId = enrollment.child("course_id").getValue(String::class.java)
            val studentId = enrollment.child("student_id").getValue(String::class.java)
            val grade = enrollment.child("grade").getValue(String::class.java)

            if (courseId != null && studentId != null) {
                val studentSnapshot = db.getReference("users")
                    .child(studentId)
                    .get()
                    .await()
                
                val student = studentSnapshot.getValue(User::class.java)
                if (student != null) {
                    enrollments.add(
                        Enrollment(
                            course_id = courseId,
                            student_id = studentId,
                            grade = grade ?: "",
                            student = student,
                            course = Course(lecturer_id = "")
                        )
                    )
                }
            }
        }

        enrollments
    }

    suspend fun getEnrollmentsByCourseId(courseId: String): Result<List<Enrollment>> = runCatching {
        val snapshot = db.getReference(documentName)
            .orderByChild("course_id")
            .equalTo(courseId)
            .get()
            .await()

        if (!snapshot.exists()) {
            return@runCatching emptyList()
        }

        val enrollments = mutableListOf<Enrollment>()
        for (enrollment in snapshot.children) {
            val studentId = enrollment.child("student_id").getValue(String::class.java)
            val grade = enrollment.child("grade").getValue(String::class.java)

            if (studentId != null) {
                val studentSnapshot = db.getReference("users")
                    .child(studentId)
                    .get()
                    .await()
                
                val student = studentSnapshot.getValue(User::class.java)
                if (student != null) {
                    enrollments.add(
                        Enrollment(
                            course_id = courseId,
                            student_id = studentId,
                            grade = grade ?: "",
                            student = student,
                            course = null
                        )
                    )
                }
            }
        }

        enrollments
    }

    suspend fun updateGrade(courseId: String, studentId: String, grade: String): Result<Unit> = runCatching {
        val snapshot = db.getReference(documentName)
            .orderByChild("course_id")
            .equalTo(courseId)
            .get()
            .await()

        if (!snapshot.exists()) {
            throw Exception("Enrollment not found")
        }

        for (enrollment in snapshot.children) {
            val enrollmentStudentId = enrollment.child("student_id").getValue(String::class.java)
            if (enrollmentStudentId == studentId) {
                enrollment.ref.child("grade").setValue(grade).await()
                break
            }
        }
    }
}