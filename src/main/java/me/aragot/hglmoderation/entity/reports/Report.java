package me.aragot.hglmoderation.entity.reports;

import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.events.PlayerListener;

import java.util.*;

public class Report {

    private String _id;
    private UUID reportedUUID; //Reported Player UUID

    //Maybe change to HashMap<UUID, Long> for uuid and submitDate?
    private UUID reporterUUID; //Reporter Player UUID
    private long submittedAt;
    private Reasoning reasoning;
    private ReportState state;
    private Priority priority;

    private UUID reviewedBy; //Minecraft Player UUID
    private String punishmentId;
    private String discordLog;

    private ArrayList<String> reportedUserMessages;

    public Report(String reportId, UUID reportedUUID, UUID reporterUUID, long submittedAt, Reasoning reasoning, Priority priority, ReportState state){
        this._id = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.submittedAt = submittedAt;
        this.reasoning = reasoning;
        if(Reasoning.getChatReasons().contains(reasoning)) this.reportedUserMessages = PlayerListener.Companion.getUserMessages().get(reportedUUID);
        this.priority = priority;
        this.state = state;
    }

    // Used for Codec
    public Report() {}

    public void setReviewedBy(UUID reviewerId) {
        this.reviewedBy = reviewerId;
    }

    public UUID getReviewedBy() {
        return this.reviewedBy;
    }
    public String getId() {
        return _id;
    }

    public UUID getReportedUUID() {
        return reportedUUID;
    }

    public UUID getReporterUUID() {
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

    public void setId(String _id) {
        this._id = _id;
    }

    public void setReportedUUID(UUID reportedUUID) {
        this.reportedUUID = reportedUUID;
    }

    public void setReporterUUID(UUID reporterUUID) {
        this.reporterUUID = reporterUUID;
    }

    public void setSubmittedAt(long submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setReasoning(Reasoning reasoning) {
        this.reasoning = reasoning;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setReportedUserMessages(ArrayList<String> reportedUserMessages) {
        this.reportedUserMessages = reportedUserMessages;
    }
}