package me.aragot.hglmoderation.service.report

import me.aragot.hglmoderation.HGLModeration
import me.aragot.hglmoderation.admin.preset.PresetHandler
import me.aragot.hglmoderation.entity.PlayerData
import me.aragot.hglmoderation.entity.Reasoning
import me.aragot.hglmoderation.entity.reports.Report
import me.aragot.hglmoderation.repository.PlayerDataRepository
import me.aragot.hglmoderation.repository.ReportRepository
import me.aragot.hglmoderation.response.Responder
import me.aragot.hglmoderation.service.player.PlayerUtils.Companion.getUsernameFromUUID
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.SimpleDateFormat
import java.util.*

class ReportConverter {
    companion object {
        fun getMcReportComponent(report: Report, incoming: Boolean = false): Component
        {
            val mm = MiniMessage.miniMessage()
            if (incoming) return mm.deserialize("<gold>============= <white>Incoming</white> <red>Report: #" + report.id + "</red> =============</gold>\n")
                .append(this.getMcReportOverview(report))

            return mm.deserialize("<gold>================= <red>Report: #" + report.id + "</red> =================</gold>\n")
                .append(this.getMcReportOverview(report))
        }

        private fun getMcReportOverview(report: Report): Component
        {
            val mm = MiniMessage.miniMessage()
            val reportedUserName = getUsernameFromUUID(report.reportedUUID)
            val prio = "<white>Priority:</white> <red>" + report.priority.name + "</red>"
            val reason = "<white>Reasoning:</white> <red>" + report.reasoning.name + "</red>"
            val reportState = "<white>State:</white> <red>" + report.state.name + "</red>"
            val reported = "<white>Reported:</white> <red>$reportedUserName</red>"

            val viewDetails: String = this.getViewDetailsRaw(report)

            val reviewReport =
                "<click:run_command:'/review " + report.id + "'><white>[<yellow><b>Review</b></yellow>]</white></click>"

            val deserialize = """                    $prio
                    $reason
                    $reported
                    $reportState

                    $viewDetails   $reviewReport
                    <gold>===================================================</gold>"""
            return mm.deserialize(deserialize)
        }

        private fun getViewDetailsRaw(report: Report): String
        {
            val reportedUserName = getUsernameFromUUID(report.reportedUUID)
            val reporterUserName = getUsernameFromUUID(report.reporterUUID)

            var reportDetails = """
                <yellow><b>Report #${report.id}</b></yellow>
                
                <gray>Reported Player:</gray> <red>$reportedUserName</red>
                <gray>Reported By:</gray> <red>$reporterUserName</red>
                <gray>Reasoning:</gray> <red>${report.reasoning.name}</red>
                <gray>Priority:</gray> <red>${report.priority.name}</red>
                <gray>Submitted at:</gray> <red>${
                SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(
                    Date(report.submittedAt * 1000)
                )}</red>
                <gray>State:</gray> <red>${report.state.name}</red>
                
                """.trimIndent()

            if (Reasoning.getChatReasons().contains(report.reasoning)) {
                reportDetails += """<gray>User Messages:</gray>
                    ${this.getFormattedUserMessages(report)}
                    """.trimIndent()
            }

            return "<hover:show_text:'$reportDetails'><white>[<blue><b>View Details</b></blue>]</white></hover>"
        }

        fun getMCReportActions(report: Report): Component {
            val mm = MiniMessage.miniMessage()
            val playerDataRepository = PlayerDataRepository()
            val data: PlayerData? = playerDataRepository.getPlayerData(report.reportedUUID)

            if (data === null) {
                return mm.deserialize("<red>Unexpected Error. Reported Player was not found.</red>")
            }

            val preset = PresetHandler.instance.getPresetForScore(
                report.reasoning,
                data.punishmentScore
            )
            val presetName = if (preset == null) "None" else preset.name

            val serverName = try {
                HGLModeration.instance.server.getPlayer(UUID.fromString(report.reportedUUID))
                    .orElseThrow().currentServer.orElseThrow().serverInfo.name
            } catch (x: NoSuchElementException) {
                "None"
            }
            val firstLine = "<hover:show_text:'<green>Accept and Punish</green>'><click:suggest_command:'/preset apply $presetName ${report.id}'><white>[<green><b>Punish</b></green>]</white></click></hover>   <hover:show_text:'<red>Decline Report</red>'><click:suggest_command:'/review ${report.id} decline'><white>[<red><b>Decline</b></red>]</white></click></hover>   <hover:show_text:'<red>Mark as malicious</red>'><click:suggest_command:'/review ${report.id} malicious'><white>[<red><b>Decline & Mark as malicious</b></red>]</white></click></hover>"
            val secondLine = "<hover:show_text:'${data.formattedPunishments}'><white>[<blue><b>Previous Punishments</b></blue>]</white></hover>   <hover:show_text:'${getRawOtherReports(report)}'><white>[<blue><b>Other Reports</b></blue>]</white></hover>"
            val thirdLine = "<hover:show_text:'<blue>Teleport to Server</blue>'><click:run_command:'/server $serverName'><white>[<blue><b>Follow Player</b></blue>]</white></click></hover>"

            return mm.deserialize(firstLine).appendNewline().append(mm.deserialize(secondLine)).appendNewline().append(mm.deserialize(thirdLine))
        }

        private fun getRawOtherReports(report: Report): String
        {
            val reportRepository = ReportRepository()
            val reports = reportRepository.getReportsForPlayerExcept(report.reportedUUID, report.id)
            if (reports.isEmpty()) return "No Reports found"
            val formatted =
                java.lang.StringBuilder("<gray><blue>ID</blue>   |   <blue><b>State</b></blue>   |   <blue>Reason</blue>")

            for (reps in reports) {
                formatted.append("\n<gray>").append(reps.id).append(" |</gray> <yellow>")
                    .append(reps.state.name).append("</yellow> <gray>|</gray> <red>")
                    .append(reps.reasoning).append("</red>")
            }

            return formatted.toString()
        }

        private fun getFormattedUserMessages(report: Report): String {
            if (report.reportedUserMessages == null || report.reportedUserMessages.isEmpty()) return "\n<gray>No Messages sent</gray>"

            val messages = StringBuilder()

            val username = try {
                HGLModeration.instance.server.getPlayer(UUID.fromString(report.reportedUUID)).orElseThrow().username
            } catch (x: NoSuchElementException) {
                getUsernameFromUUID(report.reportedUUID)
            }

            for (message in report.reportedUserMessages) messages.append("\n<red>").append(username)
                .append("</red>: ").append(message)

            return messages.toString()
        }

        fun getComponentForReports(reportList: List<Report>): Component {
            val reports = StringBuilder(Responder.prefix + " <gold>Current Reports:</gold>")
            if (reportList.isEmpty()) return MiniMessage.miniMessage()
                .deserialize(reports.append("<white> None</white>").toString())
            val userNameCache = HashMap<String, String?>()
            val displayMax = 10
            var count = 0
            for (report in reportList) {
                if (count == displayMax) break
                if (userNameCache[report.reportedUUID] == null) {
                    userNameCache[report.reportedUUID] = getUsernameFromUUID(report.reportedUUID)
                }
                val fetchReport =
                    "<click:run_command:'/fetcher report " + report.id + "'><white>[<yellow><b>Check out</b></yellow>]</white></click>"
                reports.append("\n<gray>")
                    .append(report.id)
                    .append(" ➡ </gray><red>")
                    .append(report.priority.name)
                    .append("</red><gray> ➡ </gray><red>")
                    .append(report.reasoning.name)
                    .append("</red><gray> ➡ </gray><red>")
                    .append(userNameCache[report.reportedUUID])
                    .append("</red>").append("\n").append(getViewDetailsRaw(report))
                    .append("   ").append(fetchReport)
                count++
            }
            return MiniMessage.miniMessage().deserialize(reports.toString())
        }
    }
}