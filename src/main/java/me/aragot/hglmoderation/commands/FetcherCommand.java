package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.repository.PunishmentRepository;
import me.aragot.hglmoderation.repository.ReportRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.player.PlayerDataConverter;
import me.aragot.hglmoderation.service.punishment.PunishmentConverter;
import me.aragot.hglmoderation.service.report.ReportConverter;
import me.aragot.hglmoderation.service.player.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class FetcherCommand {

    public static BrigadierCommand createBrigadierCommand() {
        LiteralCommandNode<CommandSource> fetcherNode = BrigadierCommand.literalArgumentBuilder("fetcher")
                .requires(source -> source.hasPermission("hglmoderation.fetcher"))
                //execute when /fetcher
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if (source instanceof Player)
                        Responder.respond(source, "Invalid usage. Please try using <white>/fetcher <type> <id/player></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("punishment");
                            builder.suggest("report");
                            builder.suggest("player_data");

                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String type = context.getArgument("type", String.class);

                            Player player;
                            switch (type) {
                                case "player_data":
                                    player = context.getSource() instanceof Player ? ((Player) context.getSource()) : null;

                                    if (player == null) {
                                        Responder.respond(context.getSource(), "Sorry but you cannot use this command in that way with the console. Please try adding a username.", ResponseType.ERROR);
                                        break;
                                    }
                                    PlayerDataRepository repository = new PlayerDataRepository();
                                    PlayerData data = repository.getPlayerData(player);
                                    context.getSource().sendMessage(PlayerDataConverter.Companion.getComponentForPlayerData(data));
                                    break;
                                case "report":
                                    List<Report> openReports = ReportRepository.Companion.getOpenReports();
                                    context.getSource().sendMessage(ReportConverter.Companion.getComponentForReports(openReports));
                                    break;
                                case "punishment":
                                    player = context.getSource() instanceof Player ? ((Player) context.getSource()) : null;

                                    if (player == null) {
                                        Responder.respond(context.getSource(), "Sorry but you cannot use this command in that way with the console. Please try adding a username.", ResponseType.ERROR);
                                        break;
                                    }
                                    PunishmentRepository punishmentRepository = new PunishmentRepository();
                                    ArrayList<Punishment> punishments = punishmentRepository.getPunishmentsFor(player.getUniqueId().toString(), player.getRemoteAddress().getAddress().getHostAddress());

                                    if (punishments.isEmpty()) {
                                        Responder.respond(context.getSource(), "You currently don't have any punishments.", ResponseType.DEFAULT);
                                        break;
                                    }

                                    Punishment punishment = punishments.get(0);

                                    context.getSource().sendMessage(PunishmentConverter.Companion.getComponentForPunishment(punishment));
                                    break;
                                default:
                                    Responder.respond(context.getSource(),
                                            "Sorry but I couldn't find the type you were looking for. Please try using <white>/fetcher <type> <id/player></white>"
                                            , ResponseType.ERROR);
                                    break;
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("id", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("id");
                                    builder.suggest("username");
                                    builder.suggest("under_review");

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String id = context.getArgument("id", String.class);
                                    String type = context.getArgument("type", String.class);

                                    PlayerData data = null;
                                    String playerUuid = null;
                                    if (!id.equalsIgnoreCase("under_review")) {
                                        playerUuid = PlayerUtils.Companion.getUuidFromUsername(id);
                                    }

                                    if (playerUuid != null) {
                                        PlayerDataRepository playerDataRepository = new PlayerDataRepository();
                                        data = playerDataRepository.getPlayerData(playerUuid);
                                    }

                                    switch (type) {
                                        case "player_data":

                                            if (playerUuid == null) {
                                                Responder.respond(context.getSource(), "Sorry but you cannot use this command in that way with the console. Please try adding a username.", ResponseType.ERROR);
                                                break;
                                            }

                                            if (data == null) {
                                                Responder.respond(context.getSource(), "Sorry but this player never joined the server.", ResponseType.ERROR);
                                                break;
                                            }

                                            context.getSource().sendMessage(PlayerDataConverter.Companion.getComponentForPlayerData(data));
                                            break;
                                        case "report":
                                            if (id.equalsIgnoreCase("under_review")) {
                                                List<Report> reportsInProgress = ReportRepository.Companion.getReportsInProgress();

                                                context.getSource().sendMessage(ReportConverter.Companion.getComponentForReports(reportsInProgress));
                                                break;
                                            }

                                            if (playerUuid != null) {
                                                ReportRepository reportRepository = new ReportRepository();
                                                ArrayList<Report> reportsForPlayer = reportRepository.getReportsForPlayer(playerUuid);

                                                context.getSource().sendMessage(ReportConverter.Companion.getComponentForReports(reportsForPlayer));
                                                break;
                                            }
                                            ReportRepository reportRepository = new ReportRepository();
                                            Report report = reportRepository.getReportById(id);
                                            if (report == null) {
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find this report in the database.", ResponseType.ERROR);
                                                break;
                                            }

                                            context.getSource().sendMessage(ReportConverter.Companion.getMcReportOverview(report, false));
                                            break;
                                        case "punishment":
                                            Punishment punishment;
                                            PunishmentRepository punishmentRepository = new PunishmentRepository();
                                            if (playerUuid != null) { //if punishment != null then data cannot be null either
                                                if(data == null){
                                                    Responder.respond(context.getSource(), "This player didn't receive any punishments yet.", ResponseType.DEFAULT);
                                                    break;
                                                }

                                                ArrayList<Punishment> punishments = punishmentRepository.getPunishmentsFor(playerUuid, data.getLatestIp());
                                                if (punishments.isEmpty()) {
                                                    Responder.respond(context.getSource(), "This player didn't receive any punishments yet.", ResponseType.DEFAULT);
                                                    break;
                                                }

                                                punishment = punishments.get(0);

                                                context.getSource().sendMessage(PunishmentConverter.Companion.getComponentForPunishment(punishment));
                                                break;
                                            }
                                            punishment = punishmentRepository.getPunishmentById(id);

                                            if (punishment == null) {
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find this punishment in the database.", ResponseType.ERROR);
                                                break;
                                            }

                                            context.getSource().sendMessage(PunishmentConverter.Companion.getComponentForPunishment(punishment));
                                            break;
                                        default:
                                            Responder.respond(context.getSource(),
                                                    "Sorry but I couldn't find the type you were looking for. Please try using <white>/fetcher <type> <id/player></white>"
                                                    , ResponseType.ERROR);
                                            break;
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
        return new BrigadierCommand(fetcherNode);
    }
}
