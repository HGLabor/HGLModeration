package me.aragot.hglmoderation.data;


import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerData {
    private int reportScore; //Score of successful reports, needed to get Report Priority of a Player
    private int punishmentScore; // Punishment score of a Player, needed for punishment level

    private String playerId;
    private String discordId;
    private ArrayList<Notification> notifications;
    private ArrayList<String> punishments = new ArrayList<>();

    public static ArrayList<PlayerData> dataList = new ArrayList<>();
    public static HashMap<Notification, ArrayList<String>> notificationGroups = new HashMap<>();

    public PlayerData(Player player){
        this.playerId = player.getUniqueId().toString();
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

    public int getPunishmentScore() {
        return punishmentScore;
    }

    public void setPunishmentScore(int punishmentScore) {
        this.punishmentScore = punishmentScore;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
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
        if(this.notifications == null) this.notifications = new ArrayList<>();
        if(!this.notifications.contains(notif)) this.notifications.add(notif);
        if(notificationGroups.get(notif) == null) notificationGroups.put(notif, new ArrayList<>());

        notificationGroups.get(notif).add(this.playerId);
    }

    public void removeNotification(Notification notif){
        if(this.notifications == null) return;
        if(this.notifications.contains(notif)) this.notifications.remove(notif);
        if(notificationGroups.get(notif) == null) notificationGroups.put(notif, new ArrayList<>());

        notificationGroups.get(notif).remove(this.playerId);
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
