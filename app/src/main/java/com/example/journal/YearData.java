/**
 * Class contains constant information that is mutual for all users.
 * Class also defines current quarter and week.
 *
 * The information is used for:
 *  - Making URL links to HTML pages (first group of properties)
 *  - Building layouts (second group of properties)
 *  - Defining current quarter and week
 */

package com.example.journal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;



public class YearData {
    private static int[] quarterIds_ = new int[] {40, 42, 43, 44};
    private static int[] amountsOfWeeks_ = new int[] {9, 7, 11, 9};
    private static int[] daysInMonth_ = new int[13];
    private static int[][] firstMondays_ = new int[][] { // year, month, day
            {2020, 8, 31},
            {2020, 11, 9},
            {2021, 1, 11},
            {2021, 4, 5}
    };

    private static String[] monthNames_ = new String[] {
            "января",
            "февраля",
            "марта",
            "апреля",
            "мая",
            "июня",
            "июля",
            "августа",
            "сентября",
            "октября",
            "ноября",
            "декабря"
    };
    private static String[] weekDayNames_ = new String[] {
            "Вс",
            "Пн",
            "Вт",
            "Ср",
            "Чт",
            "Пт",
            "Сб"
    };

    private static ArrayList< ArrayList< ArrayList<String> > > dates_ = new ArrayList<>(4);

    private static int currentQuarter_ = -1;
    private static int currentWeek_ = -1;

    static {
        fillDaysInMonth();
        fillDates();
        defineQuarterAndWeek();
    }

    // getters
    public static int               getCurrentQuarter() {
        return currentQuarter_;
    }
    public static int               getCurrentWeek() {
        return currentWeek_;
    }
    public static int               getAmountOfWeeks(int quarterNumber) {
        return amountsOfWeeks_[quarterNumber - 1];
    }                 // 1
    public static int               getDaysInMonth(int month) {
        return daysInMonth_[month];
    } // 1
    public static int               getQuarterId(int quarterNumber) {
        return quarterIds_[quarterNumber - 1];
    }                     // 1
    public static int[]             getFirstMonday(int quarterNumber) {
        return firstMondays_[quarterNumber - 1];
    }                   // 1
    public static ArrayList<String> getDates(int quarterNumber, int weekNumber) {
        return dates_.get(quarterNumber - 1).get(weekNumber - 1);
    }         // 1

    private static boolean isLeap(int a) {
        if (a % 400 == 0) {
            return true;
        }
        else if (a % 400 != 0 && a % 100 == 0) {
            return false;
        }
        else if (a % 400 != 0 && a % 100 != 0 && a % 4 == 0) {
            return true;
        }
        else {
            return false;
        }
    }
    private static void fillDaysInMonth() {
        int secondYear = Calendar.getInstance().get(Calendar.YEAR);
        if (Calendar.getInstance().get(Calendar.MONTH) > 7) {
            secondYear++;
        }

        daysInMonth_[1] = 31;
        if (isLeap(secondYear)) {
            daysInMonth_[2] = 29;
        } else {
            daysInMonth_[2] = 28;
        }
        daysInMonth_[3] = 31;
        daysInMonth_[4] = 30;
        daysInMonth_[5] = 31;
        daysInMonth_[6] = 30;
        daysInMonth_[7] = 31;
        daysInMonth_[8] = 31;
        daysInMonth_[9] = 30;
        daysInMonth_[10] = 31;
        daysInMonth_[11] = 30;
        daysInMonth_[12] = 31;
    }
    private static void fillDates() {
        GregorianCalendar cal;
        for (int i = 0; i < 4; i++) {
            cal = new GregorianCalendar(firstMondays_[i][0], firstMondays_[i][1] - 1, firstMondays_[i][2]);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            dates_.add(new ArrayList<ArrayList<String>>());
            for (int j = 0; j < amountsOfWeeks_[i]; j++) {
                dates_.get(i).add(new ArrayList<String>());
                for (int k = 0; k < 7; k++) {
                    String currentDate = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                    currentDate += " ";
                    currentDate += monthNames_[cal.get(Calendar.MONTH)];
                    currentDate += ", ";
                    currentDate += Integer.toString(cal.get(Calendar.YEAR));
                    currentDate += " (";
                    currentDate += weekDayNames_[cal.get(Calendar.DAY_OF_WEEK) - 1];
                    currentDate += ")";
                    dates_.get(i).get(j).add(currentDate);
                    cal.roll(Calendar.DAY_OF_WEEK, true);
                }
                cal.roll(Calendar.WEEK_OF_YEAR, true);
            }
        }
    }
    private static void defineQuarterAndWeek() {
        GregorianCalendar tempCalendar = new GregorianCalendar();
        tempCalendar.set(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if (tempCalendar.get(Calendar.YEAR) < firstMondays_[0][0] || tempCalendar.get(Calendar.YEAR) > firstMondays_[3][0]) {
            currentQuarter_ = -1;
            currentWeek_ = -1;
        } else if (tempCalendar.get(Calendar.YEAR) == firstMondays_[0][0]) {
            if (tempCalendar.get(Calendar.MONTH) < firstMondays_[0][1] - 1) {
                currentQuarter_ = 1;
                currentWeek_ = 1;
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[0][1] - 1) {
                currentQuarter_ = 1;
                currentWeek_ = 1;
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) > firstMondays_[0][2] - 1) {
                    while (tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[0][2] - 1) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[0][1] - 1 && tempCalendar.get(Calendar.MONTH) < firstMondays_[1][1] - 1 ) {
                currentQuarter_ = 1;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[0][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[0][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[0]) {
                    currentQuarter_ = 2;
                    currentWeek_ = 1;
                }
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[1][1] - 1) {
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) < firstMondays_[1][2] - 1) {
                    currentQuarter_ = 1;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[0][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[0][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                    if (currentWeek_ > amountsOfWeeks_[0]) {
                        currentQuarter_ = 2;
                        currentWeek_ = 1;
                    }
                } else {
                    currentQuarter_ = 2;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[1][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[1][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[1][1] - 1) {
                currentQuarter_ = 2;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[1][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[1][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[1]) {
                    currentQuarter_ = 3;
                    currentWeek_ = 1;
                }
            }
        } else {
            if (tempCalendar.get(Calendar.MONTH) < firstMondays_[2][1] - 1) {
                currentQuarter_ = 3;
                currentWeek_ = 1;
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[2][1] - 1) {
                currentQuarter_ = 3;
                currentWeek_ = 1;
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) > firstMondays_[2][2] - 1) {
                    while (tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[2][2] - 1) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[2][1] - 1 && tempCalendar.get(Calendar.MONTH) < firstMondays_[3][1] - 1 ) {
                currentQuarter_ = 3;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[2][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[2][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[2]) {
                    currentQuarter_ = 4;
                    currentWeek_ = 1;
                }
            } else if (tempCalendar.get(Calendar.MONTH) == firstMondays_[3][1] - 1) {
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) < firstMondays_[3][2] - 1) {
                    currentQuarter_ = 1;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[2][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[2][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                    if (currentWeek_ > amountsOfWeeks_[2]) {
                        currentQuarter_ = 4;
                        currentWeek_ = 1;
                    }
                } else {
                    currentQuarter_ = 4;
                    currentWeek_ = 1;
                    while (tempCalendar.get(Calendar.MONTH) != firstMondays_[3][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[3][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek_++;
                        if (currentWeek_ > 100) {
                            currentWeek_ = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > firstMondays_[3][1] - 1) {
                currentQuarter_ = 4;
                currentWeek_ = 1;
                while (tempCalendar.get(Calendar.MONTH) != firstMondays_[3][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != firstMondays_[3][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek_++;
                    if (currentWeek_ > 100) {
                        currentWeek_ = 1;
                        return;
                    }
                }
                if (currentWeek_ > amountsOfWeeks_[3]) {
                    currentQuarter_ = 4;
                    currentWeek_ = 1;
                }
            }
        }
    }
}
