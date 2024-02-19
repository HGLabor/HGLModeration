package me.aragot.hglmoderation.discord.commands;

import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.time.Instant;
import java.util.*;

public class CommandExecutor {
    private final SlashCommandInteractionEvent event;
    public static HashMap<UUID, Map.Entry<Instant, String>> discordLinkKeys = new HashMap<>();
    public CommandExecutor(SlashCommandInteractionEvent event){
       //Usually do calculations and needed variables here, dunno if needed :/
        this.event = event;
    }

    public void onReport(){
    }

    public void onPreset(){

        EmbedBuilder eb = HGLBot.getEmbedTemplate(ResponseType.DEFAULT);
        eb.setTitle("Presets");
        eb.setDescription("Welcome to the PresetGUI. You can view, modify, add and remove the current presets! Please choose an option from the menu below to start your journey.");

        StringSelectMenu.Builder presetPicker = StringSelectMenu.create("preset-picker");
        presetPicker.setPlaceholder("Choose an Option");

        for(Preset preset : PresetHandler.instance.getPresetList()){
            presetPicker.addOption(preset.getName(), preset.getName().toLowerCase(), preset.getDescription(), Emoji.fromUnicode("\uD83D\uDD27"));
        }

        presetPicker.addOption("Add new Preset", "preset-add", "Creates a new Preset to the list", Emoji.fromUnicode("\u2795"));

        event.replyEmbeds(eb.build()).setEphemeral(true)
                .addActionRow(presetPicker.build())
                .queue();

    }

    public void onLogs(){
        switch(event.getSubcommandName()){ //Cannot be null
            case "set":
                Channel ch = event.getOption("logchannel").getAsChannel(); //Cannot be null
                String channelType = event.getOption("type").getAsString();

                if(ch.getType() != ChannelType.TEXT)
                    event.replyEmbeds(
                       HGLBot.getEmbedTemplate(ResponseType.ERROR, "Sorry, you can only use text channels for the logchannel").build()
                    ).queue();

                if(channelType.equalsIgnoreCase("report"))
                    Config.instance.setReportChannelId(ch.getId());
                else if(channelType.equalsIgnoreCase("punishment"))
                    Config.instance.setPunishmentChannelId(ch.getId());
                else {
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "The type " + channelType + " is not valid.").build()).queue();
                    return;
                }

                event.replyEmbeds(
                        HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully set the log channel to: <#" + Config.instance.getReportChannelId() + ">").build()
                ).queue();
               break;

            case "pingrole":

                if(event.getOption("role") == null){
                    Config.instance.setReportPingroleId("");
                    event.replyEmbeds(
                            HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully unset the pingrole!").build()
                    ).queue();
                    return;
                }

                Role role = event.getOption("role").getAsRole();
                Config.instance.setReportPingroleId(role.getId());

                event.replyEmbeds(
                        HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully set the pingrole to: <@&" + Config.instance.getReportPingroleId() + ">").build()
                ).queue();
                break;
        }
    }
    public void onLink(){
        if(event.getSubcommandName().equalsIgnoreCase("generate")){ //cannot be null
            UUID key = UUID.randomUUID();
            discordLinkKeys.put(key, Map.entry(Instant.now(), event.getUser().getId()));
            EmbedBuilder eb = HGLBot.getEmbedTemplate(ResponseType.DEFAULT);
            String desc = "Successfully generated a new key to link your account." +
                    " Due to security purposes your key will expire within the next 15 minutes." +
                    " Run the following command on the server to finish linking: ```" +
                    "/link " + key +
                    "```";
            eb.setDescription(desc);
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        } else if(event.getSubcommandName().equalsIgnoreCase("reset")){
            PlayerData data = getDataByDiscordId(event.getUser().getId());
            if(data != null){
                data.setDiscordId("");
                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully unbound your discord account!").build()).queue();
            } else {
                event.replyEmbeds(
                        HGLBot.getEmbedTemplate(ResponseType.ERROR, "Your account is not linked to any minecraft account. Please Link your account first by using ``/link``.")
                                .build()
                ).queue();
            }
        }
    }

    private PlayerData getDataByDiscordId(String discordId){
       for(PlayerData data : PlayerData.dataList){
           if(discordId.equalsIgnoreCase(data.getDiscordId())) return data;
       }
       return null;
    }

}
