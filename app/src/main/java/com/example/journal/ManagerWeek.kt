package com.example.journal

import android.content.Context
import java.io.File
import java.io.FileReader

class ManagerWeek(private val ROOT_DIRECTORY: String, private val pupilUrl: String) {
    private val weekStates = ArrayList<ArrayList<PageLoadState>>()

    var weekLinks = ArrayList<ArrayList<String>>()

    // lessonsData[quarter][week][day][lesson]
    var lessonsData = ArrayList<ArrayList<ArrayList<ArrayList<StructLesson>>>>()

    // datesData[quarter][week][day]
    var datesData = ArrayList<ArrayList<ArrayList<StructDate>>>()

    init {
        checkFolders()
        initializeArrayLists()
        generateLinks()
    }

    fun fillWeek(weekPath: String, quarter: Int, week: Int, context: Context) {
        val lessonNames = ArrayList<String>()
        val marks = ArrayList<String>()
        val hometasks = ArrayList<String>()

        val text = FileReader(weekPath).readText()
        var dayCounter = 0
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

            dayCounter++
        }

        val maxLessons = dayCounter / 6
    }
    fun generateLinks() {
        weekLinks.clear()
        repeat(4) { weekLinks.add(ArrayList()) }
        for (i in 0..3) {
            var currentDate = YearData.FIRST_MONDAYS[i].clone()
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
            lessonsData.add(ArrayList())
            for (j in 0 until YearData.AMOUNTS_OF_WEEKS[i]) {
                datesData[i].add(ArrayList())
                lessonsData[i].add(ArrayList())
                for (k in 0..5) {
                    lessonsData[i][j].add(ArrayList())
                    datesData[i][j].add(StructDate(null))
                }
                weekStates[i].add(PageLoadState.DOWNLOADING)
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