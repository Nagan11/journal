package com.example.journal.parsers

class LastPageParser {
    init { System.loadLibrary("JniParserLastPage") }
    external fun parsePage(pagePath: String, dataPath: String)
}