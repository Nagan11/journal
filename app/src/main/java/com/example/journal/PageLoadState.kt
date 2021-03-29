package com.example.journal

enum class PageLoadState {
    DOWNLOADING,
    DOWNLOADING_ERROR,
    GATHERING,
    GATHERING_ERROR,
    BUILDING,
    READY
}