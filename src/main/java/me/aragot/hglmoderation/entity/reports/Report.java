package me.aragot.hglmoderation.entity.reports;

import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.events.PlayerListener;

import java.util.*;

public class Report {

    private final String _id;
    private final String reportedUUID; //Reported Player UUID

    //Maybe change to HashMap<String, Long> for uuid and submitDate?
    private final String reporterUUID; //Reporter Player UUID
    private final long submittedAt;
    private final Reasoning reasoning;
    private ReportState state;
    private final Priority priority;

    private String reviewedBy = ""; //Minecraft Player UUID
    private String punishmentId = "";
    private String discordLog = "";

    private ArrayList<String> reportedUserMessages;

    public Report(String reportId, String reportedUUID, String reporterUUID, long submittedAt, Reasoning reasoning, Priority priority, ReportState state){
        this._id = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.submittedAt = submittedAt;
        this.reasoning = reasoning;
        if(Reasoning.getChatReasons().contains(reasoning)) this.reportedUserMessages = PlayerListener.Companion.getUserMessages().get(reportedUUID);
        this.priority = priority;
        this.state = state;
    }

    //Used for ReportCodec
    public Report(String reportId, String reportedUUID, String reporterUUID, long submittedAt, Reasoning reasoning, ReportState state, Priority priority, String reviewedBy, String punishmentId, String discordLog, ArrayList<String> reportedUserMessages) {
        this._id = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.submittedAt = submittedAt;
        this.reasoning = reasoning;
        this.state = state;
        this.priority = priority;
        this.reviewedBy = reviewedBy;
        this.punishmentId = punishmentId;
        this.discordLog = discordLog;
        this.reportedUserMessages = reportedUserMessages;
    }

    public void setReviewedBy(String reviewerId) {
        this.reviewedBy = reviewerId;
    }

    public String getReviewedBy() {
        return this.reviewedBy;
    }
    public String getId() {
        return _id;
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

    public void setState(ReportState state) {
        this.state = state;
    }

    public Priority getPriority() {
        return priority;
    }

    public ArrayList<String> getReportedUserMessages() {
        return reportedUserMessages == null ? new ArrayList<>() : reportedUserMessages;
    }

    public void setPunishmentId(String punishmentId) {
        this.punishmentId = punishmentId;
    }

    public String getPunishmentId() {
        return this.punishmentId;
    }

    public String getDiscordLog() {
        return this.discordLog;
    }

    public void setDiscordLog(String messageId) {
        this.discordLog = messageId;
    }

    //Maybe separate as well?
    public String getFormattedState() {
        return this.getState() == ReportState.DONE ? "was already <blue>reviewed</blue>" : "is already <yellow>under review</yellow>";
    }
}