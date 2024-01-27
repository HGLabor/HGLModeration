package me.aragot.hglmoderation.data.reports;

import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.database.ModerationDB;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;

import java.time.Instant;
import java.util.ArrayList;

public class Report {

    private long reportId;
    private String reportedUUID; //Reported Player UUID
    private String reporterUUID; //Reporter Player UUID
    private long submittedAt;
    private Reasoning reasoning;
    private ReportState state;
    private Priority priority;

    private String reviewedBy; //Minecraft Player UUID
    private long punishmentId;

    private ArrayList<String> reportedUserMessages;

    public static ArrayList<Report> reportLog = new ArrayList<>();

    public Report(long reportId, String reportedUUID, String reporterUUID,  long submittedAt, Reasoning reasoning, Priority priority, ReportState state){
        this.reportId = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.submittedAt = submittedAt;
        this.reasoning = reasoning;
        if(Reasoning.getChatReasons().contains(reasoning)) this.reportedUserMessages = PlayerListener.userMessages.get(reportedUUID);
        this.priority = priority;
        this.state = state;
    }


    public static boolean synchronizeDB(){
        ArrayList<Report> prevReports = ModerationDB.getAllReports();
        ArrayList<Report> missingReports = new ArrayList<>();

        //Remove all Reports that are in the database from the reports that have to be pushed to the database
        for(Report report : reportLog){

            boolean found = false;

            for(Report prevReport : prevReports){
                if(report.getReportId() == prevReport.getReportId()){
                    found = true;
                    break;
                }
            }

            if(!found) missingReports.add(report);
        }

        return ModerationDB.pushReports(missingReports);
    }

    public static void submitReport(String reportedUUID, String reporterUUID, Reasoning reasoning, Priority priority){
        Report report = new Report(
                getNextReportId(),
                reportedUUID,
                reporterUUID,
                Instant.now().getEpochSecond(),
                reasoning,
                priority,
                ReportState.OPEN);

        reportLog.add(report);

        if(HGLBot.instance != null){
            HGLBot.logReport(report);
        }
    }

    public static long getNextReportId(){
        return reportLog.size();
    }

    public static Priority getPriorityForReporter(Player player){
        PlayerData stats = PlayerData.getPlayerData(player);

        if(stats.getReportScore() < 0) return Priority.LOW;
        if(stats.getReportScore() > 5) return Priority.HIGH;
        return Priority.MEDIUM;
    }

    public void setReviewer(String reviewerName){
        this.reviewedBy = reviewerName;
    }

    public String getReviewedBy(){
        return this.reviewedBy;
    }
    public long getReportId() {
        return reportId;
    }

    public String getReportedUUID() {
        return reportedUUID;
    }

    public String getReporterUUID() {
        return reporterUUID;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public Reasoning getReasoning() {
        return reasoning;
    }

    public ReportState getState() {
        return state;
    }

    public Priority getPriority() {
        return priority;
    }

    public ArrayList<String> getReportedUserMessages() {
        return reportedUserMessages == null ? new ArrayList<>() : reportedUserMessages;
    }

    public void setPunishmentId(long punishmentId){
        this.punishmentId = punishmentId;
    }

    public long getPunishmentId(){
        return this.punishmentId;
    }
}
