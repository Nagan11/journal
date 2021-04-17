package com.example.journal

import java.util.*

data class StructDay(
        var quarter: Int,
        var week: Int,
        var weekDay: Int
) {
    operator fun minus(amount: Int): StructDay { // positive amount only
        weekDay -= amount;
        while (weekDay < 0) {
            weekDay += 6
            if (--week < 0) {
                if (quarter <= 0) {
                    quarter--
                    week = 0
                    weekDay = 0
                    return this
                }
                week = YearData.AMOUNTS_OF_WEEKS[--quarter] - 1
            }
        }
        return this
    }
    operator fun plus(amount: Int): StructDay { // positive amount only
        weekDay += amount
        while (weekDay > 5) {
            weekDay -= 6
            if (++week >= YearData.AMOUNTS_OF_WEEKS[quarter]) {
                week = 0
                if (++quarter > 3) {
                    week = 0
                    weekDay = 0
                    return this
                }
            }
        }
        return this
    }

    fun setCurrentDay() {
        val currentCalendar = GregorianCalendar()
        currentCalendar.firstDayOfWeek = Calendar.MONDAY
        this.weekDay =
                if (currentCalendar[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) 0
                else currentCalendar[Calendar.DAY_OF_WEEK] - 2

        if (currentCalendar[Calendar.YEAR] < YearData.FIRST_MONDAYS[0][0] || currentCalendar[Calendar.YEAR] > YearData.FIRST_MONDAYS[3][0]) {
            this.quarter = -1
            this.week = -1
            this.weekDay = -1
            return
        }

        var dayOfYear: Int = currentCalendar[Calendar.DAY_OF_YEAR]
        if (currentCalendar[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
            dayOfYear++
        } else {
            dayOfYear -= (currentCalendar[Calendar.DAY_OF_WEEK] - 2)
        }

        if (currentCalendar[Calendar.YEAR] == YearData.FIRST_MONDAYS[0][0]) {
            val dayOfYear1Q = GregorianCalendar(
                    YearData.FIRST_MONDAYS[0][0],
                    YearData.FIRST_MONDAYS[0][1] - 1,
                    YearData.FIRST_MONDAYS[0][2]
            )[Calendar.DAY_OF_YEAR]
            val dayOfYear2Q = GregorianCalendar(
                    YearData.FIRST_MONDAYS[1][0],
                    YearData.FIRST_MONDAYS[1][1] - 1,
                    YearData.FIRST_MONDAYS[1][2]
            )[Calendar.DAY_OF_YEAR]

            if (dayOfYear < dayOfYear2Q) {
                val week: Int = (dayOfYear - dayOfYear1Q) / 7
                if (week >= YearData.AMOUNTS_OF_WEEKS[0]) {
                    this.quarter = 1
                    this.week = 0
                } else {
                    this.quarter = 0
                    this.week = week
                }
            } else {
                val week: Int = (dayOfYear - dayOfYear2Q) / 7
                if (week >= YearData.AMOUNTS_OF_WEEKS[1]) {
                    this.quarter = 2
                    this.week = 0
                } else {
                    this.quarter = 1
                    this.week = week
                }
            }
        } else {
            val dayOfYear3Q = GregorianCalendar(
                    YearData.FIRST_MONDAYS[2][0],
                    YearData.FIRST_MONDAYS[2][1] - 1,
                    YearData.FIRST_MONDAYS[2][2]
            )[Calendar.DAY_OF_YEAR]
            val dayOfYear4Q = GregorianCalendar(
                    YearData.FIRST_MONDAYS[3][0],
                    YearData.FIRST_MONDAYS[3][1] - 1,
                    YearData.FIRST_MONDAYS[3][2]
            )[Calendar.DAY_OF_YEAR]

            if (dayOfYear < dayOfYear4Q) {
                val week: Int = (dayOfYear - dayOfYear3Q) / 7
                if (week >= YearData.AMOUNTS_OF_WEEKS[2]) {
                    this.quarter = 3
                    this.week = 0
                } else {
                    this.quarter = 2
                    this.week = week
                }
            } else {
                val week: Int = (dayOfYear - dayOfYear4Q) / 7
                if (week >= YearData.AMOUNTS_OF_WEEKS[3]) {
                    this.quarter = 3
                    this.week = YearData.AMOUNTS_OF_WEEKS[3] - 1
                } else {
                    this.quarter = 3
                    this.week = week
                }
            }
        }
    }
}