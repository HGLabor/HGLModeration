package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.entity.Notification;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.service.StringUtils;

public class NotificationCommand {
    public static BrigadierCommand createBrigadierCommand(){
        LiteralCommandNode<CommandSource> notificationNode = BrigadierCommand.literalArgumentBuilder("notification")
                .requires(source -> source.hasPermission("hglmoderation.notification"))
                .executes(context -> {
                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond(source, "Invalid usage. Please try using <white>/notification <add/remove/list> <type></white>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("add");
                            builder.suggest("remove");
                            builder.suggest("list");

                            return builder.buildFuture();
                        })

                        .executes(context -> {

                            String action = context.getArgument("action", String.class);
                            Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                            PlayerDataRepository repository = new PlayerDataRepository();

                            if(player == null)
                                return BrigadierCommand.FORWARD;

                            if(action.equalsIgnoreCase("list")){
                                PlayerData data = repository.getPlayerData(player);
                                StringBuilder notificationGroups = new StringBuilder(data.getNotifications().isEmpty() ? "<br><gray>-</gray> <white>None</white>" : "");

                                for(Notification notif : data.getNotifications())
                                    notificationGroups.append("<br><gray>-</gray> <white>").append(StringUtils.Companion.prettyEnum(notif)).append("</white>");

                                Responder.respond(player, "You're currently in the following notification groups:<br>" + notificationGroups, ResponseType.SUCCESS);
                                return Command.SINGLE_SUCCESS;
                            }

                            Responder.respond(player, "Invalid usage. Please try using <white>/notification <add/remove/list> <type></white>", ResponseType.ERROR);

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(BrigadierCommand.requiredArgumentBuilder("type", StringArgumentType.word())
                                .suggests((context, builder) -> {

                                    boolean hasPerms = context.getSource().hasPermission("hglmoderation.moderation.notifications");
                                    for(Notification notif : Notification.values()){
                                        if(notif.requiresPermission() && !hasPerms)
                                            continue;
                                        builder.suggest(notif.name());
                                    }

                                    return builder.buildFuture();
                                })

                                .executes(context -> {
                                    String action = context.getArgument("action", String.class);
                                    String type = context.getArgument("type", String.class);
                                    Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
                                    PlayerDataRepository repository = new PlayerDataRepository();

                                    if(player == null) return BrigadierCommand.FORWARD;

                                    Notification notif;

                                    try {
                                        notif = Notification.valueOf(type.toUpperCase());
                                    } catch (IllegalArgumentException x) {
                                        Responder.respond(player, "Sorry, but this notification group doesn't exist", ResponseType.ERROR);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    boolean hasPerms = context.getSource().hasPermission("hglmoderation.moderation.notifications");
                                    if(notif.requiresPermission() && !hasPerms){
                                        Responder.respond(player, "You don't have the permissions modify these notifications!", ResponseType.ERROR);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    //Retrieve data in if statements to be more efficient

                                    if(action.equalsIgnoreCase("add")){
                                        PlayerData data = repository.getPlayerData(player);
                                        data.addNotification(notif);
                                        Responder.respond(player, "Successfully added you to the " + StringUtils.Companion.prettyEnum(notif) + " notification group!", ResponseType.SUCCESS);
                                    } else if(action.equalsIgnoreCase("remove")){
                                        PlayerData data = repository.getPlayerData(player);
                                        data.removeNotification(notif);
                                        Responder.respond(player, "Successfully removed you to the " + StringUtils.Companion.prettyEnum(notif) + " notification group!", ResponseType.SUCCESS);
                                    } else {
                                        Responder.respond(player, "Sorry, but this is not a valid action. Maybe try using: <white>/notification list", ResponseType.ERROR);
                                    }


                                    return Command.SINGLE_SUCCESS;
                                }))
                )

                .build();
        return new BrigadierCommand(notificationNode);
    }
}
