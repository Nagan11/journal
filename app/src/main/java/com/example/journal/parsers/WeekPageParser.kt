package com.example.journal.parsers

class WeekPageParser {
    init { System.loadLibrary("JniParserPage") }
    external fun parsePage(pagePath: String, dataPath: String)
}