package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.entity.reports.ReportState;
import me.aragot.hglmoderation.repository.ReportRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.report.ReportConverter;
import me.aragot.hglmoderation.service.report.ReportManager;
import me.aragot.hglmoderation.service.player.PlayerUtils;
import me.aragot.hglmoderation.service.permissions.PermCompare;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ReviewCommand {

    public static BrigadierCommand createBrigadierCommand(){
        LiteralCommandNode<CommandSource> reviewNode = BrigadierCommand.literalArgumentBuilder("review")
                .requires(source -> source.hasPermission("hglmoderation.review"))
                //execute when /review
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond(source, "Invalid usage. Please try using <white>/review <reportID></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("reportID", StringArgumentType.word())

                        .executes(context -> {
                            String reportId = context.getArgument("reportID", String.class);
                            Player executedBy = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            if(executedBy == null) return BrigadierCommand.FORWARD;
                            ReportRepository reportRepository = new ReportRepository();
                            Report report = reportRepository.getReportById(reportId);

                            if(report == null){
                                Responder.respond(executedBy, "Sorry, but I wasn't able to find this report.", ResponseType.ERROR);
                                return Command.SINGLE_SUCCESS;
                            }

                            try {
                                int permission = PermCompare.comparePermissionOf(executedBy.getUniqueId(), UUID.fromString(report.getReportedUUID())).get();
                                if(permission != PermCompare.GREATER_THAN){
                                    Responder.respond(executedBy, "Sorry but you don't have enough permissions to review this report. The reported user has a higher role than you.", ResponseType.ERROR);
                                    return Command.SINGLE_SUCCESS;
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            if (report.getState() == ReportState.OPEN) {
                                new ReportManager().startReview(report, executedBy.getUniqueId().toString());
                            } else if(!report.getReviewedBy().equalsIgnoreCase(executedBy.getUniqueId().toString())){
                                String reviewer = PlayerUtils.Companion.getUsernameFromUUID(report.getReviewedBy());

                                Responder.respond(
                                        executedBy,
                                        "Thank you for the engagement, but this report " + (report.getState() == ReportState.DONE ? "was already <blue>reviewed</blue>" : "is already <yellow>under review</yellow>") + " by <red>" + reviewer +"</red>.",
                                        ResponseType.DEFAULT
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            executedBy.sendMessage(ReportConverter.Companion.getMCReportActions(report));

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
                                    ReportRepository reportRepository = new ReportRepository();
                                    Report report = reportRepository.getReportById(reportId);

                                    if(report == null){
                                        Responder.respond(executedBy, "Sorry, but I wasn't able to find this report.", ResponseType.ERROR);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if((report.getState() == ReportState.DONE) ||
                                       (report.getState() == ReportState.UNDER_REVIEW && !report.getReviewedBy().equalsIgnoreCase(executedBy.getUniqueId().toString()))){
                                        if(report.getReviewedBy() == null || report.getReviewedBy().isEmpty()){
                                            Responder.respond(executedBy, "Please start reviewing this report before handling it. Use /review " + report.getId() + " to start the review process", ResponseType.ERROR);
                                            return Command.SINGLE_SUCCESS;
                                        }
                                        String reviewer = PlayerUtils.Companion.getUsernameFromUUID(report.getReviewedBy());
                                        Responder.respond(executedBy,
                                                "Sorry but this is not within your scope. Please contact <red>" + reviewer + "</red> to talk about this case.",
                                                ResponseType.DEFAULT);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    //Report is from myself and under_review or open
                                    ReportManager manager = new ReportManager();
                                    String action = context.getArgument("action", String.class);
                                    if(action.equalsIgnoreCase("decline")){
                                        manager.decline(report);
                                        Responder.respond(executedBy,
                                                "<green>Thank you for reviewing this report! Keep up the good work :)</green>",
                                                ResponseType.DEFAULT);

                                    } else if(action.equalsIgnoreCase("malicious")){
                                        manager.malicious(report);

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
