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
            _duration: Int, _functionQueue: ArrayDeque<FramerateSynchronizedFunction>
    ) : FramerateSynchronizedFunction {
        override var framesLeft = _duration

        override val function: (frameTimeNanos: Long) -> Unit = {
            framesLeft--
        }

        override val onFunctionStop: () -> Unit = {
            _functionQueue.removeFirst()
            if (_functionQueue.size > 0) _functionQueue.first.launchFunction()
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
            _duration: Int, _functionQueue: ArrayDeque<FramerateSynchronizedFunction>,
            _button: Button, _enabledAtEnd: Boolean,
            _textColorEndInt: Int, _bgColorEndInt: Int, _newText: String
    ) : FramerateSynchronizedFunction {
        override var framesLeft = _duration
        val middleOfAnimation = _duration / 2

        private lateinit var textColor: Color24
        private lateinit var bgColor: Color24
        private val textColorEnd = Color24(Integer.toHexString(_textColorEndInt))
        private val bgColorEnd = Color24(Integer.toHexString(_bgColorEndInt))
        private val middleColor by lazy {
            Color24(
                    bgColor.r + ((bgColorEnd.r - bgColor.r) / 2),
                    bgColor.g + ((bgColorEnd.g - bgColor.g) / 2),
                    bgColor.b + ((bgColorEnd.b - bgColor.b) / 2)
            )
        }

        private val rBgDiff by lazy { (bgColorEnd.r - bgColor.r) / _duration }
        private val gBgDiff by lazy { (bgColorEnd.g - bgColor.g) / _duration }
        private val bBgDiff by lazy { (bgColorEnd.b - bgColor.b) / _duration }

        private val rTextEndMiddleDiff by lazy { (textColorEnd.r - middleColor.r) / middleOfAnimation }
        private val gTextEndMiddleDiff by lazy { (textColorEnd.g - middleColor.g) / middleOfAnimation }
        private val bTextEndMiddleDiff by lazy { (textColorEnd.b - middleColor.b) / middleOfAnimation }

        private val rMiddleTextStartDiff by lazy { (middleColor.r - textColor.r) / middleOfAnimation }
        private val gMiddleTextStartDiff by lazy { (middleColor.g - textColor.g) / middleOfAnimation }
        private val bMiddleTextStartDiff by lazy { (middleColor.b - textColor.b) / middleOfAnimation }

        override val function: (frameTimeNanos: Long) -> Unit = {
            if (framesLeft > middleOfAnimation) {
                textColor.r += rMiddleTextStartDiff
                textColor.g += gMiddleTextStartDiff
                textColor.b += bMiddleTextStartDiff
                bgColor.r += rBgDiff
                bgColor.g += gBgDiff
                bgColor.b += bBgDiff
            } else if (framesLeft == middleOfAnimation) {
                textColor = middleColor.copy()
                bgColor = middleColor.copy()
                _button.text = _newText
            } else if (framesLeft < middleOfAnimation) {
                textColor.r += rTextEndMiddleDiff
                textColor.g += gTextEndMiddleDiff
                textColor.b += bTextEndMiddleDiff
                bgColor.r += rBgDiff
                bgColor.g += gBgDiff
                bgColor.b += bBgDiff
            }
            _button.setBackgroundColor(bgColor.toHexColor32().toLong(16).toInt())
            _button.setTextColor(textColor.toHexColor32().toLong(16).toInt())
        }

        private var callback: (Long) -> Unit = {}
        override val launchFunction: () -> Unit = {
            textColor = Color24(Integer.toHexString(_button.textColors.defaultColor))
            bgColor = Color24(Integer.toHexString((_button.background as ColorDrawable).color))
            callback = {
                function(it)
                framesLeft--
                if (framesLeft > 0) Choreographer.getInstance().postFrameCallback(callback) else onFunctionStop()
            }
            Choreographer.getInstance().postFrameCallback(callback)
        }

        override val onFunctionStop: () -> Unit = {
            _button.setBackgroundColor(bgColorEnd.toHexColor32().toLong(16).toInt())
            _button.setTextColor(textColorEnd.toHexColor32().toLong(16).toInt())
            _button.isEnabled = _enabledAtEnd

            _functionQueue.removeFirst()
            if (_functionQueue.size > 0) _functionQueue.first.launchFunction()
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
            }
            false
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