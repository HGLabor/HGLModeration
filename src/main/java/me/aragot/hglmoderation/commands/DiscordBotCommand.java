package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

public class DiscordBotCommand {

    public static BrigadierCommand createBrigadierCommand(ProxyServer server, Logger logger){
        LiteralCommandNode<CommandSource> reportNode = BrigadierCommand.literalArgumentBuilder("dcbot")
                .requires(source -> source.hasPermission("hglmoderation.dcbot"))
                //execute when /report
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond((Player) source, "Invalid usage. Please try using <white>/dcbot <action></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("init");

                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String action = context.getArgument("action", String.class);

                            switch(action){
                                case "init":
                                    Config.loadConfig();
                                    HGLBot.init(server, logger);
                                    break;
                                default:
                                    context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(Responder.prefix + "<red>Please only use the provided actions.</red>"));
                                    break;
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
        return new BrigadierCommand(reportNode);
    }
}
