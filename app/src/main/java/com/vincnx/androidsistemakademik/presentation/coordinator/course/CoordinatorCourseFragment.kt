package com.vincnx.androidsistemakademik.presentation.coordinator.course

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
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
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.vincnx.androidsistemakademik.domain.entities.User
import kotlinx.coroutines.tasks.await

class CoordinatorCourseFragment : Fragment() {
    private lateinit var rvCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var dbClient: DatabaseClient
    private lateinit var sessionManager: SessionManager
    private lateinit var enrollmentRepo: EnrollmentRepo
    private lateinit var courseRepo: CourseRepo
    private val btnAddCourse by lazy { view?.findViewById<View>(R.id.btn_add_course) }
    private var lecturers = mutableListOf<User>()

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
        return inflater.inflate(R.layout.fragment_coordinator_course, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        view?.let {
            rvCourses = it.findViewById(R.id.rv_courses)
            setupRecyclerView()
            fetchCourses()

            btnAddCourse?.setOnClickListener {
                showAddCourseDialog()
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

    private fun fetchCourses() {
        viewLifecycleOwner.lifecycleScope.launch {
            courseRepo.listCourses()
                .onSuccess { courses ->
                    courseAdapter.submitList(courses)
                }
                .onFailure { error ->
                    Log.e("CourseFragment", "Error loading courses: ${error.message}", error)
                }
        }
    }

    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_course, null)
        
        val courseNameInput = dialogView.findViewById<EditText>(R.id.et_course_name)
        val lecturerSpinner = dialogView.findViewById<Spinner>(R.id.spinner_lecturer)

        // Fetch lecturers and populate spinner
        viewLifecycleOwner.lifecycleScope.launch {
            fetchLecturers { users ->
                lecturers = users.toMutableList()
                val lecturerNames = users.map { it.name }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    lecturerNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lecturerSpinner.adapter = adapter
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Course")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val courseName = courseNameInput.text.toString()
                val selectedLecturerPosition = lecturerSpinner.selectedItemPosition
                
                if (courseName.isBlank()) {
                    Toast.makeText(context, "Please enter course name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (selectedLecturerPosition < 0 || selectedLecturerPosition >= lecturers.size) {
                    Toast.makeText(context, "Please select a lecturer", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedLecturer = lecturers[selectedLecturerPosition]
                
                viewLifecycleOwner.lifecycleScope.launch {
                    courseRepo.createCourse(courseName, selectedLecturer.id)
                        .onSuccess {
                            Toast.makeText(context, "Course added successfully", Toast.LENGTH_SHORT).show()
                            fetchCourses() // Refresh the course list
                        }
                        .onFailure { error ->
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun fetchLecturers(onComplete: (List<User>) -> Unit) {
        try {
            val snapshot = dbClient.database.getReference("users")
                .orderByChild("role")
                .equalTo("lecturer")
                .get()
                .await()

            val users = mutableListOf<User>()
            for (userSnapshot in snapshot.children) {
                userSnapshot.getValue(User::class.java)?.let { user ->
                    users.add(User(
                        id = userSnapshot.key ?: "",
                        name = user.name,
                        email = user.email,
                        role = user.role
                    ))
                }
            }
            onComplete(users)
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading lecturers: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("CoordinatorCourseFragment", "Error loading lecturers: ${e.message}", e)
            onComplete(emptyList())
        }
    }
}