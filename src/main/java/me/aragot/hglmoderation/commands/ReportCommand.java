package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
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
                            String argumentProvided = context.getArgument("player", String.class);

                            server.getPlayer(argumentProvided).ifPresent(player ->
                                    player.sendMessage(Component.text("Hello!"))
                            );
                            return Command.SINGLE_SUCCESS;
                        })

                )

                //Second Argument for reasoning missing

                .build();
        return new BrigadierCommand(reportNode);
    }
}
