package com.example.journal

import android.os.Build
import java.io.DataOutputStream
import java.io.FileWriter
import java.net.*
import java.nio.charset.StandardCharsets

class LoginManager(rootDirectory: String) {
    private val USER_AGENT = "Mozilla/5.0"
    private val ROOT_DIRECTORY = rootDirectory
    private val LOGIN_PRIMARY_DOMAIN = "https://schools.by/login"

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

        val connection = URL(LOGIN_PRIMARY_DOMAIN).openConnection() as HttpURLConnection

        connection.connectTimeout = 1500
        connection.readTimeout = 1500

        connection.instanceFollowRedirects = false
        connection.useCaches = false
        connection.requestMethod = "POST"

        connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        connection.setRequestProperty("accept-encoding", "gzip, deflate, br")
        connection.setRequestProperty("accept-language", "en-gb")
        connection.setRequestProperty("Connection", "keep-alive")
        connection.setRequestProperty("content-length", Integer.toString(postParameters!!.length))
        connection.setRequestProperty("content-type", "application/x-www-form-urlencoded")
        connection.setRequestProperty("cookie", "csrftoken=$csrftoken;")
        connection.setRequestProperty("origin", "https://schools.by")
        connection.setRequestProperty("referer", "https://schools.by/login")
        connection.setRequestProperty("user-agent", USER_AGENT)

        connection.doInput = true
        connection.doOutput = true

        val stream = DataOutputStream(connection.outputStream)
        stream.writeBytes(postParameters)
        stream.flush()
        stream.close()

        connection.getContent()

        val responseCode = connection.responseCode
        when (responseCode) {
            302 -> {
                println("success")
            }
            200 -> {
                connection.disconnect()
                return false
            }
            else -> {
                connection.disconnect()
                throw Exception("Unknown response code")
            }
        }

        val headers = connection.headerFields
        connection.disconnect()

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
                        pupilUrl = entry.value.get(0)
                    }
                }
            } catch (npe: NullPointerException) {continue}
        }


        if (sessionid == null || pupilUrl == null) {
            throw Exception("sessionid or pupilUrl missing")
        } else {
            return true
        }
    }

    @Throws(Exception::class)
    private fun newCsrftoken(): String {
        val con = URL(LOGIN_PRIMARY_DOMAIN).openConnection() as HttpURLConnection

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
                            if (cookie.name == "csrftoken") {
                                return cookie.value
                            }
                        }
                    }
                }
            } catch (npe: NullPointerException) {continue}
        }
        throw Exception("csrftoken missing")
    }

    fun writeLoginDataToFiles(username: String, realName: String) {
        val foutUsername = FileWriter("$ROOT_DIRECTORY/UserData/username.txt", false)
        foutUsername.write(username)
        foutUsername.flush()
        foutUsername.close()

        val foutSessionid = FileWriter("$ROOT_DIRECTORY/UserData/sessionid.txt", false)
        foutSessionid.write(sessionid)
        foutSessionid.flush()
        foutSessionid.close()

        val foutUrl = FileWriter("$ROOT_DIRECTORY/UserData/pupilUrl.txt", false)
        foutUrl.write(pupilUrl)
        foutUrl.flush()
        foutUrl.close()

        val foutRealName = FileWriter("$ROOT_DIRECTORY/UserData/realName.txt", false)
        foutRealName.write(realName)
        foutRealName.flush()
        foutRealName.close()

        val foutStatus = FileWriter("$ROOT_DIRECTORY/UserData/status.txt", false)
        foutStatus.write("YES")
        foutStatus.flush()
        foutStatus.close()
    }

    private fun getEncodedPostParameters(username: String, password: String) : String {
        var pp = "csrfmiddlewaretoken=$csrftoken"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pp += "&username="
            pp += URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
            pp += "&password="
            pp += URLEncoder.encode(password, StandardCharsets.UTF_8.toString())
        } else {
            pp += "&username="
            pp += URLEncoder.encode(username)
            pp += "&password="
            pp += URLEncoder.encode(password)
        }
        return pp
    }
}