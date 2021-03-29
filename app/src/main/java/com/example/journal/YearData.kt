package com.example.journal

import java.util.*

class YearData {
    companion object {
        val QUARTER_IDS = intArrayOf(40, 42, 43, 44)
        val AMOUNTS_OF_WEEKS = intArrayOf(9, 7, 11, 9)
        val DAYS_IN_MONTH = IntArray(13)
        val FIRST_MONDAYS = arrayOf(
                intArrayOf(2020, 8, 31),
                intArrayOf(2020, 11, 9),
                intArrayOf(2021, 1, 11),
                intArrayOf(2021, 4, 5)
        )

        val MONTH_NAMES = arrayOf(
                "января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        val WEEK_DAY_NAMES = arrayOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")

        val DATES = ArrayList<ArrayList<ArrayList<String>>>()

        init {
            fillDaysInMonth()
            fillDates()
        }

        fun isLeap(year: Int): Boolean {
            if (year % 400 == 0) return true
            if (year % 100 == 0) return false
            if (year % 4 == 0)   return true
            return false
        }
        fun fillDaysInMonth() {
            var secondYear = Calendar.getInstance()[Calendar.YEAR]
            if (Calendar.getInstance()[Calendar.MONTH] > 7) secondYear++

            DAYS_IN_MONTH[1] = 31
            DAYS_IN_MONTH[2] = if (isLeap(secondYear)) 29 else 28
            DAYS_IN_MONTH[3] = 31
            DAYS_IN_MONTH[4] = 30
            DAYS_IN_MONTH[5] = 31
            DAYS_IN_MONTH[6] = 30
            DAYS_IN_MONTH[7] = 31
            DAYS_IN_MONTH[8] = 31
            DAYS_IN_MONTH[9] = 30
            DAYS_IN_MONTH[10] = 31
            DAYS_IN_MONTH[11] = 30
            DAYS_IN_MONTH[12] = 31
        }
        fun fillDates() {
            for (quarter in 0..3) {
                val cal = GregorianCalendar(FIRST_MONDAYS[quarter][0], FIRST_MONDAYS[quarter][1] - 1, FIRST_MONDAYS[quarter][2])
                cal.firstDayOfWeek = Calendar.MONDAY
                DATES.add(ArrayList())

                for (week in 0 until AMOUNTS_OF_WEEKS[quarter]) {
                    DATES[quarter].add(ArrayList())
                    for (k in 0..6) {
                        var currentDate =
                                "${cal[Calendar.DAY_OF_MONTH]} " +
                                "${MONTH_NAMES[cal[Calendar.MONTH]]}, " +
                                "${cal[Calendar.YEAR]} " +
                                "(${WEEK_DAY_NAMES[cal[Calendar.DAY_OF_WEEK] - 1]})"

                        DATES[quarter][week].add(currentDate)
                        cal.roll(Calendar.DAY_OF_WEEK, true)
                    }
                    cal.roll(Calendar.WEEK_OF_YEAR, true)
                }
            }
        }
    }
}