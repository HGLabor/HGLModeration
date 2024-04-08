package me.aragot.hglmoderation.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.punishments.PunishmentType;
import me.aragot.hglmoderation.tools.PlayerUtils;

import java.time.Instant;
import java.util.*;

public class PlayerListener {

    public static HashMap<String, ArrayList<String>> userMessages = new HashMap<>();
    public static HashMap<String, Punishment> playerMutes = new HashMap<>();

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event){

        Punishment mute = playerMutes.get(event.getPlayer().getUniqueId().toString());

        if(mute != null){
            if(mute.isActive()){
                event.setResult(PlayerChatEvent.ChatResult.denied());
                event.getPlayer().sendMessage(mute.getMuteComponent());
                return;
            }
            playerMutes.remove(event.getPlayer().getUniqueId().toString());
        }

        ArrayList<String> messages = userMessages.get(event.getPlayer().getUniqueId().toString());
        if(messages == null){
            userMessages.put(event.getPlayer().getUniqueId().toString(), new ArrayList<>(Collections.singletonList(event.getMessage())));
            return;
        }

        if(messages.size() != 15){
            messages.add(event.getMessage());
            return;
        }

        messages.remove(0);
        messages.add(event.getMessage());
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent event){
        PlayerData data = PlayerData.getPlayerData(event.getPlayer());
        String hostAddress = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        data.setLatestIp(hostAddress);
        ArrayList<Punishment> activePunishments = Punishment.getActivePunishmentsFor(data.getPlayerId(), hostAddress);

        if(!activePunishments.isEmpty()){
            for(Punishment punishment : activePunishments) {
                if(!data.getPunishments().contains(punishment.getId()))
                    data.addPunishment(punishment.getId());
                if(punishment.getTypes().contains(PunishmentType.BAN) || punishment.getTypes().contains(PunishmentType.IP_BAN)){
                    punishment.enforce(event);
                    return;
                } else if(punishment.getTypes().contains(PunishmentType.MUTE)){
                    punishment.enforce(event.getPlayer());
                }
            }
        } else if(!data.getPunishments().isEmpty() && data.getPunishmentScore() > 0){
            Punishment latest = Punishment.getPunishmentById(data.getPunishments().get(data.getPunishments().size() - 1));
            //Reset score after one year since last punishment
            if(latest.getEndsAtTimestamp() + (60 * 60 * 24 * 365) <= Instant.now().getEpochSecond()){
                data.setPunishmentScore(0);
            }
        }

        String uuid = event.getPlayer().getUniqueId().toString();
        for(Notification notif : data.getNotifications()){
            PlayerData.notificationGroups.computeIfAbsent(notif, k -> new ArrayList<>());
            PlayerData.notificationGroups.get(notif).add(uuid);
        }

    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event){
        PlayerData data = PlayerData.getPlayerData(event.getPlayer());

        String uuid = event.getPlayer().getUniqueId().toString();
        for(Notification notif : data.getNotifications())
            PlayerData.notificationGroups.get(notif).remove(uuid);

        playerMutes.remove(event.getPlayer().getUniqueId().toString());
        userMessages.remove(uuid);
        HGLModeration.instance.getDatabase().updatePlayerData(data);
        PlayerData.dataList.remove(data);
        PlayerUtils.removePlayerFromCache(event.getPlayer().getUniqueId().toString());
    }

}
