package com.example.journal

import android.content.Context
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
    private val CONTEXT = this
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
    private var userDataParams = HashMap<String, String>()

    private val managerWeek: ManagerWeek by lazy { ManagerWeek(ROOT_DIRECTORY, userDataParams["pupilUrl"]!!) }
    private val parserPage: ParserPage by lazy { ParserPage() }
    private val pageParserMutex = Mutex()

    private val firstDay = StructDay(0, 0, 0)
    private val dayToPos = HashMap<StructDay, Int>()
    private val posToDay = HashMap<Int, StructDay>()

    inner class JournalRecyclerAdapter(context: Context) : RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder>() {
        private val context: Context = context

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val rootLayout: LinearLayout
            val dateView: TextView
            val weekDayNameView: TextView

            init {
                rootLayout = itemView.findViewById(R.id.dayRootLayout)
                dateView = itemView.findViewById(R.id.date)
                weekDayNameView = itemView.findViewById(R.id.weekDay)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.view_day, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            println(pos)
            GlobalScope.launch {
                if (posToDay[pos] == null) {
                    val a = firstDay.copy()
                    if (pos < Int.MAX_VALUE / 2) {
                        a.minus(Int.MAX_VALUE / 2 - pos)
                    } else {
                        a.plus(pos - Int.MAX_VALUE / 2)
                    }
                    posToDay[pos] = a.copy()
                }

                if (posToDay[pos]!!.quarter < 0 || posToDay[pos]!!.quarter > 3) {
                    holder.dateView.text = ""
                    holder.weekDayNameView.text = ""
                    return@launch
                }

                if (
                        managerWeek.datesData[posToDay[pos]!!.quarter]
                                [posToDay[pos]!!.week][posToDay[pos]!!.weekDay].dateString == ""
                ) {
                    managerWeek.datesData[posToDay[pos]!!.quarter][posToDay[pos]!!.week][posToDay[pos]!!.weekDay].yearDay = posToDay[pos]
                    managerWeek.datesData[posToDay[pos]!!.quarter][posToDay[pos]!!.week][posToDay[pos]!!.weekDay].generateStrings()
                }
                holder.dateView.text = managerWeek.datesData[posToDay[pos]!!.quarter][posToDay[pos]!!.week][posToDay[pos]!!.weekDay].dateString
                holder.weekDayNameView.text = managerWeek.datesData[posToDay[pos]!!.quarter][posToDay[pos]!!.week][posToDay[pos]!!.weekDay].weekDayString
            }
        }

        override fun getItemCount(): Int = Int.MAX_VALUE
    }

    suspend fun updatePage(quarter: Int, week: Int) {
        val downloader = PageDownloader(
                ROOT_DIRECTORY,
                userDataParams["sessionid"]!!,
                quarter, week,
                managerWeek.weekLinks[quarter][week]
        )
        if (!downloader.downloadPage()) return

        pageParserMutex.withLock {
            parserPage.parsePage(
                    "$ROOT_DIRECTORY/pages/q${quarter}w${week}.html",
                    "$ROOT_DIRECTORY/data/q${quarter}w${week}.txt"
            )
        }

        managerWeek.fillWeek("$ROOT_DIRECTORY/data/q${quarter}w${week}.txt", quarter, week, CONTEXT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        if (currentUser == null) startActivity(Intent(CONTEXT, LoginActivity::class.java)) else readUserDataParams()
        userNameTextView.text = userDataParams["realName"]

        firstDay.setCurrentDay()
        dayToPos[firstDay.copy()] = Int.MAX_VALUE / 2
        println("quarter -> ${firstDay.quarter}, week -> ${firstDay.week}, weekDay -> ${firstDay.weekDay}")

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

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        logOutButton.setOnClickListener {
            File("$ROOT_DIRECTORY/users/$currentUser.txt").delete()
            File("$ROOT_DIRECTORY/current_user.txt").delete()
            startActivity(Intent(CONTEXT, LoginActivity::class.java))
        }

        journalRecyclerView.layoutManager = LinearLayoutManager(CONTEXT)
        journalRecyclerView.adapter = JournalRecyclerAdapter(CONTEXT)
        journalRecyclerView.scrollToPosition(Int.MAX_VALUE / 2)
    }

    override fun onBackPressed() {}

    private fun readUserDataParams() {
        userDataParams.clear()
        val paramsReader = FileReader("$ROOT_DIRECTORY/users/$currentUser.txt")
        val paramsList = paramsReader.readLines()
        for (str in paramsList) {
            var it = 0
            var parameterName = ""
            var parameterValue = ""

            while (str[it] != ':') parameterName += str[it++]
            it += 2
            while (it < str.length) parameterValue += str[it++]

            userDataParams[parameterName] = parameterValue
        }
    }
}