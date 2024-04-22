package me.aragot.hglmoderation.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.discord.commands.CommandExecutor;
import me.aragot.hglmoderation.repository.PlayerDataRepository;
import me.aragot.hglmoderation.response.Responder;
import me.aragot.hglmoderation.response.ResponseType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class LinkCommand {

    public static BrigadierCommand createBrigadierCommand(){
        LiteralCommandNode<CommandSource> linkNode = BrigadierCommand.literalArgumentBuilder("link")
                .requires(source -> source.hasPermission("hglmoderation.link"))
                .executes(context -> {

                    CommandSource source = context.getSource();
                    if(source instanceof Player)
                        Responder.respond(source, "Invalid usage. Please try using <white>/link <key/reset>", ResponseType.ERROR);

                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("reset");
                            builder.suggest("key");
                            return builder.buildFuture();
                        })
                        .executes(LinkCommand::getCommandStatus)
                )

                .build();
        return new BrigadierCommand(linkNode);
    }

    public static int getCommandStatus(CommandContext<CommandSource> context){
        String action = context.getArgument("action", String.class);
        Player player = context.getSource() instanceof Player ? (Player) context.getSource() : null;
        PlayerDataRepository repository = new PlayerDataRepository();

        if(player == null) return BrigadierCommand.FORWARD;


        if(action.equalsIgnoreCase("reset")){
            PlayerData data = repository.getPlayerData(player);
            data.setDiscordId("");

            Responder.respond(player, "Successfully unbound your account!", ResponseType.SUCCESS);
            return Command.SINGLE_SUCCESS;
        }

        Map.Entry<Instant, String> keyData;

        try{
            keyData = CommandExecutor.discordLinkKeys.get(UUID.fromString(action));
        } catch (IllegalArgumentException x) {
            Responder.respond(player, "Invalid key. Please request a new key from the discord bot by using <white>/link</white> in the discord server.", ResponseType.ERROR);
            return Command.SINGLE_SUCCESS;
        }

        if(keyData == null){
            Responder.respond(player, "Invalid key. Please request a new key from the discord bot by using <white>/link</white> in the discord server.", ResponseType.ERROR);
            return Command.SINGLE_SUCCESS;
        }

        //900 Secs == 15 Minutes
        if(keyData.getKey().plusSeconds(900).isBefore(Instant.now())){
            Responder.respond(player, "Your key expired. Please request a new key from the discord bot by using <white>/link</white> in the discord server.", ResponseType.ERROR);
            return Command.SINGLE_SUCCESS;
        }

        //If here then it exists and is less than 15 minutes ago
        PlayerData data = repository.getPlayerData(player);
        data.setDiscordId(keyData.getValue());

        Responder.respond(player, "Successfully linked your account to your discord account!", ResponseType.SUCCESS);

        return Command.SINGLE_SUCCESS;
    }

}
