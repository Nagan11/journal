package com.example.journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main_menu.*
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
    private val pageParser: PageParser by lazy { PageParser(ROOT_DIRECTORY) }
    private val curQuarterWeek = CurrentQuarterWeek()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        supportFragmentManager.beginTransaction().hide(journalFragment).commitNow()

        if (currentUser == null) startActivity(Intent(CONTEXT, LoginActivity::class.java)) else readUserDataParams()
        userNameTextView.text = userDataParams["realName"]

        for (str in weekManager.weekLinks) {
            println(str)
        }
    }

    override fun onBackPressed() {
        if (!journalFragment.isHidden) {
            journalButton.isEnabled = true
            supportFragmentManager.beginTransaction().hide(journalFragment).commitNow()
        }
    }
    fun logOutButtonOnClick(view: View) {
        File("$ROOT_DIRECTORY/users/$currentUser.txt").delete()
        File("$ROOT_DIRECTORY/current_user.txt").delete()
        startActivity(Intent(CONTEXT, LoginActivity::class.java))
    }
    fun journalButtonOnClick(view: View) {
        journalButton.isEnabled = false
        supportFragmentManager.beginTransaction().show(journalFragment).commitNow()
        findViewById<View>(R.id.journalFragment).translationZ = 10f
    }
    fun lpButtonOnClick(view: View) {

    }
    fun subjectsButtonOnClick(view: View) {

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