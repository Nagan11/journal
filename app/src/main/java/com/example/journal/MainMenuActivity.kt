package com.example.journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.fragment_journal.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileReader

class MainMenuActivity : AppCompatActivity() {
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

    private val weekManager: WeekManager by lazy { WeekManager(ROOT_DIRECTORY, userDataParams["pupilUrl"]!!) }
    private val pageParser: PageParser by lazy { PageParser() }
    private val pageParserMutex = Mutex()
    private val startDay = StructDay(0, 0, 0)

    inner class JournalRecyclerAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//        companion object {
//            const val TYPE_DATE = 0
//            const val TYPE_LESSON = 1
//        }

        private val context: Context = context

        private inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val dateView: TextView
            val weekDayNameView: TextView

            fun bind() {

            }

            init {
                dateView = itemView.findViewById(R.id.date)
                weekDayNameView = itemView.findViewById(R.id.weekDay)
            }
        }

        private inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val lessonView: TextView
            val markView: TextView
            val hometaskView: TextView

            fun bind() {

            }

            init {
                lessonView = itemView.findViewById(R.id.lesson)
                markView = itemView.findViewById(R.id.mark)
                hometaskView = itemView.findViewById(R.id.hometask)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> DateViewHolder(
                        LayoutInflater.from(parent.context).inflate(
                                R.layout.view_date, parent, false
                        )
                )
                else -> LessonViewHolder(
                        LayoutInflater.from(parent.context).inflate(
                                R.layout.view_lesson, parent, false
                        )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            
        }

        override fun getItemCount(): Int = Int.MAX_VALUE
    }

    suspend fun updatePage(quarter: Int, week: Int) {
        val downloader = PageDownloader(
                ROOT_DIRECTORY,
                userDataParams["sessionid"]!!,
                quarter, week,
                weekManager.weekLinks[quarter][week]
        )
        if (!downloader.downloadPage()) return

        pageParserMutex.withLock {
            pageParser.parsePage(
                    "$ROOT_DIRECTORY/pages/q${quarter}w${week}.html",
                    "$ROOT_DIRECTORY/data/q${quarter}w${week}.txt"
            )
        }

        weekManager.fillWeek("$ROOT_DIRECTORY/data/q${quarter}w${week}.txt", quarter, week, CONTEXT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        if (currentUser == null) startActivity(Intent(CONTEXT, LoginActivity::class.java)) else readUserDataParams()
        userNameTextView.text = userDataParams["realName"]

        startDay.setCurrentDay()
        println("quarter -> ${startDay.quarter}, week -> ${startDay.week}, weekDay -> ${startDay.weekDay}")

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
    }

    override fun onBackPressed() {}
    fun journalButtonOnClick(view: View) {
        supportFragmentManager.beginTransaction()
                .hide(lpFragment)
                .hide(settingsFragment)
                .show(journalFragment)
                .commitNow()
    }
    fun lpButtonOnClick(view: View) {
        supportFragmentManager.beginTransaction()
                .hide(journalFragment)
                .hide(settingsFragment)
                .show(lpFragment)
                .commitNow()
    }
    fun settingsButtonOnClick(view: View) {
        supportFragmentManager.beginTransaction()
                .hide(journalFragment)
                .hide(lpFragment)
                .show(settingsFragment)
                .commitNow()
    }

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

            userDataParams.set(parameterName, parameterValue)
        }
    }
}