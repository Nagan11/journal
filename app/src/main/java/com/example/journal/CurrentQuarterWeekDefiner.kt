package com.example.journal

import java.util.*

class CurrentQuarterWeekDefiner {
    var quarter: Int = -1
    var week: Int = -1

    init {
        define()
        decrement()
    }

    fun define() {
        val currentCalendar = GregorianCalendar()
        currentCalendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

        if (currentCalendar[Calendar.YEAR] < YearData.FIRST_MONDAYS[0][0] || currentCalendar[Calendar.YEAR] > YearData.FIRST_MONDAYS[3][0]) {
            quarter = -1
            week = -1
            return
        }

        if (currentCalendar[Calendar.YEAR] == YearData.FIRST_MONDAYS[0][0]) {
            var cal = currentCalendar.clone() as GregorianCalendar
            var dayOfYear = GregorianCalendar(
                    YearData.FIRST_MONDAYS[0][0],
                    YearData.FIRST_MONDAYS[0][1] - 1,
                    YearData.FIRST_MONDAYS[0][2]
            )[Calendar.DAY_OF_YEAR]

            quarter = 1
            week = 1
            while (cal[Calendar.DAY_OF_YEAR] > dayOfYear && week <= YearData.AMOUNTS_OF_WEEKS[0]) {
                week++
                cal.roll(Calendar.WEEK_OF_YEAR, false)
            }
            if (week > YearData.AMOUNTS_OF_WEEKS[0]) {
                cal = currentCalendar.clone() as GregorianCalendar
                dayOfYear = GregorianCalendar(
                        YearData.FIRST_MONDAYS[1][0],
                        YearData.FIRST_MONDAYS[1][1] - 1,
                        YearData.FIRST_MONDAYS[1][2]
                )[Calendar.DAY_OF_YEAR]

                quarter = 2
                week = 1
                while (cal[Calendar.DAY_OF_YEAR] > dayOfYear && week <= YearData.AMOUNTS_OF_WEEKS[1]) {
                    week++
                    cal.roll(Calendar.WEEK_OF_YEAR, false)
                }
            }
        } else {
            var cal = currentCalendar.clone() as GregorianCalendar
            var dayOfYear = GregorianCalendar(
                    YearData.FIRST_MONDAYS[2][0],
                    YearData.FIRST_MONDAYS[2][1] - 1,
                    YearData.FIRST_MONDAYS[2][2]
            )[Calendar.DAY_OF_YEAR]

            quarter = 3
            week = 1
            while (cal[Calendar.DAY_OF_YEAR] > dayOfYear && week <= YearData.AMOUNTS_OF_WEEKS[2]) {
                week++
                cal.roll(Calendar.WEEK_OF_YEAR, false)
            }
            if (week > YearData.AMOUNTS_OF_WEEKS[2]) {
                cal = currentCalendar.clone() as GregorianCalendar
                dayOfYear = GregorianCalendar(
                        YearData.FIRST_MONDAYS[0][0],
                        YearData.FIRST_MONDAYS[0][1] - 1,
                        YearData.FIRST_MONDAYS[0][2]
                )[Calendar.DAY_OF_YEAR]

                quarter = 4
                week = 1
                while (cal[Calendar.DAY_OF_YEAR] > dayOfYear) {
                    week++
                    cal.roll(Calendar.WEEK_OF_YEAR, false)
                    if (week > YearData.AMOUNTS_OF_WEEKS[3]) {
                        quarter = 4
                        week = YearData.AMOUNTS_OF_WEEKS[3]
                        return
                    }
                }
            }
        }
    }
    fun decrement() {
        quarter--
        week--
    }
}