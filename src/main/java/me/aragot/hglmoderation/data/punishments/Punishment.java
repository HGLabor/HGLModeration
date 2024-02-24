package me.aragot.hglmoderation.data.punishments;

import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Punishment {

    private String _id;
    private long issuedAt; //Unix Timestamp
    private String issuedTo;
    private String issuedBy; //Minecraft Player UUID
    private PunishmentType type;
    private long endsAt; // Unix Timestamp; Value(-1) = Permanent Punishment;
    private Reasoning reason;
    private String note;

    public static ArrayList<Punishment> punishments;

    public Punishment(String _id, long issuedAt, String issuedTo, String issuedBy, PunishmentType type, long endsAt, Reasoning reason, String note) {
        this._id = _id;
        this.issuedAt = issuedAt;
        this.issuedTo = issuedTo;
        this.issuedBy = issuedBy;
        this.type = type;
        this.endsAt = endsAt;
        this.reason = reason;
        this.note = note;
    }

    public static void submitPunishmentFromReport(Report report, PunishmentType type, long endsAt, String note){
        Punishment punishment = new Punishment(
                getNextPunishmentId(),
                Instant.now().getEpochSecond(),
                report.getReportedUUID(),
                report.getReviewedBy(),
                type,
                endsAt,
                report.getReasoning(),
                note
        );

        if(HGLBot.instance == null) return;

        if(!HGLModeration.instance.getDatabase().pushPunishment(punishment)){
            TextChannel channel = HGLBot.instance.getTextChannelById(Config.instance.getPunishmentChannelId());

            if(channel == null) return;

            channel.sendMessageEmbeds(
                    HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't push Punishment to Database (ID:" + punishment.getId() + ")").build()
            ).queue();

            return;
        }

        HGLBot.logPunishment(punishment);

    }

    public static Punishment getPunishmentById(String id){
        return HGLModeration.instance.getDatabase().getPunishmentById(id);
    }

    public static String getNextPunishmentId(){
        //table is hex number
        //Report id is random 8 digit hex number
        String [] table = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        boolean isUnique = false;
        String id = "";
        while(!isUnique){
            Random rand = new Random();

            for(int i = 0; i < 8; i++)
                id += table[rand.nextInt(16)];

            if(getPunishmentById(id) != null){
                id = "";
                continue;
            }

            isUnique = true;
        }

        return id;
    }

    public String getPunishedUUID(){
        return this.issuedTo;
    }

    public String getIssuerUUID(){
        return this.issuedBy;
    }

    public boolean isActive(){
        if(this.endsAt < 0) return true;
        return endsAt > Instant.now().getEpochSecond();
    }

    public String getRemainingTime(){
        if(!isActive()) return "It's over";
        long differenceSeconds = endsAt - Instant.now().getEpochSecond();
        long days = TimeUnit.SECONDS.toDays(differenceSeconds);
        long hours = TimeUnit.SECONDS.toHours(differenceSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(differenceSeconds) % 60;

        String time = "";
        if(days != 0) time += days + "D ";
        if(hours != 0) time += hours + "H ";
        if(minutes != 0) time += minutes + "M ";

        return time;
    }

    public String getDuration(){
        long differenceSeconds = endsAt - issuedAt;
        long days = TimeUnit.SECONDS.toDays(differenceSeconds);
        long hours = TimeUnit.SECONDS.toHours(differenceSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(differenceSeconds) % 60;

        String time = "";
        if(days != 0) time += days + "D ";
        if(hours != 0) time += hours + "H ";
        if(minutes != 0) time += minutes + "M ";

        return time;
    }

    public String getId(){
        return this._id;
    }

    public PunishmentType getType(){
        return this.type;
    }

    public Reasoning getReasoning(){
        return this.reason;
    }

    public long getIssuedAtTimestamp(){
        return this.issuedAt;
    }

    public long getEndsAtTimestamp(){
        return this.endsAt;
    }

    public String getNote(){
        return this.note;
    }

}
