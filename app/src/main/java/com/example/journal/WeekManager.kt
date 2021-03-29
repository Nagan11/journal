package com.example.journal

import com.example.journal.ViewSets.Day
import java.io.File
import java.io.FileReader
import kotlin.collections.ArrayList

class WeekManager(private val ROOT_DIRECTORY: String, private val pupilUrl: String) {
    private val lessonNames = ArrayList<ArrayList<ArrayList<String>>>()
    private val marks = ArrayList<ArrayList<ArrayList<String>>>()
    private val hometasks = ArrayList<ArrayList<ArrayList<String>>>()
    private val weekStates = ArrayList<ArrayList<PageLoadState>>()
    private val maxLessons = arrayOf(
            intArrayOf(YearData.AMOUNTS_OF_WEEKS[0]),
            intArrayOf(YearData.AMOUNTS_OF_WEEKS[1]),
            intArrayOf(YearData.AMOUNTS_OF_WEEKS[2]),
            intArrayOf(YearData.AMOUNTS_OF_WEEKS[3])
    )

    var weekLinks = ArrayList<ArrayList<String>>()
    var weeksLayouts = ArrayList<ArrayList<ArrayList<Day>>>(4)

    init {
        checkFolders()
        initializeArrayLists()
        generateLinks()
    }

    private fun readWeekData(weekPath: String, quarter: Int, week: Int) {
        val text = FileReader(weekPath).readText()
        var dayCounter = 0
        var temp: String
        var index = 0
        while (index < text.length) {
            temp = ""
            while (text[index] != '>') temp += text[index++]
            lessonNames[quarter][week].add(temp)
            index++

            temp = ""
            while (text[index] != '>') temp += text[index++]
            marks[quarter][week].add(temp)
            index++

            temp = ""
            while (text[index] != '>') temp += text[index++]
            hometasks[quarter][week].add(temp)
            index++

            dayCounter++
        }
        maxLessons[quarter][week] = dayCounter / 6
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

                println("week -> $week, currentDate -> ${currentDate[2]}-${currentDate[1]}-${currentDate[0]}")
                println(YearData.DAYS_IN_MONTH[currentDate[1]])

                currentDate[2] += 7
                if (currentDate[2] > YearData.DAYS_IN_MONTH[currentDate[1]]) {
                    currentDate[2] -= YearData.DAYS_IN_MONTH[currentDate[1]++]
                }
            }
        }

        var lp: String = "$pupilUrl/dnevnik/last-page"
        weekLinks.add(ArrayList())
        weekLinks[4].add(lp)
    }
    private fun initializeArrayLists() {
        for (i in 0..3) {
            lessonNames.add(ArrayList())
            marks.add(ArrayList())
            hometasks.add(ArrayList())
            weeksLayouts.add(ArrayList())
            weekStates.add(ArrayList())
            for (j in 0 until YearData.AMOUNTS_OF_WEEKS[i]) {
                lessonNames[i].add(ArrayList())
                marks[i].add(ArrayList())
                hometasks[i].add(ArrayList())
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