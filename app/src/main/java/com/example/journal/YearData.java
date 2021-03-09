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
    private static final int[] QUARTER_IDS = new int[] {40, 42, 43, 44};
    private static final int[] AMOUNTS_OF_WEEKS = new int[] {9, 7, 11, 9};
    private static final int[] DAYS_IN_MONTH = new int[13];
    private static final int[][] FIRST_MONDAYS = new int[][] { // year, month, day
            {2020, 8, 31},
            {2020, 11, 9},
            {2021, 1, 11},
            {2021, 4, 5}
    };

    private static final String[] MONTH_NAMES = new String[] {
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
    private static final String[] WEEK_DAY_NAMES = new String[] {
            "Вс",
            "Пн",
            "Вт",
            "Ср",
            "Чт",
            "Пт",
            "Сб"
    };

    private static final ArrayList< ArrayList< ArrayList<String> > > DATES = new ArrayList<>(4);

    private static int currentQuarter = -1;
    private static int currentWeek = -1;

    static {
        fillDaysInMonth();
        fillDates();
        defineQuarterAndWeek();
    }

    // getters
    public static int               getCurrentQuarter() {
        return currentQuarter;
    }
    public static int               getCurrentWeek() {
        return currentWeek;
    }
    public static int               getAmountOfWeeks(int quarterNumber) {
        return AMOUNTS_OF_WEEKS[quarterNumber - 1];
    }                  // 1
    public static int               getDaysInMonth(int month) {
        return DAYS_IN_MONTH[month];
    } // 1
    public static int               getQuarterId(int quarterNumber) {
        return QUARTER_IDS[quarterNumber - 1];
    }                      // 1
    public static int[]             getFirstMonday(int quarterNumber) {
        return FIRST_MONDAYS[quarterNumber - 1];
    }                    // 1
    public static ArrayList<String> getDates(int quarterNumber, int weekNumber) {
        return DATES.get(quarterNumber - 1).get(weekNumber - 1);
    }          // 1

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

        DAYS_IN_MONTH[1] = 31;
        if (isLeap(secondYear)) {
            DAYS_IN_MONTH[2] = 29;
        } else {
            DAYS_IN_MONTH[2] = 28;
        }
        DAYS_IN_MONTH[3] = 31;
        DAYS_IN_MONTH[4] = 30;
        DAYS_IN_MONTH[5] = 31;
        DAYS_IN_MONTH[6] = 30;
        DAYS_IN_MONTH[7] = 31;
        DAYS_IN_MONTH[8] = 31;
        DAYS_IN_MONTH[9] = 30;
        DAYS_IN_MONTH[10] = 31;
        DAYS_IN_MONTH[11] = 30;
        DAYS_IN_MONTH[12] = 31;
    }
    private static void fillDates() {
        GregorianCalendar cal;
        for (int i = 0; i < 4; i++) {
            cal = new GregorianCalendar(FIRST_MONDAYS[i][0], FIRST_MONDAYS[i][1] - 1, FIRST_MONDAYS[i][2]);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            DATES.add(new ArrayList<ArrayList<String>>());
            for (int j = 0; j < AMOUNTS_OF_WEEKS[i]; j++) {
                DATES.get(i).add(new ArrayList<String>());
                for (int k = 0; k < 7; k++) {
                    String currentDate = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                    currentDate += " ";
                    currentDate += MONTH_NAMES[cal.get(Calendar.MONTH)];
                    currentDate += ", ";
                    currentDate += Integer.toString(cal.get(Calendar.YEAR));
                    currentDate += " (";
                    currentDate += WEEK_DAY_NAMES[cal.get(Calendar.DAY_OF_WEEK) - 1];
                    currentDate += ")";
                    DATES.get(i).get(j).add(currentDate);
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
        if (tempCalendar.get(Calendar.YEAR) < FIRST_MONDAYS[0][0] || tempCalendar.get(Calendar.YEAR) > FIRST_MONDAYS[3][0]) {
            currentQuarter = -1;
            currentWeek = -1;
        } else if (tempCalendar.get(Calendar.YEAR) == FIRST_MONDAYS[0][0]) {
            if (tempCalendar.get(Calendar.MONTH) < FIRST_MONDAYS[0][1] - 1) {
                currentQuarter = 1;
                currentWeek = 1;
            } else if (tempCalendar.get(Calendar.MONTH) == FIRST_MONDAYS[0][1] - 1) {
                currentQuarter = 1;
                currentWeek = 1;
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) > FIRST_MONDAYS[0][2] - 1) {
                    while (tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[0][2] - 1) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek++;
                        if (currentWeek > 100) {
                            currentWeek = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > FIRST_MONDAYS[0][1] - 1 && tempCalendar.get(Calendar.MONTH) < FIRST_MONDAYS[1][1] - 1 ) {
                currentQuarter = 1;
                currentWeek = 1;
                while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[0][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[0][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek++;
                    if (currentWeek > 100) {
                        currentWeek = 1;
                        return;
                    }
                }
                if (currentWeek > AMOUNTS_OF_WEEKS[0]) {
                    currentQuarter = 2;
                    currentWeek = 1;
                }
            } else if (tempCalendar.get(Calendar.MONTH) == FIRST_MONDAYS[1][1] - 1) {
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) < FIRST_MONDAYS[1][2] - 1) {
                    currentQuarter = 1;
                    currentWeek = 1;
                    while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[0][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[0][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek++;
                        if (currentWeek > 100) {
                            currentWeek = 1;
                            return;
                        }
                    }
                    if (currentWeek > AMOUNTS_OF_WEEKS[0]) {
                        currentQuarter = 2;
                        currentWeek = 1;
                    }
                } else {
                    currentQuarter = 2;
                    currentWeek = 1;
                    while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[1][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[1][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek++;
                        if (currentWeek > 100) {
                            currentWeek = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > FIRST_MONDAYS[1][1] - 1) {
                currentQuarter = 2;
                currentWeek = 1;
                while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[1][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[1][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek++;
                    if (currentWeek > 100) {
                        currentWeek = 1;
                        return;
                    }
                }
                if (currentWeek > AMOUNTS_OF_WEEKS[1]) {
                    currentQuarter = 3;
                    currentWeek = 1;
                }
            }
        } else {
            if (tempCalendar.get(Calendar.MONTH) < FIRST_MONDAYS[2][1] - 1) {
                currentQuarter = 3;
                currentWeek = 1;
            } else if (tempCalendar.get(Calendar.MONTH) == FIRST_MONDAYS[2][1] - 1) {
                currentQuarter = 3;
                currentWeek = 1;
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) > FIRST_MONDAYS[2][2] - 1) {
                    while (tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[2][2] - 1) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek++;
                        if (currentWeek > 100) {
                            currentWeek = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > FIRST_MONDAYS[2][1] - 1 && tempCalendar.get(Calendar.MONTH) < FIRST_MONDAYS[3][1] - 1 ) {
                currentQuarter = 3;
                currentWeek = 1;
                while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[2][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[2][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek++;
                    if (currentWeek > 100) {
                        currentWeek = 1;
                        return;
                    }
                }
                if (currentWeek > AMOUNTS_OF_WEEKS[2]) {
                    currentQuarter = 4;
                    currentWeek = 1;
                }
            } else if (tempCalendar.get(Calendar.MONTH) == FIRST_MONDAYS[3][1] - 1) {
                if (tempCalendar.get(Calendar.DAY_OF_MONTH) < FIRST_MONDAYS[3][2] - 1) {
                    currentQuarter = 1;
                    currentWeek = 1;
                    while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[2][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[2][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek++;
                        if (currentWeek > 100) {
                            currentWeek = 1;
                            return;
                        }
                    }
                    if (currentWeek > AMOUNTS_OF_WEEKS[2]) {
                        currentQuarter = 4;
                        currentWeek = 1;
                    }
                } else {
                    currentQuarter = 4;
                    currentWeek = 1;
                    while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[3][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[3][2]) {
                        tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                        currentWeek++;
                        if (currentWeek > 100) {
                            currentWeek = 1;
                            return;
                        }
                    }
                }
            } else if (tempCalendar.get(Calendar.MONTH) > FIRST_MONDAYS[3][1] - 1) {
                currentQuarter = 4;
                currentWeek = 1;
                while (tempCalendar.get(Calendar.MONTH) != FIRST_MONDAYS[3][1] - 1 || tempCalendar.get(Calendar.DAY_OF_MONTH) != FIRST_MONDAYS[3][2]) {
                    tempCalendar.roll(Calendar.WEEK_OF_YEAR, false);
                    currentWeek++;
                    if (currentWeek > 100) {
                        currentWeek = 1;
                        return;
                    }
                }
                if (currentWeek > AMOUNTS_OF_WEEKS[3]) {
                    currentQuarter = 4;
                    currentWeek = 1;
                }
            }
        }
    }
}
