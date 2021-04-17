package com.example.journal

import java.io.DataOutputStream
import java.io.File
import java.io.FileWriter
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ManagerLogin(rootDirectory: String) {
    private val USER_AGENT = "Mozilla/5.0"
    private val ROOT_DIRECTORY = rootDirectory
    private val PRIMARY_DOMAIN = "https://schools.by"

    var csrftoken: String? = null
        private set
    var sessionid: String? = null
        private set
    var pupilUrl: String? = null
        private set

    @Throws(Exception::class)
    fun tryToLogin(username: String, password: String): Boolean {
        if (csrftoken == null) csrftoken = newCsrftoken()

        val postParameters = getEncodedPostParameters(username, password)

        val con = URL("$PRIMARY_DOMAIN/login").openConnection() as HttpURLConnection

        con.connectTimeout = 1500
        con.readTimeout = 1500
        con.instanceFollowRedirects = false
        con.useCaches = false

        con.requestMethod = "POST"
        con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        con.setRequestProperty("accept-encoding", "gzip, deflate, br")
        con.setRequestProperty("accept-language", "en-gb")
        con.setRequestProperty("Connection", "keep-alive")
        con.setRequestProperty("content-length", postParameters.length.toString())
        con.setRequestProperty("content-type", "application/x-www-form-urlencoded")
        con.setRequestProperty("cookie", "csrftoken=$csrftoken")
        con.setRequestProperty("origin", PRIMARY_DOMAIN)
        con.setRequestProperty("referer", "$PRIMARY_DOMAIN/login")
        con.setRequestProperty("user-agent", USER_AGENT)

        con.doInput = true
        con.doOutput = true

        val stream = DataOutputStream(con.outputStream)
        stream.writeBytes(postParameters)
        stream.flush()
        stream.close()

        con.getContent()

        when (con.responseCode) {
            302 -> {
                println("success")
            }
            200 -> {
                con.disconnect()
                return false
            }
            else -> {
                con.disconnect()
                throw Exception("Unknown response code")
            }
        }

        val headers = con.headerFields
        con.disconnect()

        for (entry in headers.entries) {
            try {
                when (entry.key) {
                    "Set-Cookie" -> {
                        for (str in entry.value) {
                            val cookies = HttpCookie.parse(str)
                            for (cookie in cookies) {
                                if (cookie.name.equals("sessionid")) sessionid = cookie.value
                            }
                        }
                    }
                    "Location" -> {
                        pupilUrl = entry.value[0]
                    }
                }
            } catch (npe: NullPointerException) { continue }
        }

        if (sessionid == null || pupilUrl == null) {
            throw Exception("sessionid or pupilUrl missing")
        } else {
            return true
        }
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

        for (entry in hf) {
            try {
                if (entry.key == "Set-Cookie") {
                    for (str in entry.value) {
                        val cookies = HttpCookie.parse(str)
                        for (cookie in cookies) {
                            if (cookie.name == "csrftoken") return cookie.value
                        }
                    }
                }
            } catch (npe: NullPointerException) { continue }
        }
        throw Exception("csrftoken missing")
    }

    fun writeLoginDataToFiles(username: String, realName: String) {
        val usersFolder = File("$ROOT_DIRECTORY/users")
        if (!usersFolder.exists()) usersFolder.mkdir()


        val userDataWriter = FileWriter("$ROOT_DIRECTORY/users/$username.txt", false);

        userDataWriter.write("sessionid: $sessionid" + "\n")
        userDataWriter.write("pupilUrl: $pupilUrl" + "\n")
        userDataWriter.write("realName: $realName")

        userDataWriter.flush()
        userDataWriter.close()


        val currentUserWriter = FileWriter("$ROOT_DIRECTORY/current_user.txt", false)
        currentUserWriter.write(username)
        currentUserWriter.flush()
        currentUserWriter.close()
    }

    private fun getEncodedPostParameters(username: String, password: String): String {
        return "csrfmiddlewaretoken=$csrftoken" +
                "&username=${URLEncoder.encode(username, StandardCharsets.UTF_8.toString())}" +
                "&password=${URLEncoder.encode(password, StandardCharsets.UTF_8.toString())}"
    }
}