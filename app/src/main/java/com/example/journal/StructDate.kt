package com.example.journal

data class StructDate(
        var yearDay: StructDay?
) {
    var dateString = ""
    var weekDayString = ""

    init {
        if (yearDay != null) generateStrings()
    }

    fun generateStrings() {
        var month = YearData.FIRST_MONDAYS[yearDay!!.quarter][1]
        var day = YearData.FIRST_MONDAYS[yearDay!!.quarter][2]

        day += 7 * yearDay!!.week + yearDay!!.weekDay
        while (day > YearData.DAYS_IN_MONTH[month]) day -= YearData.DAYS_IN_MONTH[month++]

        dateString = "$day ${YearData.NAME_OF_MONTH[month - 1]}"
        weekDayString = YearData.NAME_OF_DAY[yearDay!!.weekDay]
    }
}