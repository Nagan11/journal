package com.example.journal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.fragment_journal.*
import kotlinx.android.synthetic.main.fragment_last_page.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ActivityMainMenu : AppCompatActivity() {
    private val ROOT_DIRECTORY: String by lazy { filesDir.toString() }

    private val currentUser: String? by lazy {
        val currentUserFile = File("$ROOT_DIRECTORY/current_user.txt")
        if (!currentUserFile.exists()) null else FileReader(currentUserFile).readLines()[0]
    }
    private var userData = HashMap<String, String>()

    private val managerWeek: ManagerWeek by lazy {
        if (userData["pupilUrl"] == null) startActivity(Intent(this, LoginActivity::class.java))
        ManagerWeek(ROOT_DIRECTORY, userData["pupilUrl"]!!, this)
    }
    private val parserPage: ParserPage by lazy { ParserPage() }

    private val firstDay = StructDay(0, 0, 0).apply { setCurrentDay() }
    private val dayToPos = HashMap<StructDay, Int>()
    private val posToDay = HashMap<Int, StructDay>()

    private val pageParseMutex = Mutex()
    private val pageUpdateMutexes = ArrayList<ArrayList<Mutex>>().apply {
        for (i in 0..3) {
            add(ArrayList())
            for (j in 0 until YearData.AMOUNTS_OF_WEEKS[i]) get(i).add(Mutex())
        }
    }

    private val datePickerDialog by lazy {
        val cal = Calendar.getInstance()
        DatePickerDialog(
                this,
                { _, year, month, day ->
                    var chosenDay = StructDay(0, 0, 0).apply {
                        setDayWithCalendar(GregorianCalendar(year, month, day))
                    }
                    var pos = Int.MAX_VALUE / 2
                    while (chosenDay < firstDay) {
                        chosenDay++
                        pos--
                    }
                    while (chosenDay > firstDay) {
                        chosenDay--
                        pos++
                    }
                    (journalRecyclerView.layoutManager as LinearLayoutManager?)
                            ?.scrollToPositionWithOffset(pos, 0)
                },
                cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
        ).apply {
            datePicker.firstDayOfWeek = Calendar.MONDAY
            datePicker.minDate = GregorianCalendar(
                    YearData.FIRST_MONDAYS[0][0],
                    YearData.FIRST_MONDAYS[0][1] - 1,
                    YearData.FIRST_MONDAYS[0][2]
            ).timeInMillis
            datePicker.maxDate = GregorianCalendar(
                    YearData.FIRST_MONDAYS[3][0],
                    YearData.FIRST_MONDAYS[3][1] - 1,
                    YearData.FIRST_MONDAYS[3][2]
            ).apply {
                roll(Calendar.DAY_OF_WEEK, 5)
                roll(Calendar.WEEK_OF_YEAR, YearData.AMOUNTS_OF_WEEKS[3] - 1)
            }.timeInMillis
        }
    }

    inner class JournalRecyclerAdapter : RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder>() {
        val activePostitons = HashSet<Int>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val dateView: TextView
            val weekDayNameView: TextView
            val dayRootLayout: LinearLayout

            init {
                dateView = itemView.findViewById(R.id.date)
                weekDayNameView = itemView.findViewById(R.id.weekDay)
                dayRootLayout = itemView.findViewById(R.id.dayRootLayout)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.view_day, parent, false
            ))
        }

        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            println("position -> ${pos - Int.MAX_VALUE / 2}")
            activePostitons.add(pos)
            if (posToDay[pos] == null) {
                val day = firstDay.copy().apply { plus(pos - Int.MAX_VALUE / 2) }
                posToDay[pos] = day
                dayToPos[day] = pos
            }

            val quarter = posToDay[pos]!!.quarter
            val week = posToDay[pos]!!.week
            val weekDay = posToDay[pos]!!.weekDay

            if (quarter < 0 || quarter > 3) {
                holder.dateView.text = ""
                holder.weekDayNameView.text = ""
                return
            }

            if (managerWeek.datesData[quarter][week][weekDay].dateString == "") {
                managerWeek.datesData[quarter][week][weekDay].yearDay = posToDay[pos]
                managerWeek.datesData[quarter][week][weekDay].generateStrings()
            }
            holder.dateView.text = managerWeek.datesData[quarter][week][weekDay].dateString
            holder.weekDayNameView.text = managerWeek.datesData[quarter][week][weekDay].weekDayString

            GlobalScope.launch {
                pageUpdateMutexes[quarter][week].withLock {
                    if (managerWeek.weekStates[quarter][week] == WeekState.EMPTY) {
                        managerWeek.weekStates[quarter][week] = WeekState.PROCESSING
                        updatePage(quarter, week)
                    }
                }

                if (activePostitons.contains(pos)) {
                    runOnUiThread {
                        while (holder.dayRootLayout.childCount > 1) holder.dayRootLayout.removeViewAt(holder.dayRootLayout.childCount - 1)
                        for (i in 0 until managerWeek.lessonsViews[quarter][week][weekDay].size) {
                            managerWeek.lessonsViews[quarter][week][weekDay][i].let {
                                if (it.parent != null) (it.parent as LinearLayout).removeView(it)
                                holder.dayRootLayout.addView(it)
                            }
                        }
                    }
                }
            }
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            println("${holder.adapterPosition - Int.MAX_VALUE / 2} recycled")
            while (holder.dayRootLayout.childCount > 1) holder.dayRootLayout.removeViewAt(holder.dayRootLayout.childCount - 1)
            activePostitons.remove(holder.adapterPosition)
        }

        override fun getItemCount(): Int = Int.MAX_VALUE
    }

    suspend fun updatePage(quarter: Int, week: Int) {
        val downloader = PageDownloader(
                ROOT_DIRECTORY,
                userData["sessionid"]!!,
                quarter, week,
                managerWeek.weekLinks[quarter][week]
        )

        if (!downloader.downloadPage()) return
        pageParseMutex.withLock {
            parserPage.parsePage(
                    "$ROOT_DIRECTORY/pages/q${quarter}w${week}.html",
                    "$ROOT_DIRECTORY/data/q${quarter}w${week}.txt"
            )
        }

        managerWeek.createLessonViews("$ROOT_DIRECTORY/data/q${quarter}w${week}.txt", quarter, week, layoutInflater)
        managerWeek.weekStates[quarter][week] = WeekState.READY
    }

    private fun updateLastPage() {
        val downloader = PageDownloader(
                ROOT_DIRECTORY,
                userData["sessionid"]!!,
                4, 0,
                managerWeek.weekLinks[4][0]
        )
        val lpParser = ParserLastPage()

        if (!downloader.downloadPage()) return
        lpParser.parsePage(
                "$ROOT_DIRECTORY/pages/q4w0.html",
                "$ROOT_DIRECTORY/data/lp.txt"
        )

        var index = 0
        var temp: LessonYearMarks
        val text = FileReader("$ROOT_DIRECTORY/data/lp.txt").readText()
        while (index < text.length) {
            temp = LessonYearMarks("", "", "", "", "", "")
            try {
                while (text[index] != '>') temp.lesson += text[index++]
                index++
                while (text[index] != '>') temp.mark1Q += text[index++]
                index++
                while (text[index] != '>') temp.mark2Q += text[index++]
                index++
                while (text[index] != '>') temp.mark3Q += text[index++]
                index++
                while (text[index] != '>') temp.mark4Q += text[index++]
                index++
                while (text[index] != '>') temp.markYear += text[index++]
                index++
            } catch (e: Exception) { break }
            (lastPageRecyclerView.adapter as AdapterLastPage).data.add(temp)
        }
        runOnUiThread { lastPageRecyclerView.adapter?.notifyDataSetChanged() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        readUserDataParams()
        setContentView(R.layout.activity_main_menu)

        switcher.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        supportFragmentManager.beginTransaction().show(journalFragment).commitNow()
                        datePickButton.show()
                    }
                    1 -> supportFragmentManager.beginTransaction().show(lpFragment).commitNow()
                    2 -> supportFragmentManager.beginTransaction().show(settingsFragment).commitNow()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        supportFragmentManager.beginTransaction().hide(journalFragment).commitNow()
                        datePickButton.hide()
                    }
                    1 -> supportFragmentManager.beginTransaction().hide(lpFragment).commitNow()
                    2 -> supportFragmentManager.beginTransaction().hide(settingsFragment).commitNow()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    journalRecyclerView.stopScroll()
                    (journalRecyclerView.layoutManager as LinearLayoutManager?)
                            ?.scrollToPositionWithOffset(Int.MAX_VALUE / 2, 0)
                }
            }
        })
        logOutButton.setOnClickListener {
            File("$ROOT_DIRECTORY/users/$currentUser.txt").delete()
            File("$ROOT_DIRECTORY/current_user.txt").delete()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        datePickButton.setOnClickListener {
            datePickerDialog.show()
        }

        userNameTextView.text = userData["realName"]

        dayToPos[firstDay.copy()] = Int.MAX_VALUE / 2

        supportFragmentManager.beginTransaction()
                .hide(lpFragment)
                .hide(settingsFragment)
                .show(journalFragment)
                .commitNow()

        journalRecyclerView.layoutManager = LinearLayoutManager(this)
        journalRecyclerView.adapter = JournalRecyclerAdapter()
        journalRecyclerView.scrollToPosition(Int.MAX_VALUE / 2)

        lastPageRecyclerView.layoutManager = LinearLayoutManager(this)
        lastPageRecyclerView.adapter = AdapterLastPage()
        GlobalScope.launch { updateLastPage() }
    }

    override fun onBackPressed() {}

    private fun readUserDataParams() {
        userData.clear()
        val paramsReader = FileReader("$ROOT_DIRECTORY/users/$currentUser.txt")
        val paramsList = paramsReader.readLines()
        for (str in paramsList) {
            var it = 0
            var parameterName = ""
            var parameterValue = ""

            while (str[it] != ':') parameterName += str[it++]
            it += 2
            while (it < str.length) parameterValue += str[it++]

            userData[parameterName] = parameterValue
        }
    }
}