package me.aragot.hglmoderation.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.punishments.Punishment;

import java.util.*;

public class PlayerListener {

    public static HashMap<String, ArrayList<String>> userMessages = new HashMap<>();

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event){
        ArrayList<String> messages =  userMessages.get(event.getPlayer().getUniqueId().toString());
        if(messages == null){
            userMessages.put(event.getPlayer().getUniqueId().toString(), new ArrayList<>(Arrays.asList(event.getMessage())));
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
        HGLModeration.instance.getLogger().info("Player UUID: " + event.getPlayer().getUniqueId().toString());
        PlayerData data = PlayerData.getPlayerData(event.getPlayer());

        if(!data.getPunishments().isEmpty()){
            Punishment punishment = Punishment.getPunishmentById(data.getPunishments().get( data.getPunishments().size() - 1));
            //punishment null == error contact staff;
            //else check if still active
        }


        String uuid = event.getPlayer().getUniqueId().toString();
        for(Notification notif : data.getNotifications()){
            if(PlayerData.notificationGroups.get(notif) == null) PlayerData.notificationGroups.put(notif, new ArrayList<>());
            PlayerData.notificationGroups.get(notif).add(uuid);
        }

    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event){
        PlayerData data = PlayerData.getPlayerData(event.getPlayer());

        String uuid = event.getPlayer().getUniqueId().toString();
        for(Notification notif : data.getNotifications())
            PlayerData.notificationGroups.get(notif).remove(uuid);

        HGLModeration.instance.getDatabase().updatePlayerData(data);
        PlayerData.dataList.remove(data);
    }

}
