package me.aragot.hglmoderation.service.report

import com.velocitypowered.api.proxy.Player
import me.aragot.hglmoderation.discord.HGLBot
import me.aragot.hglmoderation.entity.Notification
import me.aragot.hglmoderation.entity.PlayerData
import me.aragot.hglmoderation.entity.Reasoning
import me.aragot.hglmoderation.entity.reports.Priority
import me.aragot.hglmoderation.entity.reports.Report
import me.aragot.hglmoderation.entity.reports.ReportState
import me.aragot.hglmoderation.repository.PlayerDataRepository
import me.aragot.hglmoderation.repository.ReportRepository
import me.aragot.hglmoderation.service.player.Notifier
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class ReportManager(repo: ReportRepository = ReportRepository()) {
    private val repository: ReportRepository = repo

    fun submitReport(reportedUUID: String, reporterUuid: String, reasoning: Reasoning, priority: Priority)
    {
        val report = Report(
            getNextId(),
            reportedUUID,
            reporterUuid,
            Instant.now().epochSecond,
            reasoning,
            priority,
            ReportState.OPEN
        )
        val isBotActive = HGLBot.instance !== null

        if (this.repository.flushData(report) && isBotActive) {
            HGLBot.logReport(report)
        }

        ReportRepository.unfinishedReports.add(report)

        Notifier.notify(Notification.REPORT, ReportConverter.getMcReportComponent(report, incoming = true))
    }

    fun getPriorityForPlayer(player: Player): Priority
    {
        val playerDataRepository = PlayerDataRepository()
        val data = playerDataRepository.getPlayerData(player)

        if (data.reportScore < 0) return Priority.LOW
        if (data.reportScore > 5) return Priority.HIGH
        return Priority.MEDIUM
    }

    fun startReview(report: Report, reviewer: String)
    {
        report.reviewedBy = reviewer
        report.state = ReportState.UNDER_REVIEW
        for(other: Report in ReportRepository.unfinishedReports) {
            if (!report.reportedUUID.equals(other.reportedUUID, ignoreCase = true) || report.reasoning !== other.reasoning) {
                continue
            }
            other.reviewedBy = reviewer
            other.state = ReportState.UNDER_REVIEW
        }

        this.repository.updateReportsBasedOn(report)
    }

    fun decline(report: Report)
    {
        report.state = ReportState.DONE
        val reviewableReports = ReportRepository.unfinishedReports.stream().filter { other: Report ->
            other.reportedUUID.equals(report.reportedUUID, ignoreCase = true) && other.reasoning == report.reasoning
        }.collect(Collectors.toList())

        repository.updateReportsBasedOn(report)
        ReportRepository.unfinishedReports.removeAll(reviewableReports)
    }

    fun malicious(report: Report)
    {
        this.decline(report)
        val playerDataRepository = PlayerDataRepository()
        val data: PlayerData? = playerDataRepository.getPlayerData(report.reporterUUID)
        if (data !== null)
            data.reportScore -= 2
    }

    fun accept(report: Report, punishmentId: String)
    {
        val reviewableReports = ReportRepository.unfinishedReports.stream().filter { other: Report ->
            other.reportedUUID.equals(report.reportedUUID, ignoreCase = true) && other.reasoning === report.reasoning
        }.collect(Collectors.toList())

        report.punishmentId = punishmentId
        report.state = ReportState.DONE

        val reporters = ArrayList<UUID>()
        for (other in reviewableReports) reporters.add(UUID.fromString(other.reporterUUID))

        repository.updateReportsBasedOn(report)
        ReportRepository.unfinishedReports.removeAll(reviewableReports)

        Notifier.notifyReporters(reporters)
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

            if (repository.getReportById(id) != null) {
                id = ""
                continue
            }

            isUnique = true
        }

        return id
    }
}