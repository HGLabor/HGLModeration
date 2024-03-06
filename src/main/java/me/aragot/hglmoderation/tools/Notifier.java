package me.aragot.hglmoderation.tools;

import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.reports.Report;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.UUID;

public class Notifier {

    public static void notify(Notification group, Component component){
        ProxyServer server = HGLModeration.instance.getServer();
        ArrayList<String> notifGroup = PlayerData.notificationGroups.get(group);
        if(notifGroup == null)
            return;

        for(String playerId : notifGroup){
            server.getPlayer(UUID.fromString(playerId)).ifPresent(player -> player.sendMessage(component));
        }
    }

    public void notifyReporters(Report report){

    }
}
