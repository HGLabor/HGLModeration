package me.aragot.hglmoderation.service.punishment

import me.aragot.hglmoderation.admin.config.Config
import me.aragot.hglmoderation.entity.Reasoning
import me.aragot.hglmoderation.entity.punishments.Punishment
import me.aragot.hglmoderation.response.Responder
import me.aragot.hglmoderation.service.player.PlayerUtils.Companion.getUsernameFromUUID
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class PunishmentConverter {
    companion object {
        fun getBanComponent(punishment: Punishment): Component
        {
            val banReason = """
                <blue>HGLabor</blue>
                <red><b>You were banned from our network.</b></red>
                
                <gray>Punishment ID:</gray> <red>${punishment.id}</red>
                <gray>Reason:</gray> <red>${Reasoning.getPrettyReasoning(punishment.reasoning)}</red>
                <gray>Duration:</gray> <red>${punishment.remainingTime}</red>
                
                <red><b>DO NOT SHARE YOUR PUNISHMENT ID TO OTHERS!!!</b></red>
                <gray>You can appeal for your ban here: <blue><underlined>${Config.discordLink}</underlined></blue></gray>
                """.trimIndent()

            return MiniMessage.miniMessage().deserialize(banReason)
        }

        fun getMuteComponent(punishment: Punishment): Component
        {
            val muteComponent = """<gold>=================================================</gold>

            <red>You were muted for misbehaving.
    
            <gray>Reason:</gray> <red>${Reasoning.getPrettyReasoning(punishment.reasoning)}</red>
            <gray>Duration:</gray> <red>${punishment.remainingTime}</red>
    
          <gold>=================================================</gold>"""
            return MiniMessage.miniMessage().deserialize(muteComponent)
        }

        fun getComponentForPunishment(punishment: Punishment?): Component {
            if (punishment == null) return MiniMessage.miniMessage()
                .deserialize(Responder.prefix + " <red>This player was never punished before</red>")
            val raw = """${Responder.prefix} <white>Showing Details for ID:</white> <red>${punishment.id}</red>
<white>Punished Player:</white> <red>${ if (punishment.issuedTo.contains(".")) punishment.issuedTo else getUsernameFromUUID(punishment.issuedTo) }</red>
<white>Issued By:</white> <red>${getUsernameFromUUID(punishment.issuerUUID)}</red>
<white>Duration:</white> <red>${punishment.duration}</red>
<white>Types:</white> <red>${punishment.typesAsString}</red>
<white>Reasoning:</white> <red>${Reasoning.getPrettyReasoning(punishment.reasoning)}</red>
<white>Note:</white> <br><red>${punishment.note}</red>""".trimIndent()
            return MiniMessage.miniMessage().deserialize(raw)
        }
    }
}