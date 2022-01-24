package com.example.journal.parsers

import java.net.HttpURLConnection
import java.net.URL

class RealNameParser(csrftoken: String, sessionid: String, pupilUrl: String) {
    private val USER_AGENT = "Mozilla/5.0"

    private val CSRFTOKEN = csrftoken
    private val SESSIONID = sessionid
    private val PUPIL_URL = pupilUrl

    @Throws(Exception::class)
    fun getRealName(): String {
        val mainPage = getPageCode(PUPIL_URL)

        var buffer: String
        var i = 0
        while (i < mainPage.length) {
            if (mainPage[i++].equals('<')) {
                buffer = "<"
                while (!mainPage[i].equals('>')) {
                    buffer += mainPage[i++]
                }
                buffer += mainPage[i++]

                if (buffer.equals("<title>")) {
                    while (mainPage[i].equals(' ') || mainPage[i].equals('\t') || mainPage[i].equals('\n')) {
                        i++
                    }

                    var realName = ""
                    while (!mainPage[i].equals('.')) {
                        realName += mainPage[i++]
                    }
                    return realName
                }
            }
        }
        throw Exception("No name found")
    }

    @Throws(Exception::class)
    private fun getPageCode(url: String): String {
        val con = URL(url).openConnection() as HttpURLConnection

        con.connectTimeout = 1500
        con.readTimeout = 1500
        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", USER_AGENT)
        con.setRequestProperty("cookie", "csrftoken=$CSRFTOKEN; sessionid=$SESSIONID")
        con.doInput = true

        con.connect()

        var inputLine: String?
        val response = StringBuffer()
        val reader = con.inputStream.bufferedReader()

        inputLine = reader.readLine()
        while (inputLine != null) {
            response.append(inputLine)
            inputLine = reader.readLine()
        }

        reader.close()
        con.disconnect()

        return response.toString()
    }
}