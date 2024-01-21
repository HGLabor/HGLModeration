package me.aragot.hglmoderation.data.reports;

import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.data.PlayerStats;

import java.util.ArrayList;

public class Report {

    private long reportId;
    private String reportedUUID; //Reported Player UUID
    private String reporterUUID; //Reporter Player UUID
    private long submittedAt;
    private Reasoning reasoning;
    private ReportState state;
    private Priority priority;

    private String

    private static ArrayList<Report> reportLog = new ArrayList<>();

    public Report(long reportId, String reportedUUID, String reporterUUID,  long submittedAt, Reasoning reasoning, Priority priority, ReportState state){
        this.reportId = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.submittedAt = submittedAt;
        this.reasoning = reasoning;
        this.priority = priority;
        this.state = state;
    }



    public static void submitReport(String reportedUUID, String reporterUUID, Reasoning reasoning, Priority priority){
        //Discord Webhook integration
        //Database submission
        Report report = new Report(
                getNextReportId(),
                reportedUUID,
                reporterUUID,
                System.currentTimeMillis(),
                reasoning,
                priority,
                ReportState.OPEN);

        reportLog.add(report);
    }

    public static long getNextReportId(){
        return reportLog.size();
    }

    public static Priority getPriorityForReporter(Player player){
        PlayerStats stats = PlayerStats.getPlayerStats(player);

        if(stats.getReportScore() < 0) return Priority.LOW;
        if(stats.getReportScore() > 5) return Priority.HIGH;
        return Priority.MEDIUM;
    }
}
