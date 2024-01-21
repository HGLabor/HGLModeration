package me.aragot.hglmoderation.data;


import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;

public class PlayerStats {
    private int reportScore; //Score of successful reports, needed to get Priority of a Player
    private int punishmentScore; // Punishment score of a Player, needed for punishment level

    private String playerId;

    public static ArrayList<PlayerStats> statList = new ArrayList<>();
    public PlayerStats(Player player){
        this.playerId = player.getUniqueId().toString();
    }

    public PlayerStats(int reportScore, int punishmentScore, String playerId){
        this.reportScore = reportScore;
        this.punishmentScore = punishmentScore;
        this.playerId = playerId;
    }

    public static PlayerStats getPlayerStats(Player player){
        for(PlayerStats stats : statList)
            if(stats.getPlayerId().equalsIgnoreCase(player.getUniqueId().toString())) return stats;
        PlayerStats stats = new PlayerStats(player);
        statList.add(stats);
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
}
