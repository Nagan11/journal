package com.example.journal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.io.FileReader

class ManagerWeek(private val ROOT_DIRECTORY: String, private val pupilUrl: String, private val context: Context) {
    val weekLinks = ArrayList<ArrayList<String>>()

    val weekStates = ArrayList<ArrayList<WeekState>>()

    val datesData = ArrayList<ArrayList<ArrayList<StructDate>>>()
    val lessonsViews = ArrayList<ArrayList<ArrayList<ArrayList<ConstraintLayout>>>>()

    init {
        checkFolders()
        initializeArrayLists()
        generateLinks()
    }

    @SuppressLint("InflateParams")
    fun createLessonViews(weekPath: String, quarter: Int, week: Int, inflater: LayoutInflater) {
        val lessonNames = ArrayList<String>()
        val marks = ArrayList<String>()
        val hometasks = ArrayList<String>()

        val text = FileReader(weekPath).readText()
        var lessonCounter = 0
        var temp: String
        var index = 0
        while (index < text.length) {
            temp = ""
            while (text[index] != '>') temp += text[index++]
            lessonNames.add(temp)
            index++

            temp = ""
            while (text[index] != '>') temp += text[index++]
            marks.add(temp)
            index++

            temp = ""
            while (text[index] != '>') temp += text[index++]
            hometasks.add(temp)
            index++

            lessonCounter++
        }

        var indexShift = 0
        val maxLessons = lessonCounter / 6
        for (weekDay in 0..5) {
            var lastLesson = maxLessons - 1
            while (lessonNames[indexShift + lastLesson] == "" || lessonNames[indexShift + lastLesson] == "-" && lastLesson > 0) lastLesson--
            if (lastLesson == 0 && lessonNames[indexShift] == "" || lessonNames[indexShift] == "-") {
                indexShift += maxLessons
                continue
            }

            val params = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = Calculation.dpToPx(8f, context)
                topMargin = Calculation.dpToPx(4f, context)
                rightMargin = Calculation.dpToPx(8f, context)
                bottomMargin = Calculation.dpToPx(4f, context)
            }

            for (i in 0..lastLesson) {
                lessonsViews[quarter][week][weekDay].let {
                    it.add(inflater.inflate(R.layout.view_lesson, null) as ConstraintLayout)
                    it[i].layoutParams = params
                    it[i].findViewById<TextView>(R.id.lesson).text = lessonNames[indexShift + i]
                    it[i].findViewById<TextView>(R.id.mark).text =
                            if (marks[indexShift + i] == "N/A") ""
                            else marks[indexShift + i]
                    it[i].findViewById<TextView>(R.id.hometask).text =
                            if (hometasks[indexShift + i] == "") "-"
                            else hometasks[indexShift + i]
                }
            }
            indexShift += maxLessons
        }
    }
    private fun generateLinks() {
        weekLinks.clear()
        repeat(4) { weekLinks.add(ArrayList()) }
        for (i in 0..3) {
            val currentDate = YearData.FIRST_MONDAYS[i].clone()
            for (week in 0 until YearData.AMOUNTS_OF_WEEKS[i]) {
                var currentLink = "$pupilUrl/dnevnik/" +
                        "quarter/${YearData.QUARTER_IDS[i]}/" +
                        "week/${currentDate[0]}-"
                currentLink += if (currentDate[1] < 10) "0${currentDate[1]}-" else "${currentDate[1]}-"
                currentLink += if (currentDate[2] < 10) "0${currentDate[2]}" else "${currentDate[2]}"

                weekLinks[i].add(currentLink)

                currentDate[2] += 7
                if (currentDate[2] > YearData.DAYS_IN_MONTH[currentDate[1]]) {
                    currentDate[2] -= YearData.DAYS_IN_MONTH[currentDate[1]++]
                }
            }
        }

        weekLinks.add(arrayListOf("$pupilUrl/dnevnik/last-page"))
    }
    private fun initializeArrayLists() {
        for (i in 0..3) {
            weekStates.add(ArrayList())
            datesData.add(ArrayList())
            lessonsViews.add(ArrayList())
            for (j in 0 until YearData.AMOUNTS_OF_WEEKS[i]) {
                datesData[i].add(ArrayList())
                lessonsViews[i].add(ArrayList())
                for (k in 0..5) {
                    lessonsViews[i][j].add(ArrayList())
                    datesData[i][j].add(StructDate(null))
                }
                weekStates[i].add(WeekState.EMPTY)
            }
        }
    }
    private fun checkFolders() {
        val pagesFolder = File("$ROOT_DIRECTORY/pages")
        val dataFolder = File("$ROOT_DIRECTORY/data")
        if (!pagesFolder.exists()) pagesFolder.mkdir()
        if (!dataFolder.exists()) dataFolder.mkdir()
    }
}