package com.example.journal

class ParserPage {
    init { System.loadLibrary("JniParser") }
    external fun parsePage(pagePath: String, dataPath: String)
}