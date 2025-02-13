package me.aragot.hglmoderation.discord;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.punishments.PunishmentType;
import me.aragot.hglmoderation.entity.reports.Priority;
import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.discord.actions.ActionHandler;
import me.aragot.hglmoderation.discord.commands.CommandParser;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.player.PlayerUtils;
import me.aragot.hglmoderation.service.punishment.PunishmentConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class HGLBot {

    public static JDA instance;
    public static ProxyServer server;
    public static final String authorId = "974206098364071977";
    private static User author;

    public static void init(ProxyServer server, Logger logger) {
        if (Config.instance.getDiscordBotToken().isEmpty()) {
            logger.info("No valid Discord Bot Token found, please edit the config.json file and run this command: /dcbot init");
            return;
        }
        HGLBot.server = server;
        JDABuilder builder = JDABuilder.createDefault(Config.instance.getDiscordBotToken());

        builder.addEventListeners(new CommandParser());
        builder.addEventListeners(new ActionHandler());

        instance = builder.build();


        SubcommandData setChannel = new SubcommandData("set", "Set a log channel to receive updates");
        setChannel.addOption(OptionType.STRING, "type", "Log channel type, use 'report' or 'punishment'", true);
        setChannel.addOption(OptionType.CHANNEL, "logchannel", "Log channel for reports and status updates", true);

        SubcommandData setPingRole = new SubcommandData("pingrole", "Sets a role to ping when receiving a new report.");
        setPingRole.addOption(OptionType.ROLE, "role", "Role to ping");

        SubcommandData reportList = new SubcommandData("list", "Returns a list with all open reports");

        SubcommandData reportStats = new SubcommandData("stats", "Returns all relevant statistics");

        SubcommandData reportGet = new SubcommandData("get", "Returns a report based on it's ID");
        reportGet.addOption(OptionType.STRING, "reportid", "ID of the Report", true);

        SubcommandData reportDecline = new SubcommandData("decline", "Declines a report based on the provided ID");
        reportDecline.addOption(OptionType.STRING, "reportid", "ID of the Report", true);

        instance.updateCommands().addCommands(
                Commands.slash("logs", "Modify the logs")
                        .addSubcommands(
                                setChannel,
                                setPingRole
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),
                Commands.slash("report", "Manage reports to your will")
                        .addSubcommands(
                                reportList,
                                reportStats,
                                reportGet,
                                reportDecline
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)),
                Commands.slash("link", "Link your Minecraft profile with your discord account")
                        .addSubcommands(
                                new SubcommandData("reset", "Resets your current minecraft Link"),
                                new SubcommandData("generate", "Generates a new key to Link your account with")
                        ),
                Commands.slash("preset", "Displays the PresetGUI to modify punishment presets.")
        ).queue();
        author = instance.retrieveUserById(authorId).complete();
        logger.info("Discord Bot has been initialized started!");
    }

    public static EmbedBuilder getEmbedTemplate(ResponseType type) {
        EmbedBuilder eb = new EmbedBuilder();
        switch (type) {
            case ERROR:
                eb.setTitle("Error!");
                eb.setColor(Color.RED);
                break;
            case SUCCESS:
                eb.setTitle("Success!");
                eb.setColor(Color.GREEN);
            case DEFAULT:
                eb.setTitle("Hey, listen!");
                eb.setColor(Color.BLUE);
        }

        eb.setFooter("Found a bug? Please contact my author: @" + author.getName(), author.getAvatarUrl());
        return eb;
    }

    public static EmbedBuilder getEmbedTemplate(ResponseType type, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(description);
        switch (type) {
            case ERROR:
                eb.setTitle("Error!");
                eb.setColor(Color.RED);
                break;
            case SUCCESS:
                eb.setTitle("Success!");
                eb.setColor(Color.GREEN);
                break;
            case DEFAULT:
                eb.setTitle("Hey, listen!");
                eb.setColor(Color.BLUE);
                break;
        }

        eb.setFooter("Found a bug? Please contact my author: @" + author.getName(), author.getAvatarUrl());
        return eb;
    }

    public static void logReport(Report report) {
        if (Config.instance.getReportChannelId().isEmpty()) return;
        TextChannel logChannel = instance.getTextChannelById(Config.instance.getReportChannelId());
        if (logChannel == null) return;

        if (Reasoning.getChatReasons().contains(report.getReasoning())) {
            logChannel.sendMessageEmbeds(getReportEmbed(report, true).build(), getReportMessagesEmbed(report).build()).queue(message -> report.setDiscordLog(message.getId()));
        } else {
            logChannel.sendMessageEmbeds(getReportEmbed(report, true).build()).queue(message -> report.setDiscordLog(message.getId()));
        }
    }

    public static EmbedBuilder getReportEmbed(Report report, boolean incoming) {
        String title = incoming ? "Incoming Report: " + report.getId() : "Report: " + report.getId();
        Color color = Color.green;

        if (report.getPriority() == Priority.MEDIUM) color = Color.yellow;
        else if (report.getPriority() == Priority.HIGH) color = Color.red;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setColor(color);
        eb.setFooter("Found a bug? Please contact my author: @" + author.getName(), author.getAvatarUrl());

        String reportedName = HGLModeration.instance.getPlayerNameEfficiently(report.getReportedUUID());
        String reporterName = HGLModeration.instance.getPlayerNameEfficiently(report.getReporterUUID());

        String description = "Reported Name: " + reportedName + "\n" +
                "Reported by: " + reporterName + "\n" +
                "Reasoning: " + report.getReasoning().name() + "\n" +
                "Report ID: " + report.getId() + "\n" +
                "Priority: " + report.getPriority() + "\n" +
                "Submitted at: <t:" + report.getSubmittedAt() + ":f>\n" +
                "State: " + report.getState().name();
        eb.setThumbnail("https://mc-heads.net/avatar/" + report.getReportedUUID());
        eb.setDescription(description);

        return eb;
    }

    public static EmbedBuilder getReportMessagesEmbed(Report report) {
        String title = "Message Log";

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setColor(Color.blue);
        eb.setFooter("Found a bug? Please contact my author: @" + author.getName(), author.getAvatarUrl());

        StringBuilder description = new StringBuilder("```");
        String username = HGLModeration.instance.getPlayerNameEfficiently(report.getReportedUUID());

        if (report.getReportedUserMessages().isEmpty()) {
            eb.setDescription("No messages sent");
            return eb;
        }

        for (String message : report.getReportedUserMessages()) {
            description.append(username).append(": ").append(message).append("\n");
        }

        description.append("```");

        eb.setDescription(description.toString());
        return eb;
    }

    public static ArrayList<MessageEmbed> getPunishmentEmbeds(Punishment punishment) {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Incoming " + PunishmentConverter.Companion.getTypesAsString(punishment));
        eb.setColor(Color.red);
        eb.setFooter("Found a bug? Please contact my author: @" + author.getName(), author.getAvatarUrl());
        eb.setThumbnail(punishment.getTypes().contains(PunishmentType.IP_BAN) ? "https://as1.ftcdn.net/v2/jpg/00/54/65/16/1000_F_54651607_OJOGbrFBB3mDTpZDKmdjjR94lsbZMTVa.jpg" : "https://mc-heads.net/avatar/" + punishment.getIssuedTo());

        UUID playerUuid = null;
        try {
            playerUuid = UUID.fromString(punishment.getIssuedTo());
        } catch (IllegalArgumentException ignored) {
        }

        String punishedName = punishment.getTypes().contains(PunishmentType.IP_BAN) || playerUuid == null ? punishment.getIssuedTo() : PlayerUtils.Companion.getUsernameFromUUID(playerUuid);
        String punisherName = PlayerUtils.Companion.getUsernameFromUUID(punishment.getIssuedBy());

        if (punishedName == null || punisherName == null)
            return embeds;

        String punishmentInfo = "Punished Player: " + punishedName + "\n" +
                "Punished by: " + punisherName + "\n" +
                "Reasoning: " + punishment.getReason().name() + "\n" +
                "Punishment ID: " + punishment.getId() + "\n" +
                "Duration: " + PunishmentConverter.Companion.getDuration(punishment) + "\n" +
                "Submitted at: <t:" + punishment.getIssuedAt() + ":f>\n" +
                "Ends at: <t:" + punishment.getEndsAt() + ":f>";

        eb.setDescription(punishmentInfo);
        embeds.add(eb.build());

        if (punishment.getNote().isEmpty())
            return embeds;

        eb.setTitle("Reviewers Note");
        eb.setColor(Color.BLUE);
        eb.setThumbnail("https://mc-heads.net/avatar/" + punishment.getIssuedTo());
        eb.setDescription("```" + punishment.getNote() + "```");

        embeds.add(eb.build());
        return embeds;
    }

    public static void logPunishment(Punishment punishment) {
        if (Config.instance.getReportChannelId().isEmpty()) return;
        TextChannel punishmentChannel = instance.getTextChannelById(Config.instance.getPunishmentChannelId());
        if (punishmentChannel == null) return;

        punishmentChannel.sendMessageEmbeds(getPunishmentEmbeds(punishment)).queue();
    }

    public static void logPunishmentPushFailure(Punishment punishment) {
        TextChannel channel = instance.getTextChannelById(Config.instance.getPunishmentChannelId());

        if (channel == null) return;

        channel.sendMessageEmbeds(
                HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't push Punishment to Database (ID:" + punishment.getId() + ")").build()
        ).queue();
    }

    public static void logPunishmentWarning(Player player, UUID target) {
        TextChannel channel = instance.getTextChannelById(Config.instance.getPunishmentChannelId());
        if (channel == null) return;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Someone tried to ban a greater Role");
        eb.setColor(Color.RED);
        eb.setThumbnail("https://mc-heads.net/avatar/" + player.getUniqueId().toString());

        LuckPerms luckPerms = LuckPermsProvider.get();
        String executorGroup = "Couldn't fetch";
        String targetGroup = "Couldn't fetch";
        try {
            executorGroup = luckPerms.getUserManager().loadUser(player.getUniqueId()).get().getPrimaryGroup();
            targetGroup = luckPerms.getUserManager().loadUser(target).get().getPrimaryGroup();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        String desc = "Watch out! A user tried to ban another user with a greater role.\n" +
                "\n" +
                "Executor: " + player.getUsername() + "\n" +
                "Executor's Primary Role: " + executorGroup + "\n" +
                "Target: " + PlayerUtils.Companion.getUsernameFromUUID(target) + "\n" +
                "Target's Primary Role: " + targetGroup + "\n" +
                "Attempted at: <t:" + Instant.now().getEpochSecond() + ":f>";
        eb.setDescription(desc);

        channel.sendMessageEmbeds(eb.build()).queue();
    }
}
