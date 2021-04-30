package com.example.journal

class ParserPage {
    init { System.loadLibrary("JniParserPage") }
    external fun parsePage(pagePath: String, dataPath: String)
}