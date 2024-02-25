package me.aragot.hglmoderation.admin.preset;

import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.punishments.PunishmentType;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.database.ModerationDB;
import me.aragot.hglmoderation.tools.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Preset {

    private String presetName;
    private String presetDescription;
    private ArrayList<Reasoning> reasoningScope;

    private ArrayList<PunishmentType> punishmentsTypes;

    private int weight;
    private int start;
    private int end; // end == -1 -> Meaning no end;

    private long duration; // duration == -1 -> permanent;

    public Preset(String presetName, String presetDescription, int start, int end, int weight){
        this.presetName = presetName;
        this.presetDescription = presetDescription;
        this.punishmentsTypes = new ArrayList<>();
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
        return this.weight;
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
        else if(this.duration == -1) return "Permanent";

        long days = TimeUnit.SECONDS.toDays(duration);
        long hours = TimeUnit.SECONDS.toHours(duration) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(duration) % 60;

        String time = "";
        if(days != 0) time += days + "d ";
        if(hours != 0) time += hours + "h ";
        if(minutes != 0) time += minutes + "min ";

        return time;
    }

    public ArrayList<PunishmentType> getPunishmentsTypes() {
        return punishmentsTypes;
    }

    public void setPunishmentsTypes(ArrayList<PunishmentType> punishmentsTypes) {
        this.punishmentsTypes = punishmentsTypes;
    }

    public long getDays(){
        return TimeUnit.SECONDS.toDays(duration);
    }

    public long getHours(){
        return TimeUnit.SECONDS.toHours(duration) % 24;
    }

    public long getMinutes(){
        return TimeUnit.SECONDS.toMinutes(duration) % 60;
    }

    public boolean isInRange(int score){
        return (score >= this.start && score < this.end) || (this.end == -1 && score >= this.start);
    }

    public String getReasoningScopeAsString(){
        StringBuilder builder = new StringBuilder();

        for(Reasoning reasoning : this.getReasoningScope())
            builder.append(reasoning.name()).append(",");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }

    public String getPunishmentTypesAsString(){
        StringBuilder builder = new StringBuilder();

        for(PunishmentType reasoning : this.getPunishmentsTypes())
            builder.append(reasoning.name()).append(",");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }

    public void apply(Report report){
        PlayerData data = PlayerData.getPlayerData(report.getReportedUUID());
        data.setPunishmentScore(data.getPunishmentScore() + this.getWeight());
        Punishment.submitPunishmentFromReport(data, report, this.getPunishmentsTypes(), Instant.now().getEpochSecond() + this.getDuration(), "Preset Used: " + this.getName());
    }
}
