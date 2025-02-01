package me.aragot.hglmoderation.service.punishment

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.proxy.Player
import me.aragot.hglmoderation.HGLModeration
import me.aragot.hglmoderation.discord.HGLBot
import me.aragot.hglmoderation.entity.PlayerData
import me.aragot.hglmoderation.entity.Reasoning
import me.aragot.hglmoderation.entity.punishments.Punishment
import me.aragot.hglmoderation.entity.punishments.PunishmentType
import me.aragot.hglmoderation.entity.reports.Report
import me.aragot.hglmoderation.events.PlayerListener
import me.aragot.hglmoderation.repository.PlayerDataRepository
import me.aragot.hglmoderation.repository.PunishmentRepository
import me.aragot.hglmoderation.service.report.ReportManager
import java.time.Instant
import java.util.*
import java.util.function.Consumer

class PunishmentManager(repo: PunishmentRepository = PunishmentRepository()) {
    private val repository: PunishmentRepository = repo

    fun submitPunishment(punished: PlayerData, punishment: Punishment, weight: Int, report: Report? = null): Boolean {
        val isBotActive = HGLBot.instance !== null
        if (!repository.flushData(punishment) && isBotActive) {
            HGLBot.logPunishmentPushFailure(punishment)
            return false
        }

        if (report !== null) {
            ReportManager().accept(report, punishment.id)
        }

        punished.addPunishment(punishment.id)
        punished.punishmentScore += weight
        this.enforcePunishment(punishment)

        if (isBotActive)
            HGLBot.logPunishment(punishment)

        return true
    }

    fun createPunishment(
        punished: PlayerData,
        punisher: PlayerData,
        types: ArrayList<PunishmentType>,
        reason: Reasoning,
        endsAt: Long,
        note: String = ""
    ): Punishment {
        return Punishment(
            this.getNextId(),
            Instant.now().epochSecond,
            if (types.contains(PunishmentType.IP_BAN)) punished.latestIp else punished.id.toString(),
            punisher.id,
            types,
            endsAt,
            reason,
            note
        )
    }

    fun enforcePunishment(punishment: Punishment, player: Player? = null, loginEvent: LoginEvent? = null) {
        val server = HGLModeration.instance.server
        val banComponent = PunishmentConverter.getBanComponent(punishment)
        val muteComponent = PunishmentConverter.getMuteComponent(punishment)

        if (loginEvent !== null) {
            loginEvent.result = ResultedEvent.ComponentResult.denied(PunishmentConverter.getBanComponent(punishment))
            return
        }

        if (punishment.types.contains(PunishmentType.IP_BAN)) {
            server.allPlayers.forEach((Consumer<Player> { connected: Player ->
                if (connected.remoteAddress.address.hostAddress.equals(punishment.issuedTo, ignoreCase = true)) {
                    val playerDataRepository = PlayerDataRepository()
                    val data = playerDataRepository.getPlayerData(connected)
                    if (!data.punishments
                            .contains(punishment.id)
                    ) data.addPunishment(punishment.id)
                    connected.disconnect(banComponent)
                }
            }))
            return
        }


        val realPlayer: Player? =
            if (player !== null) player else server.getPlayer(UUID.fromString(punishment.issuedTo)).orElse(null)
        if (realPlayer === null) return
        var playerUuid: UUID? = null
        try {
            playerUuid = UUID.fromString(punishment.issuedTo)
        } catch (_: IllegalArgumentException) {
        }

        if (punishment.types.contains(PunishmentType.MUTE) && playerUuid !== null) {
            PlayerListener.playerMutes[playerUuid] = punishment
            realPlayer.sendMessage(muteComponent)
        }

        if (punishment.types.contains(PunishmentType.BAN)) {
            realPlayer.disconnect(banComponent)
        }
    }

    private fun getNextId(): String {
        //table is a hex number
        //Report id is random 8 digit hex number
        val table = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
        var isUnique = false
        var id = ""
        while (!isUnique) {
            val rand = Random()

            for (i in 0..7) id += table[rand.nextInt(16)]

            if (repository.getPunishmentById(id) != null) {
                id = ""
                continue
            }

            isUnique = true
        }

        return id
    }
}