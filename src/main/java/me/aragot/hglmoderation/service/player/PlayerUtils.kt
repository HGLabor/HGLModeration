package me.aragot.hglmoderation.service.player

import org.bson.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PlayerUtils {
    companion object {
        private val playerCache = ArrayList<PlayerCacheEntry>()
        
        fun getUsernameFromUUID(uuid: String): String?
        {
            val cacheEntry = this.getCacheEntry(uuid = uuid)
            return if (cacheEntry !== null && cacheEntry.isValid()) {
                cacheEntry.username
            } else {
                try {
                    val url = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.connectTimeout = 2000
                    connection.readTimeout = 2000
                    val status = connection.responseCode

                    if (status > 299) {
                        return null
                    }

                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val content = StringBuilder()
                    var inputLine: String?
                    while (reader.readLine().also { inputLine = it } != null) {
                        content.append(inputLine)
                    }
                    reader.close()
                    connection.disconnect()
                    val doc = Document.parse(content.toString())
                    val name = doc.getString("name")
                    if (cacheEntry !== null) {
                        cacheEntry.username = name
                        cacheEntry.updateTimeStamp()
                    } else {
                        playerCache.add(PlayerCacheEntry(name, uuid))
                    }

                    name
                } catch (e: IOException) {
                    "null"
                }
            }
        }

        fun getUuidFromUsername(username: String): String?
        {
            val cacheEntry = this.getCacheEntry(username = username)
            return if (cacheEntry !== null && cacheEntry.isValid()) cacheEntry.uuid
            else try {
                val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
                val connection = url.openConnection() as HttpsURLConnection
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                val status = connection.responseCode

                if (status > 299) {
                    return null
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val content = StringBuilder()
                var inputLine: String?
                while (reader.readLine().also { inputLine = it } != null) {
                    content.append(inputLine)
                }
                reader.close()
                connection.disconnect()

                val doc = Document.parse(content.toString())
                val uuid = addHyphensToUUID(doc.getString("id"))
                if (cacheEntry !== null) {
                    cacheEntry.uuid = uuid
                    cacheEntry.updateTimeStamp()
                } else {
                    playerCache.add(PlayerCacheEntry(username, uuid))
                }

                uuid
            } catch (e: IOException) {
                null
            }
        }

        fun addHyphensToUUID(uuidWithoutHyphens: String): String
        {
            require(uuidWithoutHyphens.length == 32) { "Invalid UUID length" }

            val sb = StringBuilder(uuidWithoutHyphens)
            sb.insert(20, "-")
            sb.insert(16, "-")
            sb.insert(12, "-")
            sb.insert(8, "-")

            return sb.toString()
        }

        fun removePlayerFromCache(uuid: String? = null, username: String? = null)
        {
            if (uuid === null && username === null) return
            playerCache.removeIf { entry: PlayerCacheEntry -> (username === entry.username || uuid === entry.uuid) }
        }

        private fun getCacheEntry(username: String? = null, uuid: String? = null): PlayerCacheEntry?
        {
            return if (username === null && uuid === null) null
            else playerCache.find { entry: PlayerCacheEntry -> (username === entry.username || uuid === entry.uuid) }
        }
    }
}