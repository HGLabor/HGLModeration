package me.aragot.hglmoderation.service.punishment

import me.aragot.hglmoderation.admin.config.Config
import me.aragot.hglmoderation.entity.PlayerData
import me.aragot.hglmoderation.entity.Reasoning
import me.aragot.hglmoderation.entity.punishments.Punishment
import me.aragot.hglmoderation.repository.PunishmentRepository
import me.aragot.hglmoderation.response.Responder
import me.aragot.hglmoderation.service.player.PlayerUtils.Companion.getUsernameFromUUID
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.time.Instant
import java.util.concurrent.TimeUnit

class PunishmentConverter {
    companion object {
        //necessary?
        private const val baseWhiteSpaces = "                    "

        fun getBanComponent(punishment: Punishment): Component
        {
            val banReason = """
                <blue>HGLabor</blue>
                <red><b>You were banned from our network.</b></red>
                
                <gray>Punishment ID:</gray> <red>${punishment.id}</red>
                <gray>Reason:</gray> <red>${Reasoning.getPrettyReasoning(punishment.reasoning)}</red>
                <gray>Duration:</gray> <red>${getRemainingTime(punishment)}</red>
                
                <red><b>DO NOT SHARE YOUR PUNISHMENT ID TO OTHERS!!!</b></red>
                <gray>You can appeal for your ban here: <blue><underlined>${Config.discordLink}</underlined></blue></gray>
                """.trimIndent()

            return MiniMessage.miniMessage().deserialize(banReason)
        }

        fun getMuteComponent(punishment: Punishment): Component
        {

            val muteComponent = """
                <gold>=================================================</gold>

                $baseWhiteSpaces<red>You were muted for misbehaving.</red>
        
                $baseWhiteSpaces<gray>Reason:</gray> <red>${Reasoning.getPrettyReasoning(punishment.reasoning)}</red>
                $baseWhiteSpaces<gray>Duration:</gray> <red>${getRemainingTime(punishment)}</red>
        
                <gold>=================================================</gold>
                """.trimIndent()
            return MiniMessage.miniMessage().deserialize(muteComponent)
        }

        fun getComponentForPunishment(punishment: Punishment?): Component {
            if (punishment == null) return MiniMessage.miniMessage()
                .deserialize(Responder.prefix + " <red>This player was never punished before</red>")
            val raw = """
                ${Responder.prefix} <white>Showing Details for ID:</white> <red>${punishment.id}</red>
                <white>Punished Player:</white> <red>${ if (punishment.issuedTo.contains(".")) punishment.issuedTo else getUsernameFromUUID(punishment.issuedTo) }</red>
                <white>Issued By:</white> <red>${getUsernameFromUUID(punishment.issuerUUID)}</red>
                <white>Duration:</white> <red>${getDuration(punishment)}</red>
                <white>Types:</white> <red>${getTypesAsString(punishment)}</red>
                <white>Reasoning:</white> <red>${Reasoning.getPrettyReasoning(punishment.reasoning)}</red>
                <white>Note:</white> <br><red>${punishment.note}</red>
                """.trimIndent()
            return MiniMessage.miniMessage().deserialize(raw)
        }

        fun getFormattedPunishments(data: PlayerData): String {
            if (data.punishments.isEmpty()) return "No Punishments found"
            val repository = PunishmentRepository()
            val punishments = repository.getPunishmentsFor(data.playerId, data.latestIp)
            val formatted =
                StringBuilder("<gray><blue>ID</blue>   |   <blue>Type</blue>   |   <blue>Reason</blue>   |   <blue>Status</blue></gray>")
            for (punishment in punishments) {
                formatted.append("<br><gray>").append(punishment.id).append(" |</gray> <yellow>")
                    .append(getTypesAsString(punishment)).append("</yellow> <gray>|</gray> <red>")
                    .append(punishment.reasoning).append("</red> <gray>|</gray> ")
                    .append(if (punishment.isActive) "<green>⊙</green>" else "<red>⊙</red>")
            }
            return formatted.toString()
        }

        fun getDuration(punishment: Punishment): String {
            if (punishment.endsAtTimestamp == -1L) return "Permanent"
            val differenceSeconds: Long = punishment.endsAtTimestamp - punishment.issuedAtTimestamp
            val days = TimeUnit.SECONDS.toDays(differenceSeconds)
            val hours = TimeUnit.SECONDS.toHours(differenceSeconds) % 24
            val minutes = TimeUnit.SECONDS.toMinutes(differenceSeconds) % 60
            var time = ""
            if (days != 0L) time += days.toString() + "d "
            if (hours != 0L) time += hours.toString() + "h "
            if (minutes != 0L) time += minutes.toString() + "min "
            return time
        }

        fun getTypesAsString(punishment: Punishment): String {
            val builder = StringBuilder()
            for (reasoning in punishment.types) builder.append(reasoning.name).append(",")
            builder.replace(builder.length - 1, builder.length, "")
            return builder.toString()
        }

        private fun getRemainingTime(punishment: Punishment): String {
            if (!punishment.isActive) return "It's over"
            if (punishment.endsAtTimestamp == -1L) return "Permanent"
            val differenceSeconds: Long = punishment.endsAtTimestamp - Instant.now().epochSecond
            val days = TimeUnit.SECONDS.toDays(differenceSeconds)
            val hours = TimeUnit.SECONDS.toHours(differenceSeconds) % 24
            val minutes = TimeUnit.SECONDS.toMinutes(differenceSeconds) % 60
            val seconds = TimeUnit.SECONDS.toSeconds(differenceSeconds) % 60
            var time = ""
            if (days != 0L) time += days.toString() + "d "
            if (hours != 0L) time += hours.toString() + "h "
            if (minutes != 0L) time += minutes.toString() + "min "
            if (seconds != 0L) time += seconds.toString() + "sec "
            return time
        }
    }
}