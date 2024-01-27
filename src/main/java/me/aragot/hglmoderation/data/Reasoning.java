package me.aragot.hglmoderation.data;

import java.util.ArrayList;

public enum Reasoning {
    HACKING,
    INAPPROPRIATE_NAME,
    INSULTING,
    TEAMING;

    public static ArrayList<Reasoning> getChatReasons(){
        ArrayList<Reasoning> chatReasons = new ArrayList<>();
        chatReasons.add(Reasoning.INSULTING);
        return chatReasons;
    }
}
