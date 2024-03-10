package me.aragot.hglmoderation.data.reports;

import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.tools.Notifier;
import me.aragot.hglmoderation.tools.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


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

    private static ArrayList<Report> unfinishedReports = new ArrayList<>();

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

        HGLModeration.instance.getDatabase().pushReport(report);

        unfinishedReports.add(report);

        Notifier.notify(Notification.REPORT, report.getMCReportComponent(true));
    }

    public static void findOpenReports(){
        unfinishedReports = HGLModeration.instance.getDatabase().getUnfinishedReports();
    }

    public static Report getReportById(String id){
        for(Report report : unfinishedReports){
            if(id.equalsIgnoreCase(report.getId()))
                return report;
        }

        return HGLModeration.instance.getDatabase().getReportById(id);
    }

    public static List<Report> getReportsInProgress(){
        return unfinishedReports.stream().filter((report) -> report.getState() == ReportState.UNDER_REVIEW).collect(Collectors.toList());
    }

    public static List<Report> getOpenReports(){
        return unfinishedReports.stream().filter((report -> report.getState() == ReportState.OPEN)).collect(Collectors.toList());
    }

    public static ArrayList<Report> getReportsForPlayer(String uuid){
        return HGLModeration.instance.getDatabase().getReportsByPlayer(uuid);
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
        PlayerData data = PlayerData.getPlayerData(player);

        if(data.getReportScore() < 0) return Priority.LOW;
        if(data.getReportScore() > 5) return Priority.HIGH;
        return Priority.MEDIUM;
    }

    public void setReviewedBy(String reviewerId){
        this.reviewedBy = reviewerId;
    }

    public String getReviewedBy(){
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

    public void setState(ReportState state){
        this.state = state;
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
        String reportedUserName = PlayerUtils.getUsernameFromUUID(this.reportedUUID);
        String prio = "<white>Priority:</white> <red>" + this.priority.name() + "</red>";
        String reason = "<white>Reasoning:</white> <red>" + this.reasoning.name() + "</red>";
        String reportState = "<white>State:</white> <red>" + this.state.name() + "</red>";
        String reported = "<white>Reported:</white> <red>" + reportedUserName + "</red>";

        String viewDetails = getViewDetailsRaw();

        String reviewReport = "<click:run_command:'/review " + this.getId() + "'><white>[<yellow><b>Review</b></yellow>]</white></click>";

        String deserialize = "                    " + prio + "\n" +
                "                    " + reason + "\n" +
                "                    " + reported + "\n" +
                "                    " + reportState + "\n\n" +
                "                    " + viewDetails + "   " + reviewReport + "\n" +
                "<gold>===================================================</gold>";
        return mm.deserialize(deserialize);
    }

    public String getViewDetailsRaw(){
        String reportedUserName = PlayerUtils.getUsernameFromUUID(this.reportedUUID);
        String reporterUserName = PlayerUtils.getUsernameFromUUID(this.reporterUUID);

        String reportDetails = "<yellow><b>Report #" + this._id + "</b></yellow>\n\n" +
                "<gray>Reported Player:</gray> <red>" + reportedUserName + "</red>\n" +
                "<gray>Reported By:</gray> <red>" + reporterUserName + "</red>\n" +
                "<gray>Reasoning:</gray> <red>" + this.reasoning.name() + "</red>\n" +
                "<gray>Priority:</gray> <red>" + this.priority.name() + "</red>\n" +
                "<gray>Submitted at:</gray> <red>" + new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date(this.submittedAt * 1000)) + "</red>\n" +
                "<gray>State:</gray> <red>" + this.state.name() + "</red>";

        if(Reasoning.getChatReasons().contains(this.getReasoning()))
            reportDetails += "\n<gray>User Messages:</gray>\n" + this.getFormattedUserMessages();

        return "<hover:show_text:'" + reportDetails + "'><white>[<blue><b>View Details</b></blue>]</white></hover>";
    }

    public Component getMCReportActions(){
        MiniMessage mm = MiniMessage.miniMessage();
        Preset preset = PresetHandler.instance.getPresetForScore(this.reasoning, PlayerData.getPlayerData(this.getReportedUUID()).getPunishmentScore());
        String presetName = preset == null ? "None" : preset.getName();
        PlayerData data = PlayerData.getPlayerData(this.reportedUUID);

        String serverName;
        try {
            serverName = HGLModeration.instance.getServer().getPlayer(UUID.fromString(this.reportedUUID)).orElseThrow().getCurrentServer().orElseThrow().getServerInfo().getName();
        } catch(NoSuchElementException x){
            serverName = "None";
        }

        return mm.deserialize("<hover:show_text:'<green>Accept and Punish</green>'><click:suggest_command:'/preset apply " + presetName + " " + this.getId() + "'><white>[<green><b>Punish</b></green>]</white></click></hover>" +
                "   <hover:show_text:'<red>Decline Report</red>'><click:suggest_command:'/review " + this.getId() + " decline'><white>[<red><b>Decline</b></red>]</white></click></hover>" +
                "   <hover:show_text:'<red>Mark as malicious</red>'><click:suggest_command:'/review " + this.getId() + " malicious'><white>[<red><b>Decline & Mark as malicious</b></red>]</white></click></hover>\n" +
                "<hover:show_text:'" + data.getFormattedPunishments() + "'><white>[<blue><b>Previous Punishments</b></blue>]</white></hover>" +
                "   <hover:show_text:'" + getOtherFormattedReports() + "'><white>[<blue><b>Other Reports</b></blue>]</white></hover>\n" +
                "<hover:show_text:'<blue>Teleport to Server</blue>'><click:run_command:'/server " + serverName + "'><white>[<blue><b>Follow Player</b></blue>]</white></click></hover>");
    }

    public Component getMCReportComponent(boolean incoming){
        MiniMessage mm = MiniMessage.miniMessage();
        if(incoming)
            return mm.deserialize("<gold>============= <white>Incoming</white> <red>Report: #" + this.getId() + "</red> =============</gold>\n")
                    .append(getMcReportOverview());

        return mm.deserialize("<gold>================= <red>Report: #" + this.getId() + "</red> =================</gold>\n")
                .append(getMcReportOverview());
    }

    private String getFormattedUserMessages(){
        if(this.reportedUserMessages == null || this.reportedUserMessages.isEmpty())
            return "<gray>No Messages sent</gray>";

        StringBuilder messages = new StringBuilder();

        String username;
        try {
            username = HGLModeration.instance.getServer().getPlayer(UUID.fromString(this.reportedUUID)).orElseThrow().getUsername();
        } catch(NoSuchElementException x){
            username = PlayerUtils.getUsernameFromUUID(this.reportedUUID);
        }

        for(String message : this.getReportedUserMessages())
            messages.append("\n<red>").append(username).append("</red>: ").append(message);

        return messages.toString();
    }

    public String getFormattedState(){
        return this.getState() == ReportState.DONE ? "was already <blue>reviewed</blue>" : "is already <yellow>under review</yellow>";
    }

    private String getOtherFormattedReports(){
        ArrayList<Report> reports = HGLModeration.instance.getDatabase().getReportsForPlayerExcept(this.getReportedUUID(), this._id);
        if(reports.isEmpty()) return "No Reports found";
        StringBuilder formatted = new StringBuilder("<gray><blue>ID</blue>   |   <blue><b>State</b></blue>   |   <blue>Reason</blue>");

        for(Report report : reports) {
            formatted.append("\n<gray>").append(report.getId()).append(" |</gray> <yellow>")
                    .append(report.getState().name()).append("</yellow> <gray>|</gray> <red>")
                    .append(report.getReasoning()).append("</red>");
        }

        return formatted.toString();
    }

    public void startReview(String reviewer){
        unfinishedReports.forEach((report -> {
            if(!report.getReportedUUID().equalsIgnoreCase(this.getReportedUUID()) || (report.getReasoning() != this.getReasoning()))
                return;
            report.setReviewedBy(reviewer);
            report.setState(ReportState.UNDER_REVIEW);
        }));

        HGLModeration.instance.getDatabase().updateReportsBasedOn(this);
    }

    public void decline(){
        List<Report> reviewableReports = unfinishedReports.stream().filter(
                (report) -> report.getReportedUUID().equalsIgnoreCase(this.getReportedUUID()) && report.getReasoning() == this.getReasoning()
        ).collect(Collectors.toList());

        if(HGLModeration.instance.getDatabase().updateReportsBasedOn(this)){
            unfinishedReports.removeAll(reviewableReports);
        }
    }

    public void malicious(){
        this.decline();

        PlayerData data = PlayerData.getPlayerData(this.reporterUUID);
        data.setReportScore(data.getPunishmentScore() - 2);
    }

    public void accept(String punishmentId){
        List<Report> reviewableReports = unfinishedReports.stream().filter(
                (report) -> report.getReportedUUID().equalsIgnoreCase(this.getReportedUUID()) && report.getReasoning() == this.getReasoning()
        ).collect(Collectors.toList());
        this.setPunishmentId(punishmentId);
        this.setState(ReportState.DONE);

        ArrayList<UUID> reporters = new ArrayList<>();
        for(Report report : reviewableReports)
            reporters.add(UUID.fromString(report.getReporterUUID()));

        HGLModeration.instance.getDatabase().updateReportsBasedOn(this);

        Notifier.notifyReporters(reporters);
    }
}