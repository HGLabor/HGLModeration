package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;

import java.util.UUID;

public class ReviewCommand {

    public static BrigadierCommand createBrigadierCommand(ProxyServer server){
        LiteralCommandNode<CommandSource> reviewNode = BrigadierCommand.literalArgumentBuilder("review")
                .requires(source -> source.hasPermission("hglmoderation.review"))
                //execute when /review
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond((Player) source, "Invalid usage. Please try using <white>/review <reportID></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("reportID", StringArgumentType.word())

                        .executes(context -> {
                            String reportId = context.getArgument("reportID", String.class);
                            Player executedBy = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            if(executedBy == null) return BrigadierCommand.FORWARD;

                            Report report = HGLModeration.instance.getDatabase().getReportById(reportId);

                            if(report == null){
                                Responder.respond(executedBy, "Sorry, but I wasn't able to find this report.", ResponseType.ERROR);
                                return Command.SINGLE_SUCCESS;
                            }

                            if(report.getState() != ReportState.OPEN){
                                Player reviewedBy = server.getPlayer(UUID.fromString(report.getReviewedBy())).get();
                                Responder.respond(executedBy,
                                        "Thank you for the engagement, but this report " + report.getFormattedState() + " by <red>" + reviewedBy.getUsername() +"</red>.",
                                        ResponseType.DEFAULT);
                                return Command.SINGLE_SUCCESS;
                            }

                            report.setReviewedBy(executedBy.getUniqueId().toString());
                            report.setState(ReportState.UNDER_REVIEW);

                            HGLModeration.instance.getDatabase().updateReportsBasedOn(report);

                            executedBy.sendMessage(report.getMCReportActions());

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("decline");
                                    builder.suggest("malicious");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String reportId = context.getArgument("reportID", String.class);
                                    Player executedBy = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                    if(executedBy == null) return BrigadierCommand.FORWARD;

                                    Report report = HGLModeration.instance.getDatabase().getReportById(reportId);

                                    if(report == null){
                                        Responder.respond(executedBy, "Sorry, but I wasn't able to find this report.", ResponseType.ERROR);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(report.getState() == ReportState.DONE ||
                                            (report.getState() == ReportState.UNDER_REVIEW && !report.getReviewedBy().equalsIgnoreCase(executedBy.getUniqueId().toString()))){

                                        Player reviewedBy = server.getPlayer(UUID.fromString(report.getReviewedBy())).get();
                                        Responder.respond(executedBy,
                                                "Sorry but this is not within your scope. Please contact <red>" + reviewedBy.getUsername() +"</red> to talk about this case.",
                                                ResponseType.DEFAULT);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    //Report is from myself and under_review or open
                                    String action = context.getArgument("action", String.class);
                                    if(action.equalsIgnoreCase("decline")){
                                        HGLModeration.instance.getDatabase().updateReports(report.getReportedUUID(), report.getReasoning(), ReportState.DONE);
                                        Responder.respond(executedBy,
                                                "<green>Thank you for reviewing this report! Keep up the good work :)</green>",
                                                ResponseType.DEFAULT);

                                    } else if(action.equalsIgnoreCase("malicious")){
                                        HGLModeration.instance.getDatabase().updateReports(report.getReportedUUID(), report.getReasoning(), ReportState.DONE);

                                        PlayerData reporterData = PlayerData.getPlayerData(report.getReporterUUID());

                                        reporterData.setReportScore(reporterData.getReportScore() - 2);

                                        Responder.respond(executedBy,
                                                "<green>Thank you for reviewing this report! The Reporter has been punished. Keep up the good work :)</green>",
                                                ResponseType.DEFAULT);

                                    } else {
                                        Responder.respond(executedBy,
                                                "Invalid action. Please try using: <white>/review <reportId> <decline:malicious></white>",
                                                ResponseType.ERROR);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )


                .build();
        return new BrigadierCommand(reviewNode);
    }
}
