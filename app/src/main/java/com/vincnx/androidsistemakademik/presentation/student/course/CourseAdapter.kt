package com.vincnx.androidsistemakademik.presentation.student.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vincnx.androidsistemakademik.R
import com.vincnx.androidsistemakademik.domain.entities.Course

class CourseAdapter : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {
    private var courses = listOf<Course>()

    fun submitList(newList: List<Course>) {
        courses = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tv_course_name)

        fun bind(course: Course) {
            tvCourseName.text = course.name
        }
    }
}