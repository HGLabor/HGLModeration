package me.aragot.hglmoderation.admin.preset;

import me.aragot.hglmoderation.data.Reasoning;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Preset {

    private String presetName;
    private String presetDescription;
    private ArrayList<Reasoning> reasoningScope;

    private int weight;
    private int start;
    private int end; // end == -1 -> Meaning no end;

    private long duration;

    public Preset(String presetName, String presetDescription, int start, int end, int weight){
        this.presetName = presetName;
        this.presetDescription = presetDescription;
        this.reasoningScope = new ArrayList<>();
        this.start = start;
        this.end = end;
    }

    public void setName(String presetName){
        this.presetName = presetName;
    }

    public String getName() {
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

    public void setReasoningScope(ArrayList<Reasoning> reasoningScope){
        this.reasoningScope = reasoningScope;
    }
    public boolean isInScope(Reasoning reason){
        return this.reasoningScope.contains(reason);
    }

    public void setDescription(String description){
        this.presetDescription = description;
    }

    public String getDescription(){
        return this.presetDescription;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getDuration(){
        return this.duration;
    }

    public void setDuration(long duration){
        this.duration = duration;
    }

    public String getDurationAsString(){
        if(this.duration == 0) return "No duration specified";

        long days = TimeUnit.SECONDS.toDays(duration);
        long hours = TimeUnit.SECONDS.toHours(duration) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(duration) % 60;

        String time = "";
        if(days != 0) time += days + "D ";
        if(hours != 0) time += hours + "H ";
        if(minutes != 0) time += minutes + "M ";

        return time;
    }
}
