package me.aragot.hglmoderation.entity.punishments;

import me.aragot.hglmoderation.entity.Reasoning;

import java.time.Instant;
import java.util.*;

public class Punishment {

    private String _id;
    private long issuedAt; //Unix Timestamp
    private String issuedTo;
    private String issuedBy; //Minecraft Player UUID
    private ArrayList<PunishmentType> types;
    private long endsAt; // Unix Timestamp; Value(-1) = Permanent Punishment;
    private Reasoning reason;
    private String note;

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

    // Used for Codec
    public Punishment() {}

    public String getIssuedTo() {
        return this.issuedTo;
    }

    public String getIssuedBy() {
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

    public Reasoning getReason() {
        return this.reason;
    }

    public long getIssuedAt() {
        return this.issuedAt;
    }

    public long getEndsAt() {
        return this.endsAt;
    }

    public String getNote() {
        return this.note;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setIssuedTo(String issuedTo) {
        this.issuedTo = issuedTo;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public void setTypes(ArrayList<PunishmentType> types) {
        this.types = types;
    }

    public void setReason(Reasoning reason) {
        this.reason = reason;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static void setPunishments(ArrayList<Punishment> punishments) {
        Punishment.punishments = punishments;
    }
}
