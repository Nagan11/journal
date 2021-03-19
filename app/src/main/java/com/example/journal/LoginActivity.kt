package com.example.journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*

import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    val CONTEXT: Context = this
    val ROOT_DIRECTORY: String by lazy { filesDir.toString() }

    val LOGIN_MANAGER by lazy { LoginManager(ROOT_DIRECTORY) }

    val BUTTON_TEXT_COLOR       by lazy { ContextCompat.getColor(CONTEXT, R.color.loginButtonText) }
    val BUTTON_BACKGROUND_COLOR by lazy { ContextCompat.getColor(CONTEXT, R.color.loginButtonBackground) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setLoginButtonUsualState()
    }

    override fun onBackPressed() {}

    fun loginButtonOnClick(view: View) {
        setLoginButtonLoadingState()
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        GlobalScope.launch {
            var loggedIn: Boolean? = null
            for (i in 1..3) {
                try {
                    loggedIn = LOGIN_MANAGER.tryToLogin(username, password)
                    break
                } catch (e: Exception) {
                    if (i == 3) {
                        println("Login failed, $e")
                        runOnUiThread {
                            Toast.makeText(CONTEXT, "Ошибка", Toast.LENGTH_SHORT).show()
                        }
                        setLoginButtonUsualState()
                        return@launch
                    }
                }
            }

            if (loggedIn!!) {
                val nameParser = RealNameParser(LOGIN_MANAGER.csrftoken!!, LOGIN_MANAGER.sessionid!!, LOGIN_MANAGER.pupilUrl!!)
                var realName: String? = null
                for (i in 1..3) {
                    try {
                        realName = nameParser.getRealName()
                        break
                    } catch (e: Exception) {}
                }

                LOGIN_MANAGER.writeLoginDataToFiles(username, if (realName == null) username else realName!!)

                startActivity(Intent(CONTEXT, MainMenuActivity::class.java))
            } else {
                runOnUiThread {
                    Toast.makeText(CONTEXT, "Неправильный логин или пароль", Toast.LENGTH_SHORT).show()
                }
                setLoginButtonUsualState()
            }
        }
    }

    fun setLoginButtonLoadingState() {
        runOnUiThread {
            logInButton.setBackgroundColor(BUTTON_TEXT_COLOR)
            logInButton.setTextColor(BUTTON_BACKGROUND_COLOR)
            logInButton.isEnabled = false
        }
    }

    fun setLoginButtonUsualState() {
        runOnUiThread {
            logInButton.setBackgroundColor(BUTTON_BACKGROUND_COLOR)
            logInButton.setTextColor(BUTTON_TEXT_COLOR)
            logInButton.isEnabled = true
        }
    }
}