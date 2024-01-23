package me.aragot.hglmoderation.discord.commands;

import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandExecutor {

    private final SlashCommandInteractionEvent event;
   public CommandExecutor(SlashCommandInteractionEvent event){
       //Usually do calculations and needed variables here, dunno if needed :/
       this.event = event;
   }

   public void onReport(){

   }

   public void onLogs(){
       switch(event.getSubcommandName()){ //Cannot be null
           case "set":
               Channel ch = event.getOption("logchannel").getAsChannel(); //Cannot be null
               if(ch.getType() != ChannelType.TEXT)
                   event.replyEmbeds(
                       HGLBot.getEmbedTemplate(ResponseType.ERROR, "Sorry, you can only use text channels for the logchannel").build()
                   ).queue();

               Config.instance.setReportChannelId(ch.getId());
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


}
