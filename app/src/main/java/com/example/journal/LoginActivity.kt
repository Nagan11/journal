package com.example.journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Choreographer
import android.view.View
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

    private val LOGIN_MANAGER by lazy { LoginManager(ROOT_DIRECTORY) }

    private val BUTTON_NORMAL_TEXT_COLOR            by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonNormalText) }
    private val BUTTON_NORMAL_BACKGROUND_COLOR      by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonNormalBackground) }
    private val BUTTON_LOADING_TEXT_COLOR           by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonLoadingText) }
    private val BUTTON_LOADING_BACKGROUND_COLOR     by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonLoadingBackground) }
    private val BUTTON_ERROR_TEXT_COLOR             by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonErrorText) }
    private val BUTTON_ERROR_BACKGROUND_COLOR       by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonErrorBackground) }
    private val BUTTON_SUCCESS_TEXT_COLOR           by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonSuccessText) }
    private val BUTTON_SUCCESS_BACKGROUND_COLOR     by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonSuccessBackground) }
    private val BUTTON_NORMAL_LOADING_MIDDLE_COLOR  by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonNormalLoadingMiddle) }
    private val BUTTON_LOADING_ERROR_MIDDLE_COLOR   by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonLoadingErrorMiddle) }
    private val BUTTON_LOADING_SUCCESS_MIDDLE_COLOR by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonLoadingSuccessMiddle) }
    private val BUTTON_ERROR_NORMAL_MIDDLE_COLOR    by lazy { ContextCompat.getColor(CONTEXT, R.color.logInButtonErrorNormalMiddle) }

    private val ANIMATION_DURATION_FRAMES: Int = 16
    private val PAUSE_DURATION_FRAMES: Int     = 40

    private var animationQueue = ArrayDeque<GradientButtonAnimation>()

    class GradientButtonAnimation(logInButton: Button, animationQueue: ArrayDeque<GradientButtonAnimation>,
                                  textColorStartInt: Int, bgColorStartInt: Int,
                                  textColorMiddleInt: Int, bgColorMiddleInt: Int,
                                  textColorEndInt: Int, bgColorEndInt: Int,
                                  dur: Int, newText: String, enabledAtEnd: Boolean) {
        private var framesLeft = dur
        private val middleOfAnimation = dur / 2

        private var textColor = Color24(Integer.toHexString(textColorStartInt))
        private var bgColor = Color24(Integer.toHexString(bgColorStartInt))
        private var textColorMiddle = Color24(Integer.toHexString(textColorMiddleInt))
        private var bgColorMiddle = Color24(Integer.toHexString(bgColorMiddleInt))
        private var textColorEnd = Color24(Integer.toHexString(textColorEndInt))
        private var bgColorEnd = Color24(Integer.toHexString(bgColorEndInt))

        private val rBgEndMiddleDiff = (bgColorEnd.r - bgColorMiddle.r) / middleOfAnimation
        private val gBgEndMiddleDiff = (bgColorEnd.g - bgColorMiddle.g) / middleOfAnimation
        private val bBgEndMiddleDiff = (bgColorEnd.b - bgColorMiddle.b) / middleOfAnimation

        private val rMiddleBgStartDiff = (bgColorMiddle.r - bgColor.r) / middleOfAnimation
        private val gMiddleBgStartDiff = (bgColorMiddle.g - bgColor.g) / middleOfAnimation
        private val bMiddleBgStartDiff = (bgColorMiddle.b - bgColor.b) / middleOfAnimation

        private val rTextEndMiddleDiff = (textColorEnd.r - textColorMiddle.r) / middleOfAnimation
        private val gTextEndMiddleDiff = (textColorEnd.g - textColorMiddle.g) / middleOfAnimation
        private val bTextEndMiddleDiff = (textColorEnd.b - textColorMiddle.b) / middleOfAnimation

        private val rMiddleTextStartDiff = (textColorMiddle.r - textColor.r) / middleOfAnimation
        private val gMiddleTextStartDiff = (textColorMiddle.g - textColor.g) / middleOfAnimation
        private val bMiddleTextStartDiff = (textColorMiddle.b - textColor.b) / middleOfAnimation

        private var doFrame: (frameTimeNanos: Long) -> Unit = {
            if (framesLeft > middleOfAnimation) {
                textColor.r += rMiddleTextStartDiff
                textColor.g += gMiddleTextStartDiff
                textColor.b += bMiddleTextStartDiff

                bgColor.r += rMiddleBgStartDiff
                bgColor.g += gMiddleBgStartDiff
                bgColor.b += bMiddleBgStartDiff
            } else if (framesLeft == middleOfAnimation) {
                textColor = textColorMiddle.copy()
                bgColor = bgColorMiddle.copy()
                logInButton.setText(newText)
            } else if (framesLeft < middleOfAnimation) {
                textColor.r += rTextEndMiddleDiff
                textColor.g += gTextEndMiddleDiff
                textColor.b += bTextEndMiddleDiff

                bgColor.r += rBgEndMiddleDiff
                bgColor.g += gBgEndMiddleDiff
                bgColor.b += bBgEndMiddleDiff
            }
            logInButton.setBackgroundColor(bgColor.toHexColor32().toLong(16).toInt())
            logInButton.setTextColor(textColor.toHexColor32().toLong(16).toInt())
        }

        private var onAnimationStop: () -> Unit = {
            logInButton.setBackgroundColor(bgColorEnd.toHexColor32().toLong(16).toInt())
            logInButton.setTextColor(textColorEnd.toHexColor32().toLong(16).toInt())
            logInButton.isEnabled = enabledAtEnd

            animationQueue.removeFirst()
            if (animationQueue.size > 0) animationQueue.first.start()
        }

        private var callback: (Long) -> Unit = {}

        fun start() {
            callback = {
                doFrame(it)
                framesLeft--
                if (framesLeft > 0) Choreographer.getInstance().postFrameCallback(callback) else onAnimationStop()
            }
            Choreographer.getInstance().postFrameCallback(callback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        resetLogInButton()
    }

    override fun onBackPressed() {}

    fun logInButtonOnClick(view: View) {
        logInButton.isEnabled = false
        setButtonStateLoadingAfterNormal()
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
                        setButtonStateErrorAfterLoadingThenNormal()
                        return@launch
                    }
                }
            }

            if (loggedIn!!) {
                setButtonStateSuccessAfterLoading()
                val nameParser = RealNameParser(LOGIN_MANAGER.csrftoken!!, LOGIN_MANAGER.sessionid!!, LOGIN_MANAGER.pupilUrl!!)
                var realName: String? = null
                for (i in 1..3) {
                    try {
                        realName = nameParser.getRealName()
                        break
                    } catch (e: Exception) {}
                }
                LOGIN_MANAGER.writeLoginDataToFiles(username, if (realName == null) username else realName!!)

                GlobalScope.launch {
                    while (animationQueue.size > 0) { delay(50L) }
                    startActivity(Intent(CONTEXT, MainMenuActivity::class.java))
                }
            } else {
                setButtonStateWrongAfterLoadingThenNormal()
            }
        }
    }

    private fun resetLogInButton() {
        logInButton.setBackgroundColor(BUTTON_NORMAL_BACKGROUND_COLOR)
        logInButton.setTextColor(BUTTON_NORMAL_TEXT_COLOR)
        logInButton.setText("Войти")
        logInButton.isEnabled = true
    }

    private fun setButtonStateLoadingAfterNormal() {
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_NORMAL_TEXT_COLOR, BUTTON_NORMAL_BACKGROUND_COLOR,
                        BUTTON_NORMAL_LOADING_MIDDLE_COLOR, BUTTON_NORMAL_LOADING_MIDDLE_COLOR,
                        BUTTON_LOADING_TEXT_COLOR, BUTTON_LOADING_BACKGROUND_COLOR,
                        ANIMATION_DURATION_FRAMES, "Вход...", false
                )
        )
        runOnUiThread { if (animationQueue.size == 1) animationQueue.first.start() }
    }

    private fun setButtonStateWrongAfterLoadingThenNormal() {
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_LOADING_TEXT_COLOR, BUTTON_LOADING_BACKGROUND_COLOR,
                        BUTTON_LOADING_ERROR_MIDDLE_COLOR, BUTTON_LOADING_ERROR_MIDDLE_COLOR,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        ANIMATION_DURATION_FRAMES, "Неверный логин/пароль", false
                )
        )
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        PAUSE_DURATION_FRAMES, "Неверный логин/пароль", false
                )
        )
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        BUTTON_ERROR_NORMAL_MIDDLE_COLOR, BUTTON_ERROR_NORMAL_MIDDLE_COLOR,
                        BUTTON_NORMAL_TEXT_COLOR, BUTTON_NORMAL_BACKGROUND_COLOR,
                        ANIMATION_DURATION_FRAMES, "Войти", true
                )
        )
        runOnUiThread { if (animationQueue.size == 3) animationQueue.first.start() }
    }

    private fun setButtonStateErrorAfterLoadingThenNormal() {
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_LOADING_TEXT_COLOR, BUTTON_LOADING_BACKGROUND_COLOR,
                        BUTTON_LOADING_ERROR_MIDDLE_COLOR, BUTTON_LOADING_ERROR_MIDDLE_COLOR,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        ANIMATION_DURATION_FRAMES, "Ошибка", false
                )
        )
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        PAUSE_DURATION_FRAMES / 2, "Ошибка", false
                )
        )
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_ERROR_TEXT_COLOR, BUTTON_ERROR_BACKGROUND_COLOR,
                        BUTTON_ERROR_NORMAL_MIDDLE_COLOR, BUTTON_ERROR_NORMAL_MIDDLE_COLOR,
                        BUTTON_NORMAL_TEXT_COLOR, BUTTON_NORMAL_BACKGROUND_COLOR,
                        ANIMATION_DURATION_FRAMES, "Войти", true
                )
        )
        runOnUiThread { if (animationQueue.size == 3) animationQueue.first.start() }
    }

    private fun setButtonStateSuccessAfterLoading() {
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_LOADING_TEXT_COLOR, BUTTON_LOADING_BACKGROUND_COLOR,
                        BUTTON_LOADING_SUCCESS_MIDDLE_COLOR, BUTTON_LOADING_SUCCESS_MIDDLE_COLOR,
                        BUTTON_SUCCESS_TEXT_COLOR, BUTTON_SUCCESS_BACKGROUND_COLOR,
                        ANIMATION_DURATION_FRAMES, "Успешно", false
                )
        )
        animationQueue.addLast(
                GradientButtonAnimation(
                        logInButton, animationQueue,
                        BUTTON_SUCCESS_TEXT_COLOR, BUTTON_SUCCESS_BACKGROUND_COLOR,
                        BUTTON_SUCCESS_TEXT_COLOR, BUTTON_SUCCESS_BACKGROUND_COLOR,
                        BUTTON_SUCCESS_TEXT_COLOR, BUTTON_SUCCESS_BACKGROUND_COLOR,
                        PAUSE_DURATION_FRAMES / 2, "Успешно", false
                )
        )
        runOnUiThread { if (animationQueue.size == 2) animationQueue.first.start() }
    }
}