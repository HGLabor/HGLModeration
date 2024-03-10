package me.aragot.hglmoderation.tools;

import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
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

    public static void notifyReporters(ArrayList<UUID> reporters){
        ProxyServer server = HGLModeration.instance.getServer();
        for(UUID reporter : reporters){
            server.getPlayer(reporter).ifPresent((player) -> {
                PlayerData data = PlayerData.getPlayerData(player);
                if(data.getNotifications().contains(Notification.REPORT_STATE))
                    Responder.respond(player, "Your report has been reviewed and accepted. Thanks for keeping this community safe.", ResponseType.SUCCESS);
            });
        }
    }
}
