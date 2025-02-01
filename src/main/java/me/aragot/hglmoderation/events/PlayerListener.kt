package me.aragot.hglmoderation.events

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import me.aragot.hglmoderation.entity.Notification
import me.aragot.hglmoderation.entity.PlayerData
import me.aragot.hglmoderation.entity.punishments.Punishment
import me.aragot.hglmoderation.entity.punishments.PunishmentType
import me.aragot.hglmoderation.repository.PlayerDataRepository
import me.aragot.hglmoderation.repository.PunishmentRepository
import me.aragot.hglmoderation.service.player.PlayerUtils
import me.aragot.hglmoderation.service.punishment.PunishmentConverter.Companion.getMuteComponent
import me.aragot.hglmoderation.service.punishment.PunishmentManager
import java.time.Instant

class PlayerListener {

    companion object {
        var userMessages = HashMap<String, ArrayList<String>>()
        var playerMutes = HashMap<String, Punishment>()
    }

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val mute = playerMutes[event.player.uniqueId.toString()]

        if (mute != null) {
            if (mute.isActive) {
                event.result = PlayerChatEvent.ChatResult.denied()
                event.player.sendMessage(getMuteComponent(mute))
                return
            }
            playerMutes.remove(event.player.uniqueId.toString())
        }

        val messages = userMessages[event.player.uniqueId.toString()]

        if (messages == null) {
            userMessages[event.player.uniqueId.toString()] = ArrayList(listOf(event.message))
            return
        }

        if (messages.size != 15) {
            messages.add(event.message)
            return
        }

        messages.removeAt(0)
        messages.add(event.message)
    }

    @Subscribe
    fun onPlayerJoin(event: LoginEvent) {
        val punishmentRepository = PunishmentRepository()
        val playerDataRepository = PlayerDataRepository()
        val data = playerDataRepository.getPlayerData(event.player)
        val hostAddress = event.player.remoteAddress.address.hostAddress
        data.latestIp = hostAddress

        val activePunishments = punishmentRepository.getActivePunishmentsFor(data.id, hostAddress)
        if (activePunishments.isNotEmpty()) {
            val manager = PunishmentManager()
            for (punishment in activePunishments) {
                //If Punishment is not in player data then PunishmentType == IP BAN, therefore add to player data
                if (!data.punishments.contains(punishment.id)) data.addPunishment(punishment.id)

                if (punishment.types.contains(PunishmentType.BAN) || punishment.types.contains(PunishmentType.IP_BAN)) {
                    manager.enforcePunishment(punishment, null, event)
                    return
                } else if (punishment.types.contains(PunishmentType.MUTE)) {
                    manager.enforcePunishment(punishment, event.player, null)
                }
            }
        } else if (data.punishments.isNotEmpty() && data.punishmentScore > 0) {
            val latest = punishmentRepository.getPunishmentById(data.punishments[data.punishments.size - 1])
            //Reset score after one year since last punishment
            if (latest != null && latest.endsAt + (60 * 60 * 24 * 365) <= Instant.now().epochSecond) {
                data.punishmentScore = 0
            }
        }

        val uuid = event.player.uniqueId.toString()
        for (notif in data.notifications) {
            PlayerData.notificationGroups.computeIfAbsent(
                notif
            ) { k: Notification? -> ArrayList() }
            PlayerData.notificationGroups[notif]!!.add(uuid)
        }
    }

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        val repository = PlayerDataRepository()
        val data = repository.getPlayerData(event.player)

        val uuid = event.player.uniqueId.toString()
        for (notif in data.notifications)
            PlayerData.notificationGroups[notif]!!.remove(uuid)

        playerMutes.remove(event.player.uniqueId.toString())
        userMessages.remove(uuid)

        repository.updateData(data)
        PlayerUtils.removePlayerFromCache(event.player.uniqueId.toString())
    }
}