package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.repository.PunishmentRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.player.PlayerUtils;
import me.aragot.hglmoderation.service.punishment.PunishmentConverter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UnpunishCommand {

    public static BrigadierCommand createBrigadierCommand() {
        LiteralCommandNode<CommandSource> unpunishNode = BrigadierCommand.literalArgumentBuilder("unpunish")
                .requires(source -> source.hasPermission("hglmoderation.unpunish"))
                .executes(UnpunishCommand::handleInvalidUsage)
                .then(BrigadierCommand.requiredArgumentBuilder("type", StringArgumentType.word())
                        .executes(UnpunishCommand::handleInvalidUsage)
                        .then(BrigadierCommand.requiredArgumentBuilder("id", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("player");
                                    builder.suggest("punishment");

                                    return builder.buildFuture();
                                })
                                .executes(UnpunishCommand::handleTypeAndId)
                        )
                )
                .build();

        return new BrigadierCommand(unpunishNode);
    }

    public static int handleInvalidUsage(CommandContext<CommandSource> context) {
        Responder.respond(context.getSource(), "Invalid usage. Please try using <white>/unpunish <player/punishment> <name/punishmentId></white>", ResponseType.ERROR);

        return Command.SINGLE_SUCCESS;
    }

    public static int handleTypeAndId(CommandContext<CommandSource> context) {
        String type = context.getArgument("type", String.class).toLowerCase();
        switch (type) {
            case "player" -> {
                return handleUnpunishOfPlayer(context);
            }
            case "punishment" -> {
                return handleUnpunishOfPunishment(context);
            }
            default -> {
                return handleInvalidUsage(context);
            }
        }
    }

    public static int handleUnpunishOfPlayer(CommandContext<CommandSource> context) {
        String id = context.getArgument("id", String.class);

        UUID userId = null;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {}





        if (userId == null) {
            userId = PlayerUtils.Companion.getUuidFromUsername(id);
        }

        if (userId == null) {
            Responder.respond(context.getSource(), "Sorry, but this user was not on the server before", ResponseType.ERROR);

            return Command.SINGLE_SUCCESS;
        }

        PlayerDataRepository playerDataRepository = new PlayerDataRepository();
        PlayerData data = playerDataRepository.getPlayerData(userId);

        if (data == null) {
            Responder.respond(context.getSource(), "Sorry, but this user was not on the server before", ResponseType.ERROR);

            return Command.SINGLE_SUCCESS;
        }

        PunishmentRepository punishmentRepository = new PunishmentRepository();
        List<Punishment> activePunishments = punishmentRepository.getActivePunishmentsFor(data.getId(), data.getLatestIp());

        if (activePunishments.isEmpty()) {
            Responder.respond(context.getSource(), "This user does not have any active Punishments", ResponseType.DEFAULT);

            return Command.SINGLE_SUCCESS;
        }

        Responder.respond(context.getSource(), PunishmentConverter.Companion.getFormattedUnpunishComponents(activePunishments), ResponseType.DEFAULT);
        return Command.SINGLE_SUCCESS;
    }

    public static int handleUnpunishOfPunishment(CommandContext<CommandSource> context) {
        String id = context.getArgument("id", String.class);
        PunishmentRepository repository = new PunishmentRepository();
        Punishment punishment = repository.getPunishmentById(id.toUpperCase());

        if (punishment == null) {
            Responder.respond(context.getSource(), "Sorry but I couldn't find a punishment with the mentioned ID.", ResponseType.ERROR);
            return Command.SINGLE_SUCCESS;
        }

        if (!punishment.isActive()) {
            Responder.respond(context.getSource(), "Sorry but this punishment is currently not active.", ResponseType.ERROR);
            return Command.SINGLE_SUCCESS;
        }

        punishment.setEndsAt(Instant.now().getEpochSecond());
        PunishmentRepository punishmentRepository = new PunishmentRepository();
        boolean updated = punishmentRepository.updateData(punishment);

        if (!updated) {
            Responder.respond(context.getSource(), "Couldn't update the Punishment. Please try again later.", ResponseType.ERROR);
            return Command.SINGLE_SUCCESS;
        }
        UUID playerUuid = null;

        try {
            playerUuid = UUID.fromString(punishment.getIssuedTo());
        } catch (IllegalArgumentException ignored) {

        }

        if (playerUuid == null) {
            // This means that the punishment was an IP related punishment
            Responder.respond(context.getSource(), "Successfully ended Punishment(" + punishment.getId() + ") early.", ResponseType.SUCCESS);
            return Command.SINGLE_SUCCESS;
        }

        Punishment mute = PlayerListener.Companion.getPlayerMutes().get(playerUuid);
        if (mute != null && punishment.getId().equalsIgnoreCase(mute.getId())) {
            PlayerListener.Companion.getPlayerMutes().remove(playerUuid);
        }

        Responder.respond(context.getSource(), "Successfully ended Punishment(" + punishment.getId() + ") early.", ResponseType.SUCCESS);

        return Command.SINGLE_SUCCESS;
    }
}
