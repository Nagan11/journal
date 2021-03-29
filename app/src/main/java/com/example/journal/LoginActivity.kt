package com.example.journal

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Choreographer
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

fun Color24(hexString32: String): Color24 {
    return Color24(
            Integer.parseInt("${hexString32[2]}${hexString32[3]}", 16).toFloat(),
            Integer.parseInt("${hexString32[4]}${hexString32[5]}", 16).toFloat(),
            Integer.parseInt("${hexString32[6]}${hexString32[7]}", 16).toFloat()
    )
}
data class Color24(var r: Float, var g: Float, var b: Float) {
    fun toHexColor32(): String {
        var temp: String
        var s = "ff"

        temp = Integer.toHexString(r.roundToInt())
        s += if (temp.length == 1) "0$temp" else temp
        temp = Integer.toHexString(g.roundToInt())
        s += if (temp.length == 1) "0$temp" else temp
        temp = Integer.toHexString(b.roundToInt())
        s += if (temp.length == 1) "0$temp" else temp

        return s
    }

    operator fun plus(color: Color24): Color24  = Color24(r + color.r, g + color.g, b + color.b)
    operator fun minus(color: Color24): Color24 = Color24(r - color.r, g - color.g, b - color.b)
    operator fun times(f: Float): Color24       = Color24(r * f, g * f, b * f)
    operator fun div(f: Float): Color24         = Color24(r / f, g / f, b / f)
}

class LoginActivity : AppCompatActivity() {
    private val CONTEXT: Context = this
    private val ROOT_DIRECTORY: String by lazy { filesDir.toString() }

