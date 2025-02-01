package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.repository.PunishmentRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;

import java.time.Instant;

public class UnpunishCommand {

    public static BrigadierCommand createBrigadierCommand(){
        LiteralCommandNode<CommandSource> dcBotNode = BrigadierCommand.literalArgumentBuilder("unpunish")
                .requires(source -> source.hasPermission("hglmoderation.unpunish"))
                .executes(context -> {
                    Responder.respond(context.getSource(), "Invalid usage. Please try using <white>/unpunish <punishmentId></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("id", StringArgumentType.word())
                        .executes(context -> {
                            String id = context.getArgument("id", String.class);
                            PunishmentRepository repository = new PunishmentRepository();
                            Punishment punishment = repository.getPunishmentById(id.toUpperCase());

                            if(punishment == null){
                                Responder.respond(context.getSource(), "Sorry but I couldn't find a punishment with the mentioned ID.", ResponseType.ERROR);
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!punishment.isActive()){
                                Responder.respond(context.getSource(), "Sorry but this punishment is currently not active.", ResponseType.ERROR);
                                return Command.SINGLE_SUCCESS;
                            }

                            punishment.setEndsAt(Instant.now().getEpochSecond());
                            PunishmentRepository punishmentRepository = new PunishmentRepository();
                            boolean updated = punishmentRepository.updateData(punishment);

                            if(!updated){
                                Responder.respond(context.getSource(), "Couldn't update the Punishment. Please try again later.", ResponseType.ERROR);
                                return Command.SINGLE_SUCCESS;
                            }

                            Punishment mute = PlayerListener.Companion.getPlayerMutes().get(punishment.getIssuedTo());
                            if(mute != null && punishment.getId().equalsIgnoreCase(mute.getId())){
                                PlayerListener.Companion.getPlayerMutes().remove(punishment.getIssuedTo());
                            }

                            Responder.respond(context.getSource(), "Successfully ended Punishment(" + punishment.getId() + ") early.", ResponseType.SUCCESS);

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();
        return new BrigadierCommand(dcBotNode);
    }
}
