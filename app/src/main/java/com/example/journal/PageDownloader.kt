package com.example.journal

import java.io.BufferedReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URL

class PageDownloader(
        private val ROOT_DIRECTORY: String,
        private val sessionid: String,
        private val quarter: Int,
        private val week: Int,
        private val link: String
) {
    private val USER_AGENT = "Mozilla/5.0"
    private val PRIMARY_DOMAIN = "https://schools.by"
    private var csrftoken: String? = null

    fun downloadPage(): Boolean { // 1
        var getCodeAttempts = 5
        while (getCodeAttempts-- > 0) {
            try {
                if (csrftoken == null) csrftoken = newCsrftoken()
                val pageCode: String = getPageCode(link)
                val fout = FileWriter("$ROOT_DIRECTORY/pages/q${quarter}w${week}.html")
                fout.write(pageCode)
                fout.flush()
                fout.close()
                return true
            } catch (e: java.lang.Exception) {
                println("Exception -> $e")
            }
        }
        return false
    }

    @Throws(java.lang.Exception::class)
    private fun getPageCode(url: String): String {
        val con = URL(url).openConnection() as HttpURLConnection

        con.connectTimeout = 1500
        con.readTimeout = 1500
        con.instanceFollowRedirects = false
        con.useCaches = false

        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", USER_AGENT)
        con.setRequestProperty("cookie", "csrftoken=$csrftoken; sessionid=$sessionid")

        var buffer: String?
        var pageCode = StringBuffer()
        var input = BufferedReader(InputStreamReader(con.inputStream))

        while (input.readLine().also { buffer = it } != null) pageCode.append("$buffer\n")

        input.close()
        con.disconnect()

        if (pageCode.isEmpty()) throw java.lang.Exception("Response missing")
        return pageCode.toString()
    }

    @Throws(Exception::class)
    private fun newCsrftoken(): String {
        val con = URL("$PRIMARY_DOMAIN/login").openConnection() as HttpURLConnection

        con.connectTimeout = 1500
        con.readTimeout = 1500

        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", USER_AGENT)

        con.connect()
        val hf = con.headerFields
        con.disconnect()

        for ((key, value) in hf) {
            try {
                if (key == "Set-Cookie") {
                    val cookies = HttpCookie.parse(value[0])
                    for (cookie in cookies) {
                        if (cookie.name == "csrftoken") return cookie.value
                    }
                }
            } catch (npe: NullPointerException) { continue }
        }
        throw Exception("csrftoken not found in headers")
    }
}