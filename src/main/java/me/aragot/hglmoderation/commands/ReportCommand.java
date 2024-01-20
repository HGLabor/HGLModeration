package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.data.reports.Reasoning;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReportCommand {

    public static BrigadierCommand createBrigadierCommand(ProxyServer server){
        LiteralCommandNode<CommandSource> reportNode = BrigadierCommand.literalArgumentBuilder("report")
                .requires(source -> source.hasPermission("hglmoderation.report"))
                //execute when /report
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond((Player) source, "Invalid usage. Please try using <white>/report <player></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            server.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername()
                            ));

                            return builder.buildFuture();
                        })
                        //execute when /report <arg>
                        .executes(context -> {
                            String reported = context.getArgument("player", String.class);
                            Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            if(player == null) return BrigadierCommand.FORWARD;

                            if(!isValidPlayer(server, player, reported))
                                return Command.SINGLE_SUCCESS;

                            //Open report book here

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
                                Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                if(player == null) return BrigadierCommand.FORWARD;

                                if(!isValidPlayer(server, player, reported))
                                    return Command.SINGLE_SUCCESS;

                                String reasoning = context.getArgument("reasoning", String.class);

                                try {
                                    Reasoning reason = Reasoning.valueOf(reasoning);

                                } catch (IllegalArgumentException x) {
                                    //Open report book here as well
                                }

                                return Command.SINGLE_SUCCESS;
                            }))
                )

                //Second Argument for reasoning missing

                .build();
        return new BrigadierCommand(reportNode);
    }

    private static boolean isValidPlayer(ProxyServer server, Player sender, String reported){
        if(server.getPlayer(reported).isPresent()) return true;

        Responder.respond(sender, "The player '" + reported + "' is currently not online.", ResponseType.ERROR);

        return false;
    }
}
