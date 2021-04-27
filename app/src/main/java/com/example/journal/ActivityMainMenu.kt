package com.example.journal

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
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileReader

class ActivityMainMenu : AppCompatActivity() {
    private val ROOT_DIRECTORY: String by lazy { filesDir.toString() }

    private val currentUser: String? by lazy {
        val currentUserFile = File("$ROOT_DIRECTORY/current_user.txt")
        if (!currentUserFile.exists()) {
            null
        } else {
            val readUser = FileReader(currentUserFile)
            readUser.readLines()[0]
        }
    }
    private var userData = HashMap<String, String>()

    private val managerWeek: ManagerWeek by lazy { ManagerWeek(ROOT_DIRECTORY, userData["pupilUrl"]!!, this) }
    private val parserPage: ParserPage by lazy { ParserPage() }

    private val firstDay = StructDay(0, 0, 0)
    private val dayToPos = HashMap<StructDay, Int>()
    private val posToDay = HashMap<Int, StructDay>()

    private val pageParseMutex = Mutex()
    private val pageUpdateMutexes = ArrayList<ArrayList<Mutex>>().apply {
        for (i in 0..3) {
            add(ArrayList())
            for (j in 0 until YearData.AMOUNTS_OF_WEEKS[i]) get(i).add(Mutex())
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
                var day = firstDay.copy()
                if (pos < Int.MAX_VALUE / 2) {
                    day -= Int.MAX_VALUE / 2 - pos
                } else {
                    day += pos - Int.MAX_VALUE / 2
                }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        if (currentUser == null) startActivity(Intent(this, LoginActivity::class.java)) else readUserDataParams()
        userNameTextView.text = userData["realName"]

        firstDay.setCurrentDay()
        dayToPos[firstDay.copy()] = Int.MAX_VALUE / 2

        supportFragmentManager.beginTransaction()
                .hide(lpFragment)
                .hide(settingsFragment)
                .show(journalFragment)
                .commitNow()

        switcher.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> supportFragmentManager.beginTransaction().show(journalFragment).commitNow()
                    1 -> supportFragmentManager.beginTransaction().show(lpFragment).commitNow()
                    2 -> supportFragmentManager.beginTransaction().show(settingsFragment).commitNow()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> supportFragmentManager.beginTransaction().hide(journalFragment).commitNow()
                    1 -> supportFragmentManager.beginTransaction().hide(lpFragment).commitNow()
                    2 -> supportFragmentManager.beginTransaction().hide(settingsFragment).commitNow()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    journalRecyclerView.scrollToPosition(Int.MAX_VALUE / 2)
                }
            }
        })
        logOutButton.setOnClickListener {
            File("$ROOT_DIRECTORY/users/$currentUser.txt").delete()
            File("$ROOT_DIRECTORY/current_user.txt").delete()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        journalRecyclerView.layoutManager = LinearLayoutManager(this)
        journalRecyclerView.adapter = JournalRecyclerAdapter()
        journalRecyclerView.scrollToPosition(Int.MAX_VALUE / 2)
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