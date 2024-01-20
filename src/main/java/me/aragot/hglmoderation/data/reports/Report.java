package me.aragot.hglmoderation.data.reports;

import java.util.ArrayList;

public class Report {

    private long reportId;
    private String reportedUUID; //Reported Player UUID
    private String reporterUUID; //Reporter Player UUID
    private long unixTimeStamp;
    private Reasoning reasoning;
    private ReportState state;
    private Priority priority;

    private static ArrayList<Report> reportLog = new ArrayList<>();

    public Report(long reportId, String reportedUUID, String reporterUUID,  long unixTimeStamp, Reasoning reasoning, Priority priority, ReportState state){
        this.reportId = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.unixTimeStamp = unixTimeStamp;
        this.reasoning = reasoning;
        this.priority = priority;
        this.state = state;
    }



    public static void submitReport(String reportedUUID, String reporterUUID,  long unixTimeStamp, Reasoning reasoning, Priority priority){
        //Discord Webhook integration
        //Database submission
        Report report = new Report(
                getNextReportId(),
                reportedUUID,
                reporterUUID,
                unixTimeStamp,
                reasoning,
                priority,
                ReportState.OPEN);

        reportLog.add(report);
    }

    public static long getNextReportId(){
        return reportLog.size();
    }

    public static Priority getPriorityForReporter(){

        return Priority.MEDIUM;
    }
}
