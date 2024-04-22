package me.aragot.hglmoderation.service.player

import me.aragot.hglmoderation.entity.Notification
import me.aragot.hglmoderation.entity.PlayerData
import me.aragot.hglmoderation.response.Responder
import me.aragot.hglmoderation.service.StringUtils.Companion.prettyEnum
import me.aragot.hglmoderation.service.player.PlayerUtils.Companion.getUsernameFromUUID
import me.aragot.hglmoderation.service.punishment.PunishmentConverter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class PlayerDataConverter {
    companion object {
        fun getComponentForPlayerData(data: PlayerData): Component {
            val raw = """
                ${Responder.prefix} <white>Data for Player</white> <red>${getUsernameFromUUID(data.playerId)}</red>
                <white>Latest Ip:</white> <red>${data.latestIp}</red>
                <white>Report Score:</white> <red>${data.reportScore}</red>
                <white>Punishment score:</white> <red>${data.punishmentScore}</red>
                <white>Active Notifications:</white><br>${getNotificationList(data.notifications)}
                
                <white>Previous Punishments:</white><br>${PunishmentConverter.getFormattedPunishments(data)}
                """.trimIndent()
            return MiniMessage.miniMessage().deserialize(raw)
        }

        private fun getNotificationList(notifications: ArrayList<Notification?>): String {
            val notifList = StringBuilder()
            for (notif in notifications) notifList.append("<br><gray>-</gray> <white>").append(
                prettyEnum(
                    notif!!
                )
            ).append("</white>")
            return notifList.toString()
        }
    }
}