package com.vincnx.androidsistemakademik.presentation.student.course

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vincnx.androidsistemakademik.MyApplication
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.data.source.db.DatabaseClient
import com.vincnx.androidsistemakademik.data.source.local.SessionManager
import com.vincnx.androidsistemakademik.domain.entities.Course
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vincnx.androidsistemakademik.data.repository.CourseRepo
import com.vincnx.androidsistemakademik.data.repository.EnrollmentRepo
import kotlinx.coroutines.launch

class CourseFragment : Fragment() {
    private lateinit var rvCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var dbClient: DatabaseClient
    private lateinit var sessionManager: SessionManager
    private lateinit var enrollmentRepo: EnrollmentRepo
    private lateinit var courseRepo: CourseRepo
    private val btnEnroll by lazy { view?.findViewById<View>(R.id.btn_enroll) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (requireActivity().application as MyApplication).appContainer
        dbClient = appContainer.database
        sessionManager = appContainer.sessionManager
        enrollmentRepo = appContainer.enrollmentRepo
        courseRepo = appContainer.courseRepo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_student, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        view?.let {
            rvCourses = it.findViewById(R.id.rv_courses)
            setupRecyclerView()
            val userId = fetchUserSession()
            if (userId == null) {
                findNavController().navigate(R.id.action_courseFragment_to_loginFragment)
                return
            }
            fetchEnrolledCourse(userId)

            btnEnroll?.setOnClickListener {
                findNavController().navigate(R.id.action_courseFragment_to_studentEnrollFragment)
            }
        }
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter()
        rvCourses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
        }
    }

    private fun fetchUserSession(): String? {
        val userId = sessionManager.getUserDetails()[SessionManager.KEY_USER_ID]

        return userId
    }

    private fun fetchEnrolledCourse(userId: String) {
        lifecycleScope.launch {
            enrollmentRepo.getEnrollments(userId)
                .onSuccess { enrollments ->
                    if (enrollments.isEmpty()) {
                        Toast.makeText(context, "No enrolled courses found", Toast.LENGTH_SHORT).show()
                    } else {
                        // First get the course IDs from enrollments
                        val courseIds = enrollments.map { it.course_id }
                        // Fetch courses and combine with enrollments
                        courseRepo.getCourses(courseIds)
                            .onSuccess { courses ->
                                // Create a map of courseId to Course for easy lookup
                                val courseMap = courses.associateBy { it.id }
                                // Create new Enrollment instances with the course data
                                val enrollmentsWithCourses = enrollments.map { enrollment ->
                                    enrollment.copy(
                                        course = courseMap[enrollment.course_id]
                                    )
                                }
                                courseAdapter.submitList(enrollmentsWithCourses)
                            }
                            .onFailure { error ->
                                Log.e("CourseFragment", "Error loading courses: ${error.message}", error)
                            }
                    }
                }
                .onFailure { error ->
                    Log.e("CourseFragment", "Error loading enrollments: ${error.message}", error)
                }
        }
    }
}