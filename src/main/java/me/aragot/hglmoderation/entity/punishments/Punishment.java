package me.aragot.hglmoderation.entity.punishments;

import me.aragot.hglmoderation.entity.Reasoning;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Punishment {
    private final String _id;
    private final long issuedAt; //Unix Timestamp
    private final String issuedTo;
    private final String issuedBy; //Minecraft Player UUID
    private final ArrayList<PunishmentType> types;
    private long endsAt; // Unix Timestamp; Value(-1) = Permanent Punishment;
    private final Reasoning reason;
    private final String note;

    public static ArrayList<Punishment> punishments;

    public Punishment(String _id, long issuedAt, String issuedTo, String issuedBy, ArrayList<PunishmentType> types, long endsAt, Reasoning reason, String note) {
        this._id = _id;
        this.issuedAt = issuedAt;
        this.issuedTo = issuedTo;
        this.issuedBy = issuedBy;
        this.types = types;
        this.endsAt = endsAt;
        this.reason = reason;
        this.note = note;
    }

    public String getIssuedTo() {
        return this.issuedTo;
    }

    public String getIssuerUUID() {
        return this.issuedBy;
    }

    public boolean isActive() {
        return this.endsAt < 0 || this.endsAt > Instant.now().getEpochSecond();
    }

    public String getRemainingTime() {
        if (!isActive()) return "It's over";
        if (this.endsAt == -1) return "Permanent";
        long differenceSeconds = endsAt - Instant.now().getEpochSecond();
        long days = TimeUnit.SECONDS.toDays(differenceSeconds);
        long hours = TimeUnit.SECONDS.toHours(differenceSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(differenceSeconds) % 60;
        long seconds = TimeUnit.SECONDS.toSeconds(differenceSeconds) % 60;

        String time = "";
        if (days != 0) time += days + "d ";
        if (hours != 0) time += hours + "h ";
        if (minutes != 0) time += minutes + "min ";
        if (seconds != 0) time += seconds + "sec ";

        return time;
    }

    public void setEndsAt(long endsAt) {
        this.endsAt = endsAt;
    }

    public String getDuration() {
        long differenceSeconds = endsAt - issuedAt;
        long days = TimeUnit.SECONDS.toDays(differenceSeconds);
        long hours = TimeUnit.SECONDS.toHours(differenceSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(differenceSeconds) % 60;

        String time = "";
        if(days != 0) time += days + "d ";
        if(hours != 0) time += hours + "h ";
        if(minutes != 0) time += minutes + "min ";

        return time;
    }

    public String getId() {
        return this._id;
    }

    public ArrayList<PunishmentType> getTypes() {
        return this.types;
    }

    public Reasoning getReasoning() {
        return this.reason;
    }

    public long getIssuedAtTimestamp() {
        return this.issuedAt;
    }

    public long getEndsAtTimestamp() {
        return this.endsAt;
    }

    public String getNote() {
        return this.note;
    }

    public String getTypesAsString() {
        StringBuilder builder = new StringBuilder();

        for(PunishmentType reasoning : this.getTypes())
            builder.append(reasoning.name()).append(",");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }
}
