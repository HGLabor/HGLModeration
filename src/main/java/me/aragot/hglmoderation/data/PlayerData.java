package me.aragot.hglmoderation.data;


import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    private int reportScore; //Score of successful reports, needed to get Report Priority of a Player
    private int punishmentScore; // Punishment score of a Player, needed for punishment level

    private String _id;
    private String discordId = "";
    private ArrayList<Notification> notifications = new ArrayList<>();
    private ArrayList<String> punishments = new ArrayList<>();

    public static ArrayList<PlayerData> dataList = new ArrayList<>();
    public static HashMap<Notification, ArrayList<String>> notificationGroups = new HashMap<>();

    public PlayerData(Player player){
        this._id = player.getUniqueId().toString();
        this.notifications.add(Notification.GENERAL);
        this.notifications.add(Notification.REPORT_STATE);
    }

    //Used only for Codec
    public PlayerData(String _id, int reportScore, int punishmentScore, String discordId, ArrayList<Notification> notifications, ArrayList<String> punishments) {
        this.reportScore = reportScore;
        this.punishmentScore = punishmentScore;
        this._id = _id;
        this.discordId = discordId;
        this.notifications = notifications;
        this.punishments = punishments;
    }

    public static PlayerData getPlayerData(Player player){
        for(PlayerData data : dataList)
            if(data.getPlayerId().equalsIgnoreCase(player.getUniqueId().toString())) return data;


        PlayerData data = HGLModeration.instance.getDatabase().getPlayerDataById(player.getUniqueId().toString());
        if(data == null){
            data = new PlayerData(player);
            HGLModeration.instance.getDatabase().pushPlayerData(data);
        }

        dataList.add(data);
        return data;
    }

    public static PlayerData getPlayerData(String uuid){
        for(PlayerData data : dataList)
            if(data.getPlayerId().equalsIgnoreCase(uuid)) return data;


        return HGLModeration.instance.getDatabase().getPlayerDataById(uuid);
    }

    public int getPunishmentScore() {
        return punishmentScore;
    }

    public void setPunishmentScore(int punishmentScore) {
        this.punishmentScore = punishmentScore;
    }

    public String getPlayerId() {
        return this._id;
    }

    public void setPlayerId(String playerId) {
        this._id = playerId;
    }

    public int getReportScore() {
        return reportScore;
    }

    public void setReportScore(int reportScore) {
        this.reportScore = reportScore;
    }

    public String getDiscordId(){
        return this.discordId;
    }

    public void setDiscordId(String discordId){
        this.discordId = discordId;
    }

    public void addNotification(Notification notif){
        if(!this.notifications.contains(notif)) this.notifications.add(notif);
        if(notificationGroups.get(notif) == null) notificationGroups.put(notif, new ArrayList<>());

        notificationGroups.get(notif).add(this._id);
    }

    public void removeNotification(Notification notif){
        this.notifications.remove(notif);
        if(notificationGroups.get(notif) == null) notificationGroups.put(notif, new ArrayList<>());

        notificationGroups.get(notif).remove(this._id);
    }

    public ArrayList<String> getPunishments(){
        return this.punishments;
    }

    public void addPunishment(String id){
        this.punishments.add(id);
    }

    public ArrayList<Notification> getNotifications(){
        if(this.notifications == null) this.notifications = new ArrayList<>();
        return this.notifications;
    }
}
