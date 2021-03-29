package com.example.journal

class PageParser {
    init { System.loadLibrary("JniParser") }
    external fun parsePage(pagePath: String, dataPath: String)
}