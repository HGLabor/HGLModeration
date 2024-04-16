package me.aragot.hglmoderation.entity;


import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.repository.PunishmentRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerData {
    private int reportScore; //Score of successful reports, needed to get Report Priority of a Player
    private int punishmentScore; // Punishment score of a Player, needed for punishment level

    private final String _id;
    private String latestIp;
    private String discordId = "";
    private ArrayList<Notification> notifications = new ArrayList<>();
    private ArrayList<String> punishments = new ArrayList<>();

    public static HashMap<Notification, ArrayList<String>> notificationGroups = new HashMap<>();

    public PlayerData(Player player) {
        this._id = player.getUniqueId().toString();
        this.latestIp = player.getRemoteAddress().getAddress().getHostAddress();
        this.notifications.add(Notification.GENERAL);
        this.notifications.add(Notification.REPORT_STATE);
    }

    //Used only for Codec
    public PlayerData(String _id,String latestIp, int reportScore, int punishmentScore, String discordId, ArrayList<Notification> notifications, ArrayList<String> punishments) {
        this.reportScore = reportScore;
        this.punishmentScore = punishmentScore;
        this._id = _id;
        this.latestIp = latestIp;
        this.discordId = discordId;
        this.notifications = notifications;
        this.punishments = punishments;
    }

    public int getPunishmentScore() {
        return punishmentScore;
    }

    public String getFormattedPunishments() {
        if (this.getPunishments().isEmpty()) return "No Punishments found";
        PunishmentRepository repository = new PunishmentRepository();
        ArrayList<Punishment> punishments = repository.getPunishmentsFor(this.getPlayerId(), this.getLatestIp());
        StringBuilder formatted = new StringBuilder("<gray><blue>ID</blue>   |   <blue>Type</blue>   |   <blue>Reason</blue>   |   <blue>Status</blue></gray>");
        for (Punishment punishment : punishments) {
            formatted.append("\n<gray>").append(punishment.getId()).append(" |</gray> <yellow>")
                    .append(punishment.getTypesAsString()).append("</yellow> <gray>|</gray> <red>")
                    .append(punishment.getReasoning()).append("</red> <gray>|</gray> ")
                    .append(punishment.isActive() ? "<green>⊙</green>" : "<red>⊙</red>");
        }

        return formatted.toString();
    }

    public void setPunishmentScore(int punishmentScore) {
        this.punishmentScore = punishmentScore;
    }

    public String getPlayerId() {
        return this._id;
    }

    public int getReportScore() {
        return reportScore;
    }

    public void setReportScore(int reportScore) {
        this.reportScore = reportScore;
    }

    public String getDiscordId() {
        return this.discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public void addNotification(Notification notif) {
        if(!this.notifications.contains(notif)) this.notifications.add(notif);
        notificationGroups.computeIfAbsent(notif, k -> new ArrayList<>());

        notificationGroups.get(notif).add(this._id);
    }

    public void removeNotification(Notification notif) {
        this.notifications.remove(notif);
        notificationGroups.computeIfAbsent(notif, k -> new ArrayList<>());

        notificationGroups.get(notif).remove(this._id);
    }

    public ArrayList<String> getPunishments() {
        return this.punishments;
    }

    public void addPunishment(String id) {
        this.punishments.add(id);
    }

    public String getLatestIp() {
        return this.latestIp;
    }

    public void setLatestIp(String ip) {
        this.latestIp = ip;
    }

    public ArrayList<Notification> getNotifications() {
        if(this.notifications == null) this.notifications = new ArrayList<>();
        return this.notifications;
    }
}
