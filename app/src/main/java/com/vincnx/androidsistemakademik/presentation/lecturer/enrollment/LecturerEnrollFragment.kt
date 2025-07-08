package com.vincnx.androidsistemakademik.presentation.lecturer.enrollment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vincnx.androidsistemakademik.MyApplication
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.data.repository.CourseRepo
import com.vincnx.androidsistemakademik.data.repository.EnrollmentRepo
import com.vincnx.androidsistemakademik.data.source.db.DatabaseClient
import com.vincnx.androidsistemakademik.data.source.local.SessionManager
import com.vincnx.androidsistemakademik.presentation.student.course.CourseAdapter
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.vincnx.androidsistemakademik.domain.entities.Enrollment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class LecturerEnrollFragment : Fragment() {
    private lateinit var rvEnrollments: RecyclerView
    private lateinit var enrollmentAdapter: EnrollmentAdapter
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lecturer_enroll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        view?.let {
            rvEnrollments = it.findViewById(R.id.rv_enrollments)
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
        enrollmentAdapter = EnrollmentAdapter { enrollment ->
            showGradeEditDialog(enrollment)
        }
        rvEnrollments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = enrollmentAdapter
        }
    }

    private fun fetchUserSession(): String? {
        val userId = sessionManager.getUserDetails()[SessionManager.KEY_USER_ID]

        return userId
    }

    private fun fetchEnrolledCourse(lecturerId: String) {
        lifecycleScope.launch {
            try {
                // First get all courses taught by this lecturer
                val coursesResult = courseRepo.getCoursesByLecturerId(lecturerId)
                val courses = coursesResult.getOrThrow()

                val allEnrollments = mutableListOf<Enrollment>()

                // For each course, get its enrollments
                for (course in courses) {
                    val enrollmentsResult = enrollmentRepo.getEnrollmentsByCourseId(course.id)
                    val enrollments = enrollmentsResult.getOrThrow()
                    
                    allEnrollments.addAll(enrollments)
                }

                // Update the adapter
                enrollmentAdapter.submitList(allEnrollments)
            } catch (e: Exception) {
                // Handle error - you might want to show an error message to the user
                Log.e("LecturerEnrollFragment", "Error fetching enrollments", e)
                // TODO: Show error message to user
            }
        }
    }

    private fun showGradeEditDialog(enrollment: Enrollment) {
        val grades = arrayOf("A", "B", "C", "D", "E")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Grade")
            .setSingleChoiceItems(grades, grades.indexOf(enrollment.grade), null)
            .setPositiveButton("Save") { dialog, _ ->
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (selectedPosition != -1) {
                    val selectedGrade = grades[selectedPosition]
                    updateGrade(enrollment, selectedGrade)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateGrade(enrollment: Enrollment, newGrade: String) {
        lifecycleScope.launch {
            try {
                enrollmentRepo.updateGrade(enrollment.course_id, enrollment.student_id, newGrade)
                    .onSuccess {
                        // Get current lecturer ID and refresh the data
                        val lecturerId = fetchUserSession()
                        if (lecturerId != null) {
                            fetchEnrolledCourse(lecturerId)
                        }
                        Toast.makeText(requireContext(), "Grade updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { exception ->
                        Toast.makeText(requireContext(), "Failed to update grade: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}