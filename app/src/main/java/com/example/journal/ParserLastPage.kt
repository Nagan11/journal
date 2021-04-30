package com.example.journal

class ParserLastPage {
    init { System.loadLibrary("JniParserLastPage") }
    external fun parsePage(pagePath: String, dataPath: String)
}