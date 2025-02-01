package me.aragot.hglmoderation.service.player;

import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.entity.Notification;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.UUID;

public class Notifier {
    public static void notify(Notification group, Component component) {
        ProxyServer server = HGLModeration.instance.getServer();
        ArrayList<UUID> notifGroup = PlayerData.notificationGroups.get(group);
        if (notifGroup == null)
            return;

        for (UUID playerId : notifGroup) {
            server.getPlayer(playerId).ifPresent(player -> player.sendMessage(component));
        }
    }

    public static void notifyReporters(ArrayList<UUID> reporters) {
        ProxyServer server = HGLModeration.instance.getServer();
        PlayerDataRepository repository = new PlayerDataRepository();
        for (UUID reporter : reporters) {
            server.getPlayer(reporter).ifPresent((player) -> {
                PlayerData data = repository.getPlayerData(player);
                if (data.getNotifications().contains(Notification.REPORT_STATE))
                    Responder.respond(player, "Your report has been reviewed and accepted. Thanks for keeping this community safe.", ResponseType.SUCCESS);
            });
        }
    }
}
