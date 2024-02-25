package me.aragot.hglmoderation.data;

import java.util.ArrayList;

public enum Reasoning {
    HACKING,
    INSULTING,
    TEAMING;

    public static ArrayList<Reasoning> getChatReasons(){
        ArrayList<Reasoning> chatReasons = new ArrayList<>();
        chatReasons.add(Reasoning.INSULTING);
        return chatReasons;
    }

    public static String getPrettyReasoning(Reasoning reasoning){
        switch(reasoning){
            case HACKING:
                return "Unallowed Modifications (Hacking/Cheating)";
            case INSULTING:
                return "Extensive Insulting";
            case TEAMING:
                return "unsportsmanlike Behavior (Teaming)";
            default:
                return reasoning.name();
        }
    }
}
