package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;

import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;

public class PresetCommand {


    private static final String invalidUsage = "Invalid usage. Please try using <white>/preset <apply> <presetName> <reportId></white> or <white>/preset <info> <presetName></white>";

    public static BrigadierCommand createBrigadierCommand(ProxyServer server){
        LiteralCommandNode<CommandSource> reviewNode = BrigadierCommand.literalArgumentBuilder("preset")
                .requires(source -> source.hasPermission("hglmoderation.punish"))
                //execute when /review
                .executes(context -> {

                    Responder.respond(context.getSource(),
                            invalidUsage,
                            ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("apply");
                            builder.suggest("info");
                            return builder.buildFuture();
                        })
                        .executes(context -> {

                            Responder.respond(context.getSource(),
                                    invalidUsage,
                                    ResponseType.ERROR);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("presetName", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for(Preset preset : PresetHandler.instance.getPresetList()){
                                        builder.suggest(preset.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    //Executes when /preset info/apply preset_name
                                    String presetName = context.getArgument("presetName", String.class);
                                    String action = context.getArgument("action", String.class);


                                    if(!action.equalsIgnoreCase("info")){
                                        Responder.respond(context.getSource(),
                                                invalidUsage,
                                                ResponseType.ERROR);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Preset preset = PresetHandler.instance.getPresetByName(presetName);

                                    if(preset == null){

                                        Responder.respond(context.getSource(), "Sorry but I couldn't find the preset you were looking for.", ResponseType.ERROR);

                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String presetDisplay = Responder.prefix + " Preset Information:\n" +
                                            "<gray>Preset Name:</gray> <blue>" + preset.getName() + "</blue>\n" +
                                            "<gray>Description:</gray> <blue>" + preset.getDescription() + "</blue>\n" +
                                            "<gray>Range:</gray> <blue>" + preset.getStart() + " <gray>-></gray> " + preset.getEnd() +  "</blue>\n" +
                                            "<gray>Weight:</gray> <blue>" + preset.getWeight() + "</blue>\n" +
                                            "<gray>Duration:</gray> <blue>" + preset.getDurationAsString() + "</blue>\n" +
                                            "<gray>Reasoning Scope:</gray> <blue>" + preset.getReasoningScopeAsString() + "</blue>\n" +
                                            "<gray>Punishments:</gray> <blue>" + preset.getPunishmentTypesAsString() + "</blue>\n";

                                    context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(presetDisplay));

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(BrigadierCommand.requiredArgumentBuilder("reportId", StringArgumentType.word())
                                        .executes(context -> {
                                            //executes when /preset info/apply
                                            String presetName = context.getArgument("presetName", String.class);
                                            String action = context.getArgument("action", String.class);
                                            String reportId = context.getArgument("reportId", String.class);
                                            Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;

                                            if(player == null)
                                                return Command.SINGLE_SUCCESS;

                                            if(!action.equalsIgnoreCase("apply")){
                                                Responder.respond(context.getSource(),
                                                        invalidUsage,
                                                        ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Preset preset = PresetHandler.instance.getPresetByName(presetName);

                                            if(preset == null){
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find the preset you were looking for.", ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Report report = HGLModeration.instance.getDatabase().getReportById(reportId);

                                            if(report == null){
                                                Responder.respond(context.getSource(), "Sorry but I couldn't find the report you were looking for.", ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(report.getState() == ReportState.DONE){
                                                Responder.respond(context.getSource(), "Sorry but this report was already reviewed. Please check the ReportID for errors.", ResponseType.ERROR);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Player reviewedBy = server.getPlayer(UUID.fromString(report.getReviewedBy())).get();
                                            if(!report.getReviewedBy().equalsIgnoreCase(player.getUniqueId().toString())){
                                                Responder.respond(player,
                                                        "Sorry but this is not within your scope. Please contact <red>" + reviewedBy.getUsername() +"</red> to talk about this case.",
                                                        ResponseType.DEFAULT);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            preset.apply(report);
                                            Responder.respond(context.getSource(),
                                                    "<green>Thank you for reviewing this report. The reported player was successfully. Keep up the good work :)</green>",
                                                    ResponseType.DEFAULT);

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )


                .build();
        return new BrigadierCommand(reviewNode);
    }
}