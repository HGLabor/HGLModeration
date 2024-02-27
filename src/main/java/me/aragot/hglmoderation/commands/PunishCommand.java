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
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.punishments.PunishmentType;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class PunishCommand {

    /*
        Usage: /punish player preset reason
                /punish player type reason duration
     */
    private static final String invalidUsage = "Invalid usage. Please try using <white>/punish <player> <presetName> <reason></white> or <white>/punish <player> <type> <reason> <duration> <weight></white>";
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
                                            //executes when /punish player type(must be preset) reason
                                            String punishedPlayer = context.getArgument("player", String.class);
                                            String presetName = context.getArgument("type", String.class);
                                            String reason = context.getArgument("reason", String.class);

                                            Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                            Player toPunish;

                                            if(player == null) return Command.SINGLE_SUCCESS;

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
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            //permission check here


                                            //SubmitPunishment automatically responds to the Player.
                                            Punishment.submitPunishment(toPunish,
                                                    player,
                                                    preset.getPunishmentsTypes(),
                                                    reasoning,
                                                    Instant.now().getEpochSecond() + preset.getDuration(),
                                                    0);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(BrigadierCommand.requiredArgumentBuilder("duration", StringArgumentType.word())
                                                .executes(context -> {
                                                    //executes when /punish player type(must be PunishmentType) reason duration
                                                    String punishedPlayer = context.getArgument("player", String.class);
                                                    String punishmentName = context.getArgument("type", String.class);
                                                    String reason = context.getArgument("reason", String.class);
                                                    String durationFormat = context.getArgument("duration", String.class);

                                                    Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                                    Player toPunish;

                                                    if(player == null) return Command.SINGLE_SUCCESS;

                                                    try {
                                                        toPunish = server.getPlayer(punishedPlayer).orElseThrow();
                                                    } catch (NoSuchElementException x) {
                                                        Responder.respond(context.getSource(), "Sorry but I couldn't find the player you were looking for.", ResponseType.ERROR);
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    PunishmentType punishmentType;

                                                    try {
                                                        punishmentType = PunishmentType.valueOf(punishmentName);
                                                    } catch (IllegalArgumentException x) {
                                                        Responder.respond(context.getSource(), "Sorry but I couldn't find the preset you were looking for.", ResponseType.ERROR);
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    Reasoning reasoning;
                                                    try {
                                                        reasoning = Reasoning.valueOf(reason);
                                                    } catch (IllegalArgumentException x) {
                                                        Responder.respond(context.getSource(), "Sorry but I couldn't find a reason with that name.", ResponseType.ERROR);
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    long duration;

                                                    //only set duration multiplier
                                                    if(durationFormat.contains("d")){
                                                        duration = 24 * 60 * 60;
                                                    } else if(durationFormat.contains("h")){
                                                        duration = 60 * 60;
                                                    } else if(durationFormat.contains("m")){
                                                        duration = 60;
                                                    } else {
                                                        Responder.respond(player, "Sorry but I couldn't read the time format you have entered.", ResponseType.ERROR);
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    try {
                                                        durationFormat = durationFormat.replaceAll("[dhm]", "");
                                                        duration *= Long.parseLong(durationFormat);
                                                    } catch (NumberFormatException x) {
                                                        Responder.respond(player, "Sorry but I couldn't read the number passed for the duration.", ResponseType.ERROR);
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    //permission check here

                                                    Punishment.submitPunishment(toPunish,
                                                            player,
                                                            List.of(punishmentType),
                                                            reasoning,
                                                            Instant.now().getEpochSecond() + duration,
                                                            0);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(BrigadierCommand.requiredArgumentBuilder("weight", StringArgumentType.word())
                                                        .executes(context -> {
                                                            //executes when /punish player type(must be PunishmentType) reason duration weight
                                                            String punishedPlayer = context.getArgument("player", String.class);
                                                            String punishmentName = context.getArgument("type", String.class);
                                                            String reason = context.getArgument("reason", String.class);
                                                            String durationFormat = context.getArgument("duration", String.class);
                                                            String weightString = context.getArgument("weight", String.class);

                                                            Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                                            Player toPunish;

                                                            if(player == null) return Command.SINGLE_SUCCESS;

                                                            try {
                                                                toPunish = server.getPlayer(punishedPlayer).orElseThrow();
                                                            } catch (NoSuchElementException x) {
                                                                Responder.respond(context.getSource(), "Sorry but I couldn't find the player you were looking for.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            PunishmentType punishmentType;

                                                            try {
                                                                punishmentType = PunishmentType.valueOf(punishmentName);
                                                            } catch (IllegalArgumentException x) {
                                                                Responder.respond(context.getSource(), "Sorry but I couldn't find the preset you were looking for.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            Reasoning reasoning;
                                                            try {
                                                                reasoning = Reasoning.valueOf(reason);
                                                            } catch (IllegalArgumentException x) {
                                                                Responder.respond(context.getSource(), "Sorry but I couldn't find a reason with that name.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            long duration;

                                                            //only set duration multiplier
                                                            if(durationFormat.contains("d")){
                                                                duration = 24 * 60 * 60;
                                                            } else if(durationFormat.contains("h")){
                                                                duration = 60 * 60;
                                                            } else if(durationFormat.contains("m")){
                                                                duration = 60;
                                                            } else {
                                                                Responder.respond(player, "Sorry but I couldn't read the time format you have entered.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            try {
                                                                durationFormat = durationFormat.replaceAll("[dhm]", "");
                                                                duration *= Long.parseLong(durationFormat);
                                                            } catch (NumberFormatException x) {
                                                                Responder.respond(player, "Sorry but I couldn't read the number passed for the duration.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            int weight;
                                                            try {
                                                                weight = Integer.parseInt(weightString);
                                                            } catch (NumberFormatException x) {
                                                                Responder.respond(player, "Sorry but I couldn't read the number passed for weight.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            //permission check here

                                                            Punishment.submitPunishment(toPunish,
                                                                    player,
                                                                    List.of(punishmentType),
                                                                    reasoning,
                                                                    Instant.now().getEpochSecond() + duration,
                                                                    weight);

                                                            return Command.SINGLE_SUCCESS;
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
