package com.example.journal

import java.util.*

class DateGenerator {
    companion object {
        val MONTH_NAMES = arrayOf(
                "января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        val WEEK_DAY_NAMES = arrayOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        val DATES = ArrayList<ArrayList<ArrayList<String>>>()

        init {
            generateDates()
        }

        fun generateDates() {
            for (quarter in 0..3) {
                val cal = GregorianCalendar(YearData.FIRST_MONDAYS[quarter][0], YearData.FIRST_MONDAYS[quarter][1] - 1, YearData.FIRST_MONDAYS[quarter][2])
                cal.firstDayOfWeek = Calendar.MONDAY
                DATES.add(ArrayList())

                for (week in 0 until YearData.AMOUNTS_OF_WEEKS[quarter]) {
                    DATES[quarter].add(ArrayList())
                    for (k in 0..6) {
                        var currentDate = "${cal[Calendar.DAY_OF_MONTH]} " +
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