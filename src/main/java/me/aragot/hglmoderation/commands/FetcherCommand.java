package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FetcherCommand {

    public static BrigadierCommand createBrigadierCommand(){
        LiteralCommandNode<CommandSource> dcBotNode = BrigadierCommand.literalArgumentBuilder("dcbot")
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

                            switch(type){
                                case "player_data":
                                    break;
                                case "report":
                                    ArrayList<Report> openReports = HGLModeration.instance.getDatabase().getReportsByState(ReportState.OPEN);
                                    context.getSource().sendMessage(getComponentForReports(openReports));
                                    break;
                                case "punishment":
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
                                .executes(context -> {
                                    String id = context.getArgument("id", String.class);
                                    String type = context.getArgument("type", String.class);

                                    switch(type){
                                        case "report":
                                            Report report = HGLModeration.instance.getDatabase().getReportById(id);
                                            if(report == null){
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find this report in the database", ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            context.getSource().sendMessage(report.getMCReportComponent(false));
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
        return new BrigadierCommand(dcBotNode);
    }

    public static Component getComponentForReports(ArrayList<Report> reportList){
        StringBuilder reports = new StringBuilder(Responder.prefix + " <gold>Currently open Reports:</gold>");
        if(reportList.isEmpty()) return MiniMessage.miniMessage().deserialize(reports.append("<white> None</white>").toString());

        HashMap<String, String> userNameCache = new HashMap<>();
        int displayMax = 10;
        int count = 0;

        for(Report report : reportList){
            if(count == displayMax) break;
            if(userNameCache.get(report.getReportedUUID()) == null){
                userNameCache.put(report.getReportedUUID(), PlayerUtils.getUsernameFromUUID(UUID.fromString(report.getReportedUUID())));
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
                    .append("</red>").append("\n").append(report.getViewDetailsRaw())
                    .append("   ").append(fetchReport);

            count++;
        }

        return MiniMessage.miniMessage().deserialize(reports.toString());
    }
}
