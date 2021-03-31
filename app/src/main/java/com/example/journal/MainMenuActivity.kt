package com.example.journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_main_menu.*
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
    private val curQuarterWeek = CurrentQuarterWeekDefiner()

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


    }

    override fun onBackPressed() {}
    fun journalButtonOnClick(view: View) {
        supportFragmentManager.beginTransaction()
                .hide(lpFragment)
                .hide(settingsFragment)
                .show(journalFragment)
                .commitNow()
        println("journal clicked")
    }
    fun lpButtonOnClick(view: View) {
        supportFragmentManager.beginTransaction()
                .hide(journalFragment)
                .hide(settingsFragment)
                .show(lpFragment)
                .commitNow()
        println("lp clicked")
    }
    fun settingsButtonOnClick(view: View) {
        supportFragmentManager.beginTransaction()
                .hide(journalFragment)
                .hide(lpFragment)
                .show(settingsFragment)
                .commitNow()
        println("settings clicked")
    }

    fun readUserDataParams() {
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