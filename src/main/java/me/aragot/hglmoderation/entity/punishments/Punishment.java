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

    public void setEndsAt(long endsAt) {
        this.endsAt = endsAt;
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
}
