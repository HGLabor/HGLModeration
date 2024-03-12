package me.aragot.hglmoderation.data;

import java.util.ArrayList;

public enum Reasoning {
    HACKING,
    INSULTING,
    BAN_EVASION,
    BUG_ABUSE,
    TEAMING;

    public static ArrayList<Reasoning> getChatReasons(){
        ArrayList<Reasoning> chatReasons = new ArrayList<>();
        chatReasons.add(Reasoning.INSULTING);
        //Maybe add spam?
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
            case BAN_EVASION:
                return "Evading previous ban";
            case BUG_ABUSE:
                return "Abuse of Bugs/Glitches";
            default:
                return reasoning.name();
        }
    }
}
