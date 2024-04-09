package me.aragot.hglmoderation.repository

import com.mongodb.MongoException
import com.mongodb.client.MongoCursor
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import me.aragot.hglmoderation.entity.Reasoning
import me.aragot.hglmoderation.entity.reports.Report
import me.aragot.hglmoderation.entity.reports.ReportState
import org.bson.conversions.Bson
import java.util.function.Predicate
import java.util.stream.Collectors

class ReportRepository: Repository() {
    companion object {
        var unfinishedReports: MutableList<Report> = java.util.ArrayList()

        fun getReportsInProgress(): List<Report>
        {
            return unfinishedReports.stream().filter { report: Report -> report.state == ReportState.UNDER_REVIEW }
                .collect(Collectors.toList())
        }

        fun getOpenReports(): List<Report> {
            return unfinishedReports.stream()
                .filter((Predicate { report: Report -> report.state == ReportState.OPEN })).collect(Collectors.toList())
        }
    }

    fun getReportById(id: String): Report?
    {
        return this.database.reportCollection.find(Filters.eq("_id", id)).first();
    }

    fun flushData(report: Report): Boolean
    {
        try {
            this.database.reportCollection.insertOne(report)
            return true
        } catch (x: MongoException) {
            return false
        }
    }

    fun getReportsForPlayer(uuid: String): ArrayList<Report>
    {
        return this.database.reportCollection.find(
            Filters.and(
                Filters.eq("reportedUUID", uuid),
                Filters.ne("state", ReportState.DONE.name)
            )
        ).into(ArrayList())
    }

    fun getReportsForPlayerExcept(playerId: String, reportId: String): ArrayList<Report>
    {
        return this.database.reportCollection.find(
            Filters.and(
                Filters.eq("reportedUUID", playerId),
                Filters.ne("_id", reportId),
                Filters.ne("state", ReportState.DONE.name)
            )
        ).into(java.util.ArrayList<Report>())
    }

    fun updateReportsBasedOn(report: Report): Boolean {
        return this.database.reportCollection.updateMany(
            Filters.and(
                Filters.eq<String>("reportedUUID", report.reportedUUID),
                Filters.eq<Reasoning>("reasoning", report.reasoning),
                Filters.or(
                    Filters.eq<ReportState>("state", ReportState.UNDER_REVIEW),
                    Filters.eq<ReportState>("state", ReportState.OPEN)
                )
            ),
            Updates.combine(
                Updates.set<ReportState>("state", report.state),
                Updates.set<String>("reviewedBy", report.reviewedBy),
                Updates.set<String>("punishmentId", report.punishmentId)
            )
        ).wasAcknowledged()
    }

    fun fetchUnfinishedReports()
    {
        val cursor: MongoCursor<Report> = this.database.reportCollection.aggregate(
            listOf<Bson>(
                Aggregates.match(
                    Filters.or(
                        Filters.eq<ReportState>("state", ReportState.OPEN),
                        Filters.eq<ReportState>("state", ReportState.UNDER_REVIEW)
                    )
                )
            )
        ).iterator()

        val reportList = java.util.ArrayList<Report>()
        while (cursor.hasNext()) reportList.add(cursor.next())
        ::unfinishedReports.set(reportList)
    }
}