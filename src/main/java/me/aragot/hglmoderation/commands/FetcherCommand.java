package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.entity.Notification;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.repository.PunishmentRepository;
import me.aragot.hglmoderation.repository.ReportRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.report.ReportConverter;
import me.aragot.hglmoderation.service.player.PlayerUtils;
import me.aragot.hglmoderation.service.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FetcherCommand {

    public static BrigadierCommand createBrigadierCommand(){
        LiteralCommandNode<CommandSource> fetcherNode = BrigadierCommand.literalArgumentBuilder("dcbot")
                .requires(source -> source.hasPermission("hglmoderation.fetcher"))
                //execute when /fetcher
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
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
                            switch(type){
                                case "player_data":
                                    player = context.getSource() instanceof Player ? ((Player) context.getSource()) : null;

                                    if(player == null){
                                        Responder.respond(context.getSource(), "Sorry but you cannot use this command in that way with the console. Please try adding a username.", ResponseType.ERROR);
                                        break;
                                    }
                                    PlayerDataRepository repository = new PlayerDataRepository();
                                    PlayerData data = repository.getPlayerData(player);
                                    context.getSource().sendMessage(getComponentForPlayerData(data));
                                    break;
                                case "report":
                                    List<Report> openReports = ReportRepository.Companion.getOpenReports();
                                    context.getSource().sendMessage(getComponentForReports(openReports));
                                    break;
                                case "punishment":
                                    player = context.getSource() instanceof Player ? ((Player) context.getSource()) : null;

                                    if(player == null){
                                        Responder.respond(context.getSource(), "Sorry but you cannot use this command in that way with the console. Please try adding a username.", ResponseType.ERROR);
                                        break;
                                    }
                                    PunishmentRepository punishmentRepository = new PunishmentRepository();
                                    Punishment punishment = punishmentRepository.getPunishmentsFor(player.getUniqueId().toString(), player.getRemoteAddress().getAddress().getHostAddress()).get(0);

                                    if(punishment == null){
                                        Responder.respond(context.getSource(), "You currently don't have any active reports.", ResponseType.DEFAULT);
                                        break;
                                    }

                                    context.getSource().sendMessage(getComponentForPunishment(punishment));
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
                                        playerUuid =  PlayerUtils.Companion.getUuidFromUsername(id);
                                    }

                                    if (playerUuid != null) {
                                        PlayerDataRepository playerDataRepository = new PlayerDataRepository();
                                        data = playerDataRepository.getPlayerData(playerUuid);
                                    }

                                    switch(type){
                                        case "player_data":

                                            if(playerUuid == null){
                                                Responder.respond(context.getSource(), "Sorry but you cannot use this command in that way with the console. Please try adding a username.", ResponseType.ERROR);
                                                break;
                                            }

                                            if(data == null){
                                                Responder.respond(context.getSource(), "Sorry but this player never joined the server.", ResponseType.ERROR);
                                                break;
                                            }

                                            context.getSource().sendMessage(getComponentForPlayerData(data));
                                            break;
                                        case "report":
                                            if(id.equalsIgnoreCase("under_review")){
                                                List<Report> reportsInProgress = ReportRepository.Companion.getReportsInProgress();

                                                context.getSource().sendMessage(getComponentForReports(reportsInProgress));
                                                break;
                                            }

                                            if(playerUuid != null){
                                                ReportRepository reportRepository = new ReportRepository();
                                                ArrayList<Report> reportsForPlayer = reportRepository.getReportsForPlayer(playerUuid);

                                                context.getSource().sendMessage(getComponentForReports(reportsForPlayer));
                                                break;
                                            }
                                            ReportRepository reportRepository = new ReportRepository();
                                            Report report = reportRepository.getReportById(id);
                                            if(report == null){
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find this report in the database", ResponseType.ERROR);
                                                break;
                                            }

                                            context.getSource().sendMessage(ReportConverter.Companion.getMcReportComponent(report, false));
                                            break;
                                        case "punishment":
                                            Punishment punishment;
                                            PunishmentRepository punishmentRepository = new PunishmentRepository();
                                            if(playerUuid != null){ //if punishment != null then data cannot be null either
                                                punishment = punishmentRepository.getPunishmentsFor(playerUuid, data.getLatestIp()).get(0);
                                                context.getSource().sendMessage(getComponentForPunishment(punishment));
                                                break;
                                            }
                                            punishment = punishmentRepository.getPunishmentById(id);

                                            if(punishment == null){
                                                Responder.respond(context.getSource(), "You currently don't have any active reports.", ResponseType.DEFAULT);
                                                break;
                                            }

                                            context.getSource().sendMessage(getComponentForPunishment(punishment));
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

    //Move Following Code into Converters?
    public static Component getComponentForReports(List<Report> reportList){
        StringBuilder reports = new StringBuilder(Responder.prefix + " <gold>Current Reports:</gold>");
        if(reportList.isEmpty()) return MiniMessage.miniMessage().deserialize(reports.append("<white> None</white>").toString());

        HashMap<String, String> userNameCache = new HashMap<>();
        int displayMax = 10;
        int count = 0;

        for(Report report : reportList){
            if(count == displayMax) break;
            if(userNameCache.get(report.getReportedUUID()) == null){
                userNameCache.put(report.getReportedUUID(), PlayerUtils.Companion.getUsernameFromUUID(report.getReportedUUID()));
            }
            String fetchReport = "<click:run_command:'/fetcher report " + report.getId() + "'><white>[<yellow><b>Check out</b></yellow>]</white></click>";

            reports.append("\n<gray>")
                    .append(report.getId())
                    .append(" ➡ </gray><red>")
                    .append(report.getPriority().name())
                    .append("</red><gray> ➡ </gray><red>")
                    .append(report.getReasoning().name())
                    .append("</red><gray> ➡ </gray><red>")
                    .append(userNameCache.get(report.getReportedUUID()))
                    .append("</red>").append("\n").append(ReportConverter.Companion.getViewDetailsRaw(report))
                    .append("   ").append(fetchReport);

            count++;
        }

        return MiniMessage.miniMessage().deserialize(reports.toString());
    }

    public static Component getComponentForPunishment(Punishment punishment){

        if(punishment == null)
            return MiniMessage.miniMessage().deserialize(Responder.prefix + " <red>This player was never punished before</red>");

        String raw = Responder.prefix + " <white>Showing Details for ID:</white> <red>" + punishment.getId() + "</red>\n" +
                "<white>Punished Player:</white> <red>" + (punishment.getIssuedTo().contains(".") ? punishment.getIssuedTo() : PlayerUtils.Companion.getUsernameFromUUID(punishment.getIssuedTo())) + "</red>\n" +
                "<white>Issued By:</white> <red>" + PlayerUtils.Companion.getUsernameFromUUID(punishment.getIssuerUUID()) + "</red>\n" +
                "<white>Duration:</white> <red>" + punishment.getDuration() + "</red>\n" +
                "<white>Types:</white> <red>" + punishment.getTypesAsString() + "</red>\n" +
                "<white>Reasoning:</white> <red>" + Reasoning.getPrettyReasoning(punishment.getReasoning()) + "</red>\n" +
                "<white>Note:</white> <br><red>" + punishment.getNote() + "</red>";

        return MiniMessage.miniMessage().deserialize(raw);
    }

    public static Component getComponentForPlayerData(PlayerData data){

        String raw = Responder.prefix + " <white>Data for Player</white> <red>" + PlayerUtils.Companion.getUsernameFromUUID(data.getPlayerId()) + "</red>\n" +
                "<white>Latest Ip:</white> <red>" + data.getLatestIp() + "</red>\n" +
                "<white>Report Score:</white> <red>" + data.getReportScore() + "</red>\n" +
                "<white>Punishment score:</white> <red>" + data.getPunishmentScore() + "</red>\n" +
                "<white>Active Notifications:</white> " +
                getNotificationList(data.getNotifications()) + "\n\n" +
                "<white>Previous Punishments:</white> \n" +
                data.getFormattedPunishments();

        return MiniMessage.miniMessage().deserialize(raw);
    }

    public static String getNotificationList(ArrayList<Notification> notifications){
        StringBuilder notifList = new StringBuilder();
        for(Notification notif : notifications)
            notifList.append("<br><gray>-</gray> <white>").append(StringUtils.Companion.prettyEnum(notif)).append("</white>");

        return notifList.toString();
    }
}
