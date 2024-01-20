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
}
