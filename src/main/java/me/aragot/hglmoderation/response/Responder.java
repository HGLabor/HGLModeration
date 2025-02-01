package me.aragot.hglmoderation.response;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;

//Used for Ingame Responses to the Player
public class Responder {
    public static final String prefix = "<dark_gray> | <c:#24ade3>HG</c:#24ade3><c:#96c6cf>Labor</c:#96c6cf> » </dark_gray>";
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static void respond(CommandSource source, String raw, ResponseType type){
        switch(type){
            case ERROR:
                source.sendMessage(mm.deserialize(prefix + "<red> " + raw));
                break;
            case DEFAULT:
                source.sendMessage(mm.deserialize(prefix + " " + raw));
                break;
            default:
                source.sendMessage(mm.deserialize(prefix + "<yellow> " + raw));
                break;
        }
    }
}