    private val BUTTON_NORMAL_TEXT_COLOR        by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonNormalText) }
    private val BUTTON_NORMAL_BACKGROUND_COLOR  by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonNormalBackground) }
    private val BUTTON_LOADING_TEXT_COLOR       by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonLoadingText) }
    private val BUTTON_LOADING_BACKGROUND_COLOR by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonLoadingBackground) }
    private val BUTTON_ERROR_TEXT_COLOR         by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonErrorText) }
    private val BUTTON_ERROR_BACKGROUND_COLOR   by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonErrorBackground) }
    private val BUTTON_SUCCESS_TEXT_COLOR       by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonSuccessText) }
    private val BUTTON_SUCCESS_BACKGROUND_COLOR by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonSuccessBackground) }

    private val ANIMATION_DURATION_FRAMES: Int = 16
    private val PAUSE_DURATION_FRAMES: Int     = 30

    private val loginManager by lazy { LoginManager(ROOT_DIRECTORY) }

    private var functionQueue = ArrayDeque<FramerateSynchronizedFunction>()

    interface FramerateSynchronizedFunction {
        var framesLeft: Int
        val function: (frameTimeNanos: Long) -> Unit
        val onFunctionStop: () -> Unit
        val launchFunction: () -> Unit
    }

    class AnimationPause(
            duration: Int, functionQueue: ArrayDeque<FramerateSynchronizedFunction>
    ) : FramerateSynchronizedFunction {
        override var framesLeft = duration

        override val function: (frameTimeNanos: Long) -> Unit = {
            framesLeft--
        }

        override val onFunctionStop: () -> Unit = {
            functionQueue.removeFirst()
            if (functionQueue.size > 0) functionQueue.first.launchFunction()
        }

        private var callback: (Long) -> Unit = {}
        override val launchFunction: () -> Unit = {
            callback = {
                function(it)
                framesLeft--
                if (framesLeft > 0) Choreographer.getInstance().postFrameCallback(callback) else onFunctionStop()
            }
            Choreographer.getInstance().postFrameCallback(callback)
        }
    }
    class GradientButtonAnimation(
            duration: Int, functionQueue: ArrayDeque<FramerateSynchronizedFunction>,
            button: Button, enabledAtEnd: Boolean,
            textColorEndInt: Int, bgColorEndInt: Int, newText: String
    ) : FramerateSynchronizedFunction {
        override var framesLeft = duration
        private val middleOfAnimation = duration / 2

        private lateinit var textColor: Color24
        private lateinit var bgColor: Color24

        private val textColorEnd = Color24(Integer.toHexString(textColorEndInt))
        private val bgColorEnd = Color24(Integer.toHexString(bgColorEndInt))
        private val middleColor by lazy { bgColor + ((bgColorEnd - bgColor) / 2f) }

        private val bgDiff by lazy {
            (bgColorEnd - bgColor) / duration.toFloat()
        }
        private val textEndMiddleDiff by lazy {
            (textColorEnd - middleColor) / middleOfAnimation.toFloat()
        }
        private val middleTextStartDiff by lazy {
            (middleColor - textColor) / middleOfAnimation.toFloat()
        }

        override val function: (frameTimeNanos: Long) -> Unit = {
            when {
                framesLeft > middleOfAnimation -> textColor += middleTextStartDiff
                framesLeft < middleOfAnimation -> textColor += textEndMiddleDiff
                else -> {
                    textColor = middleColor.copy()
                    button.text = newText
                }
            }
            bgColor += bgDiff
            button.setBackgroundColor(bgColor.toHexColor32().toLong(16).toInt())
            button.setTextColor(textColor.toHexColor32().toLong(16).toInt())
        }

        private var callback: (Long) -> Unit = {}
        override val launchFunction: () -> Unit = {
            textColor = Color24(Integer.toHexString(button.textColors.defaultColor))
            bgColor = Color24(Integer.toHexString((button.background as ColorDrawable).color))
            callback = {
                function(it)
                framesLeft--
                if (framesLeft > 0) Choreographer.getInstance().postFrameCallback(callback) else onFunctionStop()
            }
            Choreographer.getInstance().postFrameCallback(callback)
        }

        override val onFunctionStop: () -> Unit = {
            button.setBackgroundColor(bgColorEnd.toHexColor32().toLong(16).toInt())
            button.setTextColor(textColorEnd.toHexColor32().toLong(16).toInt())
            button.isEnabled = enabledAtEnd

            functionQueue.removeFirst()
            if (functionQueue.size > 0) functionQueue.first.launchFunction()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        resetLogInButton()

        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (logInButton.isEnabled) logInButtonOnClick(logInButton)
                true
            } else {
                false
            }
        }
    }

    override fun onBackPressed() {}

    fun logInButtonOnClick(view: View) {
        logInButton.isEnabled = false
        setButtonStateLoading()
        val view = this.currentFocus

        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        GlobalScope.launch {
            var loggedIn: Boolean? = null
            for (i in 1..3) {
                try {
                    loggedIn = loginManager.tryToLogin(username, password)
                    break
                } catch (e: Exception) {
                    if (i == 3) {
                        println("Login failed, $e")
                        setButtonStateError()
                        return@launch
                    }
                }
            }

            if (loggedIn!!) {
                setButtonStateSuccess()
                if (view != null) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }

                val nameParser = RealNameParser(loginManager.csrftoken!!, loginManager.sessionid!!, loginManager.pupilUrl!!)
                var realName: String? = null
                for (i in 1..3) {
                    try {
                        realName = nameParser.getRealName()
                        break
                    } catch (e: Exception) {}
                }
                loginManager.writeLoginDataToFiles(username, if (realName == null) username else realName!!)

                GlobalScope.launch {
                    while (functionQueue.size > 0) delay(50L)
                    startActivity(Intent(CONTEXT, MainMenuActivity::class.java))
                }
            } else {
                setButtonStateWrongData()
            }
        }
    }

    private fun resetLogInButton() {
        logInButton.setBackgroundColor(BUTTON_NORMAL_BACKGROUND_COLOR)
        logInButton.setTextColor(BUTTON_NORMAL_TEXT_COLOR)
        logInButton.setText("Войти")
        logInButton.isEnabled = true
    }

    private fun setButtonStateLoading() {
        functionQueue.addLast(
                GradientButtonAnimation(
                        ANIMATION_DURATION_FRAMES, functionQueue,
                        logInButton, false,
                        BUTTON_LOADING_TEXT_COLOR, BUTTON_LOADING_BACKGROUND_COLOR, "Вход..."
                )
        )
        runOnUiThread { if (functionQueue.size == 1) functionQueue.first.launchFunction() }
    }
    private fun setButtonStateWrongData() {
        functionQueue.addLast(
                GradientButtonAnimation(
                        ANIMATION_DURATION_FRAMES, functionQueue,
                        logInButton, false,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR, "Неверный логин/пароль"
                )
        )
        functionQueue.addLast(
                AnimationPause(PAUSE_DURATION_FRAMES * 3 / 2, functionQueue)
        )
        functionQueue.addLast(
                GradientButtonAnimation(
                        ANIMATION_DURATION_FRAMES, functionQueue,
                        logInButton, true,
                        BUTTON_NORMAL_TEXT_COLOR, BUTTON_NORMAL_BACKGROUND_COLOR, "Войти"
                )
        )
        runOnUiThread { if (functionQueue.size == 3) functionQueue.first.launchFunction() }
    }
    private fun setButtonStateError() {
        functionQueue.addLast(
                GradientButtonAnimation(
                        ANIMATION_DURATION_FRAMES, functionQueue,
                        logInButton, false,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR, "Ошибка"
                )
        )
        functionQueue.addLast(
                AnimationPause(PAUSE_DURATION_FRAMES, functionQueue)
        )
        functionQueue.addLast(
                GradientButtonAnimation(
                        ANIMATION_DURATION_FRAMES, functionQueue,
                        logInButton, true,
                        BUTTON_NORMAL_TEXT_COLOR, BUTTON_NORMAL_BACKGROUND_COLOR, "Войти"
                )
        )
        runOnUiThread { if (functionQueue.size == 3) functionQueue.first.launchFunction() }
    }
    private fun setButtonStateSuccess() {
        functionQueue.addLast(
                GradientButtonAnimation(
                        ANIMATION_DURATION_FRAMES, functionQueue,
                        logInButton, false,
                        BUTTON_SUCCESS_TEXT_COLOR, BUTTON_SUCCESS_BACKGROUND_COLOR, "Успешно"
                )
        )
        functionQueue.addLast(
                AnimationPause(PAUSE_DURATION_FRAMES, functionQueue)
        )
        runOnUiThread { if (functionQueue.size == 2) functionQueue.first.launchFunction() }
    }
}