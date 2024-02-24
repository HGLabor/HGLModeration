package me.aragot.hglmoderation.data.reports;

import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.Notifier;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


public class Report {

    private final String _id;
    private final String reportedUUID; //Reported Player UUID
    private final String reporterUUID; //Reporter Player UUID
    private final long submittedAt;
    private final Reasoning reasoning;
    private ReportState state;
    private final Priority priority;

    private String reviewedBy; //Minecraft Player UUID
    private String punishmentId;
    private String discordLog;

    private ArrayList<String> reportedUserMessages;

    public Report(String reportId, String reportedUUID, String reporterUUID, long submittedAt, Reasoning reasoning, Priority priority, ReportState state){
        this._id = reportId;
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

        Notifier.notify(Notification.REPORT, report.getMCReportComponent(true));
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

    public Component getMcReportOverview(){
        MiniMessage mm = MiniMessage.miniMessage();
        String prio = "<white>Priority:</white> <red>" + this.priority.name() + "</red>";
        String reason = "<white>Reasoning:</white> <red>" + this.reasoning.name() + "</red>";
        String reportState = "<white>State:</white> <red>" + this.state.name() + "</red>";
        String reported = "<white>Reported:</white> <red>" + HGLModeration.instance.getServer().getPlayer(UUID.fromString(this.reportedUUID)).get().getUsername() + "</red>";
        String reportDetails = "<yellow><b>Report #" + this._id + "</b></yellow>\n\n" +
                "<gray>Reported Player:</gray> <red>" +  HGLModeration.instance.getServer().getPlayer(UUID.fromString(this.reportedUUID)).get().getUsername() + "</red>\n" +
                "<gray>Reported By:</gray> <red>" + HGLModeration.instance.getServer().getPlayer(UUID.fromString(this.reporterUUID)).get().getUsername() + "</red>\n" +
                "<gray>Reasoning:</gray> <red>" + this.reasoning.name() + "</red>\n" +
                "<gray>Priority:</gray> <red>" + this.priority.name() + "</red>\n" +
                "<gray>Submitted at: <red>" + new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date(this.submittedAt * 1000)) + "</red>\n" +
                "<gray>State:</gray> <red>" + this.state.name() + "</red>";

        String viewDetails = "<hover:show_text:'" + reportDetails + "'><white>[<blue><b>View Details</b></blue>]</white></hover>";
        String deserialize = prio + " " + reason + " " + reported + " " + reportState + " " + viewDetails;
        return mm.deserialize(deserialize);
    }

    public Component getMCReportActions(){
        MiniMessage mm = MiniMessage.miniMessage();
        //Missing Punishment preset for command
        // /review <punishmentId> <boolean:accept> <preset/punishReporter>
        // /punish <type:[ban/mute/]> <boolean:notifReporters?trueByDefault>
        return mm.deserialize("<click:suggest_command:'/punishment preset'><green><b>[Punish]</b></green></click>" +
                "   <click:suggest_command:'/review id false'><red><b>[Decline]</b></red></click>" +
                "   <click:suggest_command:'/review id false decreasePriority'><red><b>[Decline & mark as malicious]</b></red></click>" +
                "   <click:run_command:'/server " + HGLModeration.instance.getServer().getPlayer(UUID.fromString(this.reportedUUID)).get().getCurrentServer().get().getServerInfo().getName() + "'>" +
                "<blue><b>[Teleport to Server]</b></blue></click>");
    }

    public Component getMCReportComponent(boolean incoming){
        MiniMessage mm = MiniMessage.miniMessage();
        if(incoming)
            return mm.deserialize(Responder.prefix + " <b><white>INCOMING</white> <red>REPORT</red></b>\n")
                    .append(getMcReportOverview())
                    .append(Component.text("\n")).append(getMCReportActions());

        return mm.deserialize(Responder.prefix + " <b><yellow>Report #" + this._id + "</yellow></b>\n")
                .append(getMcReportOverview())
                .append(Component.text("\n")).append(getMCReportActions());
    }
}
