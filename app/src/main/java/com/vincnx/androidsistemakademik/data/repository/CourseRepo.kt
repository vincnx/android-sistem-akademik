package com.vincnx.androidsistemakademik.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.vincnx.androidsistemakademik.domain.entities.Course
import kotlinx.coroutines.tasks.await

class CourseRepo(val db: FirebaseDatabase) {
    private val documentName = "courses"

    suspend fun getCourses(courseIds: List<String>): Result<List<Course>> = runCatching {
        val courses = mutableListOf<Course>()
        
        for (courseId in courseIds) {
            val snapshot = db.getReference(documentName)
                .child(courseId)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.getValue(Course::class.java)?.let { course ->
                    courses.add(course)
                }
            }
        }

        courses
    }

    suspend fun listCourses(): Result<List<Course>> = runCatching {
        val courses = mutableListOf<Course>()

        val snapshot = db.getReference(documentName)
            .get()
            .await()

        if (snapshot.exists()) {
            for (courseSnapshot in snapshot.children) {
                val course = courseSnapshot.getValue(Course::class.java)
                if (course != null) {
                    courses.add(Course(
                        id = courseSnapshot.key ?: "",
                        name = course.name,
                        lecturer_id = course.lecturer_id
                    ))
                }
            }
        }

        courses
    }

    suspend fun getCoursesByLecturerId(lecturerId: String): Result<List<Course>> = runCatching {
        val courses = mutableListOf<Course>()

        val snapshot = db.getReference(documentName)
            .orderByChild("lecturer_id")
            .equalTo(lecturerId)
            .get()
            .await()

        if (snapshot.exists()) {
            for (courseSnapshot in snapshot.children) {
                val course = courseSnapshot.getValue(Course::class.java)
                if (course != null) {
                    courses.add(Course(
                        id = courseSnapshot.key ?: "",
                        name = course.name,
                        lecturer_id = course.lecturer_id
                    ))
                }
            }
        }

        courses
    }

}