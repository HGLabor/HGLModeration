package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.punishments.PunishmentType;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;

import java.util.NoSuchElementException;

public class PunishCommand {

    /*
        Usage: /punish player preset reason
                /punish player type reason duration
     */
    private static final String invalidUsage = "Invalid usage. Please try using <white>/punish <player> <presetName> <reason></white> or <white>/punish <player> <type> <reason> <duration> <weight></white>\nPlease keep in mind that <weight> is optional.";
    public static BrigadierCommand createBrigadierCommand(ProxyServer server){
        LiteralCommandNode<CommandSource> reviewNode = BrigadierCommand.literalArgumentBuilder("punish")
                .requires(source -> source.hasPermission("hglmoderation.punish"))
                //execute when /review
                .executes(context -> {

                    Responder.respond(context.getSource(),
                            invalidUsage,
                            ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            Player executedBy = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            if(executedBy == null) return builder.buildFuture();

                            try {
                                executedBy.getCurrentServer().orElseThrow().getServer().getPlayersConnected().forEach(player -> builder.suggest(player.getUsername()));
                            } catch (NoSuchElementException x) {
                                return builder.buildFuture();
                            }

                            return builder.buildFuture();
                        })
                        .executes(context -> {

                            Responder.respond(context.getSource(),
                                    invalidUsage,
                                    ResponseType.ERROR);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("type", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for(PunishmentType type : PunishmentType.values()){
                                        builder.suggest(type.name());
                                    }
                                    for(Preset preset : PresetHandler.instance.getPresetList()){
                                        builder.suggest(preset.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    //executes on /punish player presetName
                                    Responder.respond(context.getSource(),
                                            invalidUsage,
                                            ResponseType.ERROR);

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(BrigadierCommand.requiredArgumentBuilder("reason", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for(Reasoning reasoning : Reasoning.values()){
                                                builder.suggest(reasoning.name());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            //executes when /punish player type(must be punsihment type) reason
                                            String punishedPlayer = context.getArgument("player", String.class);
                                            String presetName = context.getArgument("type", String.class);
                                            String reason = context.getArgument("reason", String.class);

                                            Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                            Player toPunish;

                                            try {
                                                 toPunish = server.getPlayer(punishedPlayer).orElseThrow();
                                            } catch (NoSuchElementException x) {
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find the player you were looking for.", ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Preset preset = PresetHandler.instance.getPresetByName(presetName);

                                            if(preset == null){
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find the preset you were looking for.", ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Reasoning reasoning;
                                            try {
                                                reasoning = Reasoning.valueOf(reason);
                                            } catch (IllegalArgumentException x) {
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find a reason with that name.", ResponseType.ERROR);
                                            }

                                            //permission check here



                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(BrigadierCommand.requiredArgumentBuilder("duration", StringArgumentType.word())
                                                .executes(context -> {

                                                })
                                                .then(BrigadierCommand.requiredArgumentBuilder("weight", StringArgumentType.word())
                                                        .executes(context -> {

                                                        })
                                                )
                                        )
                                )
                        )
                )


                .build();
        return new BrigadierCommand(reviewNode);
    }
}
