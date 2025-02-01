package me.aragot.hglmoderation.admin.preset;

import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.punishments.PunishmentType;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.exceptions.DatabaseException;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.service.punishment.PunishmentManager;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Preset {
    private UUID _id;

    private String presetName;
    private String presetDescription;

    private ArrayList<Reasoning> reasoningScope = new ArrayList<>();
    private ArrayList<PunishmentType> punishmentsTypes = new ArrayList<>();

    private int weight;
    private int start;
    private int end; // end == -1 -> Meaning no end;

    private long duration; // duration == -1 -> permanent;

    public Preset(String presetName, String presetDescription, int start, int end, int weight) {
        this._id = UUID.randomUUID();
        this.presetName = presetName;
        this.presetDescription = presetDescription;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public Preset() {
    }

    public UUID getId() {
        return this._id;
    }

    public void setId(UUID id) {
        this._id = id;
    }

    public void setName(String presetName) {
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

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setReasoningScope(ArrayList<Reasoning> reasoningScope) {
        this.reasoningScope = reasoningScope;
    }

    @BsonIgnore
    public boolean isInScope(Reasoning reason) {
        return this.reasoningScope.contains(reason);
    }

    public void setDescription(String description) {
        this.presetDescription = description;
    }

    public String getDescription() {
        return this.presetDescription;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @BsonIgnore
    public String getDurationAsString() {
        if (this.duration == 0) return "No duration specified";
        else if (this.duration == -1) return "Permanent";

        long days = TimeUnit.SECONDS.toDays(duration);
        long hours = TimeUnit.SECONDS.toHours(duration) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(duration) % 60;

        String time = "";
        if (days != 0) time += days + "d ";
        if (hours != 0) time += hours + "h ";
        if (minutes != 0) time += minutes + "min ";

        return time;
    }

    public ArrayList<PunishmentType> getPunishmentsTypes() {
        return punishmentsTypes;
    }

    public void setPunishmentsTypes(ArrayList<PunishmentType> punishmentsTypes) {
        this.punishmentsTypes = punishmentsTypes;
    }

    @BsonIgnore
    public long getDays() {
        return TimeUnit.SECONDS.toDays(duration);
    }

    @BsonIgnore
    public long getHours() {
        return TimeUnit.SECONDS.toHours(duration) % 24;
    }

    @BsonIgnore
    public long getMinutes() {
        return TimeUnit.SECONDS.toMinutes(duration) % 60;
    }

    @BsonIgnore
    public boolean isInRange(int score) {
        return (score >= this.start && score < this.end) || (this.end == -1 && score >= this.start);
    }

    @BsonIgnore
    public String getReasoningScopeAsString() {
        StringBuilder builder = new StringBuilder();

        for (Reasoning reasoning : this.getReasoningScope())
            builder.append(reasoning.name()).append(",");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }

    @BsonIgnore
    public String getPunishmentTypesAsString() {
        StringBuilder builder = new StringBuilder();

        for (PunishmentType reasoning : this.getPunishmentsTypes())
            builder.append(reasoning.name()).append(",");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }

    @BsonIgnore
    public void apply(Report report) throws DatabaseException {
        PlayerDataRepository repository = new PlayerDataRepository();

        PlayerData reported = repository.getPlayerData(report.getReportedUUID());
        PlayerData reporter = repository.getPlayerData(report.getReporterUUID());
        PlayerData reviewer = repository.getPlayerData(report.getReviewedBy());

        if (reported == null || reporter == null || reviewer == null) {
            throw new DatabaseException("Couldn't find all necessary player data from database.");
        }

        reporter.setReportScore(reporter.getReportScore() + 1);
        reported.setPunishmentScore(reported.getPunishmentScore() + this.getWeight());
        PunishmentManager manager = new PunishmentManager();
        Punishment punishment = manager.createPunishment(reported, reviewer, this.getPunishmentsTypes(), report.getReasoning(), Instant.now().getEpochSecond() + this.duration, "Preset used: " + this.presetName);

        manager.submitPunishment(reported, punishment, this.weight, report);
    }
}
