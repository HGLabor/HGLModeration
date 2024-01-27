package me.aragot.hglmoderation.data;


import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;

public class PlayerData {
    private int reportScore; //Score of successful reports, needed to get Report Priority of a Player
    private int punishmentScore; // Punishment score of a Player, needed for punishment level

    private String playerId;
    private String discordId;

    public static ArrayList<PlayerData> dataList = new ArrayList<>();
    public PlayerData(Player player){
        this.playerId = player.getUniqueId().toString();
    }

    public PlayerData(int reportScore, int punishmentScore, String playerId){
        this.reportScore = reportScore;
        this.punishmentScore = punishmentScore;
        this.playerId = playerId;
    }

    public static PlayerData getPlayerData(Player player){
        for(PlayerData stats : dataList)
            if(stats.getPlayerId().equalsIgnoreCase(player.getUniqueId().toString())) return stats;
        PlayerData stats = new PlayerData(player);
        dataList.add(stats);
        return stats;
    }

    public int getPunishmentScore() {
        return punishmentScore;
    }

    public void setPunishmentScore(int punishmentScore) {
        this.punishmentScore = punishmentScore;
    }

    public String getPlayerId() {
        return playerId;
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

    public void setDiscordId(){
        this.discordId = discordId;
    }
}
