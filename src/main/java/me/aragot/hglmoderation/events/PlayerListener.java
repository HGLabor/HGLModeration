package me.aragot.hglmoderation.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

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

}
