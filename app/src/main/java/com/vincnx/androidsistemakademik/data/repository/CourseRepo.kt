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
}