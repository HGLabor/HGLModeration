package me.aragot.hglmoderation.service.player

import com.velocitypowered.api.util.UuidUtils
import me.aragot.hglmoderation.HGLModeration
import org.bson.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

class PlayerUtils {
    companion object {
        private val playerCache = ArrayList<PlayerCacheEntry>()

        fun getUsernameFromUUID(uuid: UUID): String? {
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

        fun getUuidFromUsername(username: String): UUID? {
            val cacheEntry = this.getCacheEntry(username = username)
            return if (cacheEntry !== null && cacheEntry.isValid()) cacheEntry.uuid
            else try {
                val apiLink = "https://api.mojang.com/users/profiles/minecraft/$username"
                val url = URL(apiLink)
                val connection = url.openConnection() as HttpsURLConnection
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                val status = connection.responseCode
                HGLModeration.instance.logger.info("Fetched $apiLink and got response code: $status")
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
                val uuid = UuidUtils.fromUndashed(doc.getString("id"))
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

        fun removePlayerFromCache(uuid: UUID? = null, username: String? = null) {
            if (uuid === null && username === null) return
            playerCache.removeIf { entry: PlayerCacheEntry -> (username === entry.username || uuid === entry.uuid) }
        }

        private fun getCacheEntry(username: String? = null, uuid: UUID? = null): PlayerCacheEntry? {
            return if (username === null && uuid === null) null
            else playerCache.find { entry: PlayerCacheEntry -> (username.equals(entry.username, ignoreCase = true) || uuid === entry.uuid) }
        }
    }
}