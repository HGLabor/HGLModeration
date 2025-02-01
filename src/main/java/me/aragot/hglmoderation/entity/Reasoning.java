package me.aragot.hglmoderation.entity;

import java.util.ArrayList;
import java.util.List;

public enum Reasoning {
    HACKING,
    INSULTING,
    SPAM,
    INAPPROPRIATE_SPEECH,
    BAN_EVASION,
    BUG_ABUSE,
    TEAMING,
    CONTACT_TEAM;

    public static ArrayList<Reasoning> getChatReasons() {
        ArrayList<Reasoning> chatReasons = new ArrayList<>();
        chatReasons.add(INSULTING);
        chatReasons.add(INAPPROPRIATE_SPEECH);
        chatReasons.add(SPAM);
        return chatReasons;
    }

    public static String getPrettyReasoning(Reasoning reasoning) {
        switch(reasoning){
            case HACKING:
                return "Prohibited Modifications (Hacking/Cheating)";
            case SPAM:
                return "Spamming the chat";
            case INAPPROPRIATE_SPEECH:
                return "Using inappropriate speech";
            case INSULTING:
                return "Extensive Insulting";
            case TEAMING:
                return "unsportsmanlike Behavior (Teaming)";
            case BAN_EVASION:
                return "Evading previous ban";
            case BUG_ABUSE:
                return "Abuse of Bugs/Glitches";
            case CONTACT_TEAM:
                return "Please contact our Server-Team";
            default:
                return reasoning.name();
        }
    }

    public static List<Reasoning> getReportableReasonings() {
        ArrayList<Reasoning> reasons = new ArrayList<>(List.of(Reasoning.values()));
        reasons.remove(Reasoning.CONTACT_TEAM);

        return reasons;
    }
}
