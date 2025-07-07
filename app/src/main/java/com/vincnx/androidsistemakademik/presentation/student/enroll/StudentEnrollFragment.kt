package com.vincnx.androidsistemakademik.presentation.student.enroll

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vincnx.androidsistemakademik.MyApplication
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.data.repository.CourseRepo
import com.vincnx.androidsistemakademik.data.repository.EnrollmentRepo
import com.vincnx.androidsistemakademik.data.source.local.SessionManager
import com.vincnx.androidsistemakademik.di.AppContainer
import com.vincnx.androidsistemakademik.domain.entities.Course
import kotlinx.coroutines.launch

class StudentEnrollFragment : Fragment() {
    private lateinit var spinnerCourse: Spinner
    private lateinit var courseRepo: CourseRepo
    private lateinit var enrollmentRepo: EnrollmentRepo
    private lateinit var sessionManager: SessionManager
    private lateinit var btnSubmit: Button
    private var courses = mutableListOf<Course>()
    private var enrolledCourseIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (requireActivity().application as MyApplication).appContainer
        courseRepo = appContainer.courseRepo
        enrollmentRepo = appContainer.enrollmentRepo
        sessionManager = appContainer.sessionManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_enroll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        spinnerCourse = view.findViewById(R.id.spinner_course)
        btnSubmit = view.findViewById(R.id.btn_submit)
        loadEnrollmentsAndCourses()

        btnSubmit.setOnClickListener {
            val selectedPosition = spinnerCourse.selectedItemPosition
            if (selectedPosition != AdapterView.INVALID_POSITION) {
                val selectedCourse = courses[selectedPosition]
                createEnrollment(selectedCourse.id)
            } else {
                Toast.makeText(requireContext(), "Please select a course", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEnrollmentsAndCourses() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserDetails()[SessionManager.KEY_USER_ID]
            if (userId == null) {
                Toast.makeText(requireContext(), "User session not found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // First get enrollments
            enrollmentRepo.getEnrollments(userId)
                .onSuccess { enrollments ->
                    // Store enrolled course IDs
                    enrolledCourseIds.clear()
                    enrolledCourseIds.addAll(enrollments.map { it.course_id })
                    
                    // Then load courses
                    loadUnenrolledCourses()
                }
                .onFailure { exception ->
                    Toast.makeText(requireContext(), "Failed to load enrollments: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUnenrolledCourses() {
        lifecycleScope.launch {
            courseRepo.listCourses()
                .onSuccess { allCourses ->
                    // Filter out enrolled courses
                    val unenrolledCourses = allCourses.filter { course -> 
                        course.id !in enrolledCourseIds 
                    }
                    
                    courses.clear()
                    courses.addAll(unenrolledCourses)
                    
                    if (courses.isEmpty()) {
                        Toast.makeText(requireContext(), "No available courses to enroll", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_studentEnrollFragment_to_courseFragment)
                    } else {
                        setupSpinner()
                        btnSubmit.isEnabled = true
                    }
                }
                .onFailure { exception ->
                    Toast.makeText(requireContext(), "Failed to load courses: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            courses.map { it.name } // Assuming Course has a 'name' property
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter

        spinnerCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedCourse = courses[pos]
                // Handle the selected course
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle nothing selected
            }
        }
    }

    private fun createEnrollment(courseId: String) {
        val userId = sessionManager.getUserDetails()[SessionManager.KEY_USER_ID]
        if (userId == null) {
            Toast.makeText(requireContext(), "User session not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                enrollmentRepo.createEnrollment(courseId, userId)
                    .onSuccess {
                        Toast.makeText(requireContext(), "Successfully enrolled in course", Toast.LENGTH_SHORT).show()
                        // Optionally navigate back or refresh
                    }
                    .onFailure { exception ->
                        Toast.makeText(requireContext(), "Failed to enroll: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}