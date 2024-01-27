package me.aragot.hglmoderation.response;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Responder {

    public static final String prefix = "<white>[<aqua>HGLModeration</aqua>]</white>";
    private static final MiniMessage mm = MiniMessage.miniMessage();
    public static void respond(Player player, String raw, ResponseType type){

        switch(type){
            case ERROR:
                player.sendMessage(mm.deserialize(prefix + " <red>" + raw));
                break;
            case DEFAULT:
                player.sendMessage(mm.deserialize(prefix + " " + raw));
                break;
            default:
                player.sendMessage(mm.deserialize(prefix + "<yellow> " + raw));
                break;
        }

    }
}
