package me.aragot.hglmoderation.admin.preset;

import me.aragot.hglmoderation.data.Reasoning;

import java.util.ArrayList;

public class Preset {

    private String presetName;
    private String presetDescription;
    private ArrayList<Reasoning> reasoningScope;
    private int start;
    private int end; // end == -1 -> Meaning no end;

    public Preset(String presetName, String presetDescription, int start, int end){
        this.presetName = presetName;
        this.presetDescription = presetDescription;
        this.reasoningScope = new ArrayList<>();
        this.start = start;
        this.end = end;
    }

    public void setPresetName(String presetName){
        this.presetName = presetName;
    }

    public String getPresetName() {
        return presetName;
    }

    public ArrayList<Reasoning> getReasoningScope() {
        return reasoningScope;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int start){
        this.start = start;
    }

    public void setEnd(int end){
        this.end = end;
    }

    public void addReasoningToScope(Reasoning reason){
        if(!this.reasoningScope.contains(reason))
            this.reasoningScope.add(reason);
    }

    public void removeReasoningFromScope(Reasoning reason){
        if(this.reasoningScope.contains(reason))
            this.reasoningScope.remove(reason);
    }

    public boolean isInScope(Reasoning reason){
        return this.reasoningScope.contains(reason);
    }

    public void setPresetDescription(String description){
        this.presetDescription = description;
    }

    public String getPresetDescription(){
        return this.presetDescription;
    }

}
