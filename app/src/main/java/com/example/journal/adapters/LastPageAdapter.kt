package com.example.journal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.journal.LessonYearMarks
import com.example.journal.R

class LastPageAdapter : RecyclerView.Adapter<LastPageAdapter.ViewHolder>() {
    var data = ArrayList<LessonYearMarks>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lesson:   TextView
        val markYear: TextView
        val mark1Q:   TextView
        val mark2Q:   TextView
        val mark3Q:   TextView
        val mark4Q:   TextView

        init {
            lesson   = itemView.findViewById(R.id.lessonName)
            markYear = itemView.findViewById(R.id.markYear)
            mark1Q   = itemView.findViewById(R.id.mark1Q)
            mark2Q   = itemView.findViewById(R.id.mark2Q)
            mark3Q   = itemView.findViewById(R.id.mark3Q)
            mark4Q   = itemView.findViewById(R.id.mark4Q)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.view_subject_year, parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.lesson.text   = data[position].lesson
        holder.markYear.text = data[position].markYear
        holder.mark1Q.text   = data[position].mark1Q
        holder.mark2Q.text   = data[position].mark2Q
        holder.mark3Q.text   = data[position].mark3Q
        holder.mark4Q.text   = data[position].mark4Q
    }

    override fun getItemCount(): Int = data.size
}