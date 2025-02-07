package me.aragot.hglmoderation.repository

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.velocitypowered.api.proxy.Player
import me.aragot.hglmoderation.entity.PlayerData
import java.util.*

class PlayerDataRepository : Repository() {
    companion object {
        val dataList: MutableList<PlayerData> = ArrayList()
    }

    fun getPlayerData(player: Player): PlayerData {
        for (data in ::dataList.get()) {
            if (data.id.equals(player.uniqueId)) {
                return data
            }
        }

        var data = this.getPlayerData(uuid = player.uniqueId)
        if (data == null) {
            data = PlayerData(player)
            this.flushData(data)
        }

        ::dataList.get().add(data)
        return data
    }

    fun getPlayerData(uuid: UUID): PlayerData? {
        val data = ::dataList.get().find { playerData -> playerData.id.equals(uuid) }
        return if (data !== null) data else this.database.playerDataCollection.find(Filters.eq("_id", uuid)).first()
    }

    private fun flushData(data: PlayerData): Boolean {
        return try {
            this.database.playerDataCollection.insertOne(data)
            true
        } catch (x: MongoException) {
            false
        }
    }

    fun updateData(data: PlayerData): Boolean {
        return this.database.playerDataCollection.replaceOne(
            Filters.eq("_id", data.id),
            data
        ).wasAcknowledged()
    }
}