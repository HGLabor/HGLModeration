package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.NoSuchElementException;

public class ReportCommand {

    public static BrigadierCommand createBrigadierCommand(ProxyServer server){
        LiteralCommandNode<CommandSource> reportNode = BrigadierCommand.literalArgumentBuilder("report")
                .requires(source -> source.hasPermission("hglmoderation.report"))
                //execute when /report
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond(source, "Invalid usage. Please try using <white>/report <player></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            Player reporter = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            if(reporter == null) return builder.buildFuture();

                            reporter.getCurrentServer().ifPresent((currentServer) -> {
                                currentServer.getServer().getPlayersConnected().forEach(player -> builder.suggest(player.getUsername()));
                            });
                           return builder.buildFuture();
                        })

                        .executes(context -> {
                            String reported = context.getArgument("player", String.class);
                            Player reporter = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            if(reporter == null) return BrigadierCommand.FORWARD;

                            try {
                                Player reportedPlayer = server.getPlayer(reported).orElseThrow();

                                if(reportedPlayer.getUniqueId().equals(reporter.getUniqueId())){
                                    Responder.respond(reporter, "Sorry, but you cannot report yourself!", ResponseType.ERROR);
                                    return Command.SINGLE_SUCCESS;
                                }
                            } catch(NoSuchElementException x) {
                                Responder.respond(reporter, "The player '" + reported + "' is currently not online.", ResponseType.ERROR);
                                return Command.SINGLE_SUCCESS;
                            }


                            reportSuggestion(reporter, reported);

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("reasoning", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                for(Reasoning reason : Reasoning.values())
                                    builder.suggest(StringUtils.capitalize(reason.toString().toLowerCase()));
                                return builder.buildFuture();
                            })

                            //executes when /report playerName reason
                            .executes(context -> {
                                String reported = context.getArgument("player", String.class);
                                Player reporter = context.getSource() instanceof Player ? (Player) context.getSource() : null;

                                if(reporter == null) return BrigadierCommand.FORWARD;

                                Player reportedPlayer;
                                try {
                                    reportedPlayer = server.getPlayer(reported).orElseThrow();
                                } catch(NoSuchElementException x){
                                    Responder.respond(reporter, "The player '" + reported + "' is currently not online.", ResponseType.ERROR);
                                    return Command.SINGLE_SUCCESS;
                                }

                                if(reportedPlayer.equals(reporter)){
                                    Responder.respond(reporter, "Sorry, but you cannot report yourself!", ResponseType.ERROR);
                                    return Command.SINGLE_SUCCESS;
                                }

                                String reasoning = context.getArgument("reasoning", String.class);

                                try {
                                    Reasoning reason = Reasoning.valueOf(reasoning.toUpperCase());
                                    Report.submitReport(reportedPlayer.getUniqueId().toString(), reporter.getUniqueId().toString(), reason, Report.getPriorityForReporter(reporter));
                                    Responder.respond(reporter, "Your report has been submitted. Our team will review your report as soon as possible. Thank you for your patience!", ResponseType.SUCCESS);
                                } catch (IllegalArgumentException x) {
                                    reportSuggestion(reporter, reported);
                                }
                                return Command.SINGLE_SUCCESS;
                            }))
                )

                .build();
        return new BrigadierCommand(reportNode);
    }

    private static void reportSuggestion(Player player, String reportedName){
        MiniMessage mm = MiniMessage.miniMessage();
        Component reportText = mm.deserialize(Responder.prefix + " You are about to report <b><red>" + reportedName + "</red></b>.<br>" + "Please pick a reasoning for the report:<br><br>");

        for(Reasoning reason : Reasoning.values()){
            String name = StringUtils.capitalize(reason.name().toLowerCase());
            reportText = reportText.append(
                    mm.deserialize("   <gray>☉</gray><red> " + name + "</red>")
                            .clickEvent(ClickEvent.runCommand("/report " + reportedName + " " + name))
                            .hoverEvent(HoverEvent.showText(mm.deserialize("<red>Report </red>" + reportedName + " <red>for " + name)))
            ).appendNewline();
        }

        player.sendMessage(reportText);
    }
}
