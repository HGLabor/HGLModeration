package me.aragot.hglmoderation.discord;

import me.aragot.hglmoderation.admin.config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;

public class HGLBot {

    private static JDA instance;

    public static void init(Logger logger){
        if(Config.instance.getDiscordBotToken().isEmpty()){
            logger.info("No valid Discord Bot Token found, please edit the config.json file and run this command: /dcbot init");
            return;
        }
        JDABuilder builder = JDABuilder.createDefault(Config.instance.getDiscordBotToken());

        instance = builder.build();


        SubcommandData setChannel = new SubcommandData("set", "Set a log channel to receive updates");
        setChannel.addOption(OptionType.CHANNEL, "logChannel", "Log channel for reports and status updates", true);

        SubcommandData setPingRole = new SubcommandData("pingrole", "Sets a role to ping when receiving a new report.");
        setPingRole.addOption(OptionType.ROLE, "role", "Role to ping");

        SubcommandData reportList = new SubcommandData("list", "Returns a list with all open reports");

        SubcommandData reportStats = new SubcommandData("stats", "Returns all relevant statistics");

        SubcommandData reportGet = new SubcommandData("get", "Returns a report based on it's ID");
        reportGet.addOption(OptionType.STRING, "reportId", "ID of the Report", true);

        SubcommandData reportDecline = new SubcommandData("decline", "Declines a report based on the provided ID");
        reportDecline.addOption(OptionType.STRING, "reportId", "ID of the Report", true);

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
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
        ).queue();

        logger.info("Discord Bot has been initialized started!");
    }


}
