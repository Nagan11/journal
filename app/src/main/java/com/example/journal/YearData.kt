package com.example.journal

import java.util.*

class YearData {
    companion object {
        val QUARTER_IDS = intArrayOf(47, 55, 56, 57)
        val AMOUNTS_OF_WEEKS = intArrayOf(10, 7, 11, 9)
        val DAYS_IN_MONTH = IntArray(13)
        val FIRST_MONDAYS = arrayOf(
                intArrayOf(2021, 8, 31),
                intArrayOf(2021, 11, 8),
                intArrayOf(2022, 1, 10),
                intArrayOf(2022, 4, 4)
        )

        val NAME_OF_DAY = arrayOf(
                "Понедельник",
                "Вторник",
                "Среда",
                "Четверг",
                "Пятница",
                "Суббота",
                "Воскресенье"
        )
        val NAME_OF_MONTH = arrayOf(
                "Января",
                "Февраля",
                "Марта",
                "Апреля",
                "Мая",
                "Июня",
                "Июля",
                "Августа",
                "Сентября",
                "Октября",
                "Ноября",
                "Декабря"
        )

        init {
            fillDaysInMonth()
        }

        private fun isLeap(year: Int): Boolean {
            if (year % 400 == 0) return true
            if (year % 100 == 0) return false
            if (year % 4 == 0)   return true
            return false
        }
        private fun fillDaysInMonth() {
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

    }
}