package me.aragot.hglmoderation.entity;


import com.velocitypowered.api.proxy.Player;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    private int reportScore; //Score of successful reports, needed to get Report Priority of a Player
    private int punishmentScore; // Punishment score of a Player, needed for punishment level

    private UUID _id;
    private String latestIp;
    private String discordId = "";
    private ArrayList<Notification> notifications = new ArrayList<>();
    private ArrayList<String> punishments = new ArrayList<>();

    @BsonIgnore
    public static HashMap<Notification, ArrayList<UUID>> notificationGroups = new HashMap<>();

    public PlayerData(Player player) {
        this._id = player.getUniqueId();
        this.latestIp = player.getRemoteAddress().getAddress().getHostAddress();
        this.notifications.add(Notification.GENERAL);
        this.notifications.add(Notification.REPORT_STATE);
    }

    // Used for Codec
    public PlayerData() {
    }

    public int getPunishmentScore() {
        return punishmentScore;
    }

    public void setPunishmentScore(int punishmentScore) {
        this.punishmentScore = punishmentScore;
    }

    public UUID getId() {
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

    @BsonIgnore
    public void addNotification(Notification notif) {
        if (!this.notifications.contains(notif)) this.notifications.add(notif);
        notificationGroups.computeIfAbsent(notif, k -> new ArrayList<>());

        notificationGroups.get(notif).add(this._id);
    }

    @BsonIgnore
    public void removeNotification(Notification notif) {
        this.notifications.remove(notif);
        notificationGroups.computeIfAbsent(notif, k -> new ArrayList<>());

        notificationGroups.get(notif).remove(this._id);
    }

    public ArrayList<String> getPunishments() {
        return this.punishments;
    }

    @BsonIgnore
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
        if (this.notifications == null) this.notifications = new ArrayList<>();
        return this.notifications;
    }

    public void setId(UUID _id) {
        this._id = _id;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    public void setPunishments(ArrayList<String> punishments) {
        this.punishments = punishments;
    }

    public static void setNotificationGroups(HashMap<Notification, ArrayList<UUID>> notificationGroups) {
        PlayerData.notificationGroups = notificationGroups;
    }
}
