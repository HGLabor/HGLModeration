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
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.punishments.PunishmentType;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.punishment.PunishmentManager;
import me.aragot.hglmoderation.service.permissions.PermCompare;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

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

                                            if(!hasPermission(player, toPunish))
                                                return Command.SINGLE_SUCCESS;

                                            PlayerDataRepository playerDataRepository = new PlayerDataRepository();
                                            PlayerData victim = playerDataRepository.getPlayerData(toPunish);
                                            PlayerData punisher = playerDataRepository.getPlayerData(player);

                                            PunishmentManager manager = new PunishmentManager();
                                            Punishment punishment = manager.createPunishment(victim, punisher, preset.getPunishmentsTypes(), reasoning, Instant.now().getEpochSecond() + preset.getDuration(), "");
                                            boolean punished = manager.submitPunishment(victim, punishment, preset.getWeight(), null);

                                            if (punished) {
                                                Responder.respond(player, "<red>" + toPunish.getUsername() + "</red> was successfully punished. Punishment can be found under ID: " + punishment.getId(), ResponseType.SUCCESS);
                                            } else {
                                                Responder.respond(player, "Couldn't punish the player, database error...", ResponseType.ERROR);
                                            }

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
                                                    } else if(durationFormat.contains("m")) {
                                                        duration = 60;
                                                    } else if(durationFormat.equalsIgnoreCase("p")){
                                                        duration = -1L;
                                                    } else {
                                                        Responder.respond(player, "Sorry but I couldn't read the time format you have entered.", ResponseType.ERROR);
                                                        return Command.SINGLE_SUCCESS;
                                                    }


                                                    if(duration != -1L){
                                                        try {
                                                            durationFormat = durationFormat.replaceAll("[dhm]", "");
                                                            duration *= Long.parseLong(durationFormat);
                                                        } catch (NumberFormatException x) {
                                                            Responder.respond(player, "Sorry but I couldn't read the number passed for the duration.", ResponseType.ERROR);
                                                            return Command.SINGLE_SUCCESS;
                                                        }
                                                    }

                                                    if(!hasPermission(player, toPunish))
                                                        return Command.SINGLE_SUCCESS;

                                                    PlayerDataRepository playerDataRepository = new PlayerDataRepository();
                                                    PlayerData victim = playerDataRepository.getPlayerData(toPunish);
                                                    PlayerData punisher = playerDataRepository.getPlayerData(player);

                                                    PunishmentManager manager = new PunishmentManager();
                                                    Punishment punishment = manager.createPunishment(victim, punisher, new ArrayList<>(List.of(punishmentType)), reasoning, duration == -1 ? duration : Instant.now().getEpochSecond() + duration, "");
                                                    boolean punished = manager.submitPunishment(victim, punishment, 0, null);

                                                    if (punished) {
                                                        Responder.respond(player, "<red>" + toPunish.getUsername() + "</red> was successfully punished. Punishment can be found under ID: " + punishment.getId(), ResponseType.SUCCESS);
                                                    } else {
                                                        Responder.respond(player, "Couldn't punish the player, database error...", ResponseType.ERROR);
                                                    }

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
                                                            } else if(durationFormat.contains("m")) {
                                                                duration = 60;
                                                            } else if(durationFormat.equalsIgnoreCase("p")){
                                                                duration = -1L;
                                                            } else {
                                                                Responder.respond(player, "Sorry but I couldn't read the time format you have entered.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            if(duration != -1L){
                                                                try {
                                                                    durationFormat = durationFormat.replaceAll("[dhm]", "");
                                                                    duration *= Long.parseLong(durationFormat);
                                                                } catch (NumberFormatException x) {
                                                                    Responder.respond(player, "Sorry but I couldn't read the number passed for the duration.", ResponseType.ERROR);
                                                                    return Command.SINGLE_SUCCESS;
                                                                }
                                                            }

                                                            int weight;
                                                            try {
                                                                weight = Integer.parseInt(weightString);
                                                            } catch (NumberFormatException x) {
                                                                Responder.respond(player, "Sorry but I couldn't read the number passed for weight.", ResponseType.ERROR);
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            if(!hasPermission(player, toPunish))
                                                                return Command.SINGLE_SUCCESS;

                                                            PlayerDataRepository playerDataRepository = new PlayerDataRepository();
                                                            PlayerData victim = playerDataRepository.getPlayerData(toPunish);
                                                            PlayerData punisher = playerDataRepository.getPlayerData(player);

                                                            PunishmentManager manager = new PunishmentManager();
                                                            Punishment punishment = manager.createPunishment(victim, punisher, new ArrayList<>(List.of(punishmentType)), reasoning, duration == -1 ? duration : Instant.now().getEpochSecond() + duration, "");
                                                            boolean punished = manager.submitPunishment(victim, punishment, weight, null);

                                                            if (punished) {
                                                                Responder.respond(player, "<red>" + toPunish.getUsername() + "</red> was successfully punished. Punishment can be found under ID: " + punishment.getId(), ResponseType.SUCCESS);
                                                            } else {
                                                                Responder.respond(player, "Couldn't punish the player, database error...", ResponseType.ERROR);
                                                            }

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

    private static boolean hasPermission(Player base, Player toCompare){
        try {
            int permission = PermCompare.comparePermissionOf(base.getUniqueId(), toCompare.getUniqueId()).get();
            if(permission != PermCompare.GREATER_THAN){
                Responder.respond(base, "Sorry but you don't have enough permissions to review this report. The reported user has a higher role than you.", ResponseType.ERROR);
                HGLBot.logPunishmentWarning(base, toCompare);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }
}
