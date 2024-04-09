package me.aragot.hglmoderation.service.player

import java.time.Instant

class PlayerCacheEntry(var username: String, var uuid: String, private var timestamp: Long = Instant.now().epochSecond) {
    fun updateTimeStamp()
    {
        this.timestamp = Instant.now().epochSecond
    }

    fun isValid(): Boolean
    {
        return Instant.now().epochSecond - this.timestamp > 300
    }
}