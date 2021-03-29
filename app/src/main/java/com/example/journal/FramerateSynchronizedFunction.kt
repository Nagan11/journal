package com.example.journal

import android.view.Choreographer

abstract class FramerateSynchronizedFunction {
    abstract var durationInFrames: Int
    abstract val function: (frameTimeNanos: Long) -> Unit
    open val onFunctionStart: () -> Unit = {}
    open val onFunctionStop:  () -> Unit = {}

    private var callback: (Long) -> Unit = {}
    val start: () -> Unit = {
        onFunctionStart()
        callback = {
            function(it)
            durationInFrames--
            if (durationInFrames > 0) Choreographer.getInstance().postFrameCallback(callback) else onFunctionStop()
        }
        Choreographer.getInstance().postFrameCallback(callback)
    }
}