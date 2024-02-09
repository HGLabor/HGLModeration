package me.aragot.hglmoderation.data.reports;

import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.response.ResponseType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Instant;
import java.util.*;


public class Report {

    private String reportId;
    private String reportedUUID; //Reported Player UUID
    private String reporterUUID; //Reporter Player UUID
    private long submittedAt;
    private Reasoning reasoning;
    private ReportState state;
    private Priority priority;

    private String reviewedBy; //Minecraft Player UUID
    private String punishmentId;
    private String discordLog;

    private ArrayList<String> reportedUserMessages;

    public Report(String reportId, String reportedUUID, String reporterUUID,  long submittedAt, Reasoning reasoning, Priority priority, ReportState state){
        this.reportId = reportId;
        this.reportedUUID = reportedUUID;
        this.reporterUUID = reporterUUID;
        this.submittedAt = submittedAt;
        this.reasoning = reasoning;
        if(Reasoning.getChatReasons().contains(reasoning)) this.reportedUserMessages = PlayerListener.userMessages.get(reportedUUID);
        this.priority = priority;
        this.state = state;
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

        if(HGLBot.instance == null) return;

        HGLBot.logReport(report);

        if(!HGLModeration.instance.getDatabase().pushReport(report)){
            TextChannel channel = HGLBot.instance.getTextChannelById(Config.instance.getReportChannelId());

            if(channel == null) return;
            channel.sendMessageEmbeds(
                    HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't push report to Database (ID:" + report.getReportId() + ")").build()
            ).queue();

        }
    }

    public static String getNextReportId(){
        //table is hex number
        //Report id is random 8 digit hex number
        String [] table = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        boolean isUnique = false;
        String id = "";
        while(!isUnique){
            Random rand = new Random();

            for(int i = 0; i < 8; i++)
                id += table[rand.nextInt(16)];

            if(HGLModeration.instance.getDatabase().getReportById(id) != null){
                id = "";
                continue;
            }

            isUnique = true;
        }

        return id;
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
    public String getReportId() {
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

    public void setPunishmentId(String punishmentId){
        this.punishmentId = punishmentId;
    }

    public String getPunishmentId(){
        return this.punishmentId;
    }

    public String getDiscordLog(){
        return this.discordLog;
    }

    public void setDiscordLog(String messageId){
        this.discordLog = messageId;
    }
}
