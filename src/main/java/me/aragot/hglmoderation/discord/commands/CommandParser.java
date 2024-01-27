package me.aragot.hglmoderation.discord.commands;

import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandParser extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        CommandExecutor executor = new CommandExecutor(event);

        switch(event.getFullCommandName().split(" ")[0]){
            case "logs":
                executor.onLogs();
                break;
            case "report":
                executor.onReport();
                break;
            default:
                EmbedBuilder eb = HGLBot.getEmbedTemplate(ResponseType.ERROR);
                eb.setDescription("How did you get here? Read the footer! This is definitely a bug!");
                event.replyEmbeds(eb.build()).queue();
                break;
        }
    }
}
