package me.aragot.hglmoderation.data.punishments;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.Notifier;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Punishment {

    private final String _id;
    private final long issuedAt; //Unix Timestamp
    private final String issuedTo;
    private final String issuedBy; //Minecraft Player UUID
    private ArrayList<PunishmentType> types;
    private final long endsAt; // Unix Timestamp; Value(-1) = Permanent Punishment;
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

    public static void submitPunishmentFromReport(PlayerData data, Report report, ArrayList<PunishmentType> types, long endsAt, String note){
        Punishment punishment = new Punishment(
                getNextPunishmentId(),
                Instant.now().getEpochSecond(),
                report.getReportedUUID(),
                report.getReviewedBy(),
                types,
                endsAt,
                report.getReasoning(),
                note
        );

        boolean isBotActive = HGLBot.instance != null;

        if(!HGLModeration.instance.getDatabase().pushPunishment(punishment) && isBotActive){
            TextChannel channel = HGLBot.instance.getTextChannelById(Config.instance.getPunishmentChannelId());

            if(channel == null) return;

            channel.sendMessageEmbeds(
                    HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't push Punishment to Database (ID:" + punishment.getId() + ")").build()
            ).queue();

            return;
        }

        report.setPunishmentId(punishment.getId());
        report.setState(ReportState.DONE);

        if(!HGLModeration.instance.getDatabase().updateReportsBasedOn(report) && isBotActive){
            TextChannel channel = HGLBot.instance.getTextChannelById(Config.instance.getPunishmentChannelId());

            if(channel == null) return;

            channel.sendMessageEmbeds(
                    HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't update Reports in Database for Punishment (ID:" + punishment.getId() + ")").build()
            ).queue();

            return;
        }

        data.addPunishment(punishment.getId());

        punishment.enforce();

        HGLBot.logPunishment(punishment);
    }

    public static void submitPunishment(Player toPunish, Player punisher, ArrayList<PunishmentType> types, Reasoning reason, long endsAt){
        Punishment punishment = new Punishment(
                getNextPunishmentId(),
                Instant.now().getEpochSecond(),
                toPunish.getUniqueId().toString(),
                punisher.getUniqueId().toString(),
                types,
                endsAt,
                reason,
                ""
        );


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
        long seconds = TimeUnit.SECONDS.toSeconds(differenceSeconds) % 60;

        String time = "";
        if(days != 0) time += days + "d ";
        if(hours != 0) time += hours + "h ";
        if(minutes != 0) time += minutes + "min ";
        if(seconds != 0) time += seconds + "sec ";

        return time;
    }

    public String getDuration(){
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

    public String getId(){
        return this._id;
    }

    public ArrayList<PunishmentType> getTypes(){
        return this.types;
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

    public String getTypesAsString(){
        StringBuilder builder = new StringBuilder();

        for(PunishmentType reasoning : this.getTypes())
            builder.append(reasoning.name()).append(",");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }

    public void enforce(){
        ProxyServer server = HGLModeration.instance.getServer();
        Player player = server.getPlayer(UUID.fromString(this.getPunishedUUID())).get();
        if(!player.isActive()) return;

        if(this.getTypes().contains(PunishmentType.MUTE)){
            PlayerListener.playerMutes.put(this.getPunishedUUID(), this);
            player.sendMessage(getMuteComponent());
        }

        if(this.getTypes().contains(PunishmentType.BAN)){

            player.disconnect(getBanDisplay());
        }


    }

    public void enforce(Player player){
        if(this.getTypes().contains(PunishmentType.MUTE)){
            PlayerListener.playerMutes.put(this.getPunishedUUID(), this);
        }

        if(this.getTypes().contains(PunishmentType.BAN)){
            if(!player.isActive()) return;

            player.disconnect(getBanDisplay());
        }
    }
    public void enforce(LoginEvent event){
        event.setResult(ResultedEvent.ComponentResult.denied(getBanDisplay()));
    }

    public Component getBanDisplay(){
        String banReason = "<blue>HGLabor</blue>\n" +
                "<red><b>You were banned from our network.</b></red>\n\n" +
                "<gray>Punishment ID:</gray> <red>" + this.getId() + "</red>\n" +
                "<gray>Reason:</gray> <red>" + Reasoning.getPrettyReasoning(this.getReasoning()) + "</red>\n" +
                "<gray>Duration:</gray> <red>" + this.getRemainingTime() + "</red>\n\n" +
                "<red><b>DO NOT SHARE YOUR PUNISHMENT ID TO OTHERS!!!</b></red>\n" +
                "<gray>You can appeal for your ban here: <blue><underlined>" + Config.discordLink + "</underlined></blue></gray>";

        return MiniMessage.miniMessage().deserialize(banReason);
    }

    public Component getMuteComponent(){
        String muteComponent = "<gold>=================================================</gold>\n\n" +
                "        <red>You were muted for misbehaving.\n" +
                "        <b>DO NOT SHARE YOUR PUNISHMENT ID!!!</b></red>\n\n" +
                "        <gray>Punishment ID:</gray> <red>" + this.getId() + "</red>\n" +
                "        <gray>Reason:</gray> <red>" + Reasoning.getPrettyReasoning(this.getReasoning()) + "</red>\n" +
                "        <gray>Duration:</gray> <red>" + this.getRemainingTime() + "</red>\n\n" +
                "<gold>=================================================</gold>";
        return MiniMessage.miniMessage().deserialize(muteComponent);
    }
}
