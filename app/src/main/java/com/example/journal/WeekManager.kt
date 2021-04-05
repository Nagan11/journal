package com.example.journal

import android.content.Context
import java.io.File
import java.io.FileReader
import kotlin.collections.ArrayList

class WeekManager(private val ROOT_DIRECTORY: String, private val pupilUrl: String) {
    private val weekStates = ArrayList<ArrayList<PageLoadState>>()

    var weekLinks = ArrayList<ArrayList<String>>()
    var weeksLayouts = ArrayList<ArrayList<ArrayList<DayView>>>(4)

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
        val maxLessons: Int

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
        maxLessons = dayCounter / 6

        val ar = ArrayList<DayView>()
        index = 0
        repeat(6)
        {
            ar.add(DayView(
                    context, DateGenerator.DATES[quarter][week][0],
                    lessonNames, marks, hometasks,
                    index, index + maxLessons - 1, false
            ))
            index += maxLessons
        }
        weeksLayouts[quarter][week] = ar.clone() as java.util.ArrayList<DayView>
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
            weeksLayouts.add(ArrayList())
            weekStates.add(ArrayList())
            for (j in 0 until YearData.AMOUNTS_OF_WEEKS[i]) {
                weeksLayouts[i].add(ArrayList())
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