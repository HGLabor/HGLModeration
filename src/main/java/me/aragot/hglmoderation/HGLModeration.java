package me.aragot.hglmoderation;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.commands.*;
import me.aragot.hglmoderation.database.ModerationDB;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;
import org.slf4j.Logger;

@Plugin(
        id = "hglmoderation",
        name = "HGLModeration",
        version = "1.0-SNAPSHOT",
        authors = {"Aragot"}
)

/**
 * To do:
 * Something like /report list to see all currently active reports -> Discord as well
 * /Punish command for minecraft ingame -> No Discord Integration
 * Discord Integration of using punishment preset
 * Discord Log change message?
 *  -> Maybe ignore and delete options on click if its already reviewed?
 * Permission checks
 * Message Reporters when finishing review?
 * How to handle
 */

public class HGLModeration {

    private final Logger logger;
    private final ProxyServer server;
    private ModerationDB database;

    public static HGLModeration instance;


    @Inject
    public HGLModeration(ProxyServer server, Logger logger){
        this.server = server;
        this.logger = logger;
        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerEventListeners();
        registerCommands();
        Config.loadConfig();
        PresetHandler.loadPresets();

        HGLBot.init(this.server, this.logger);
        this.database =  new ModerationDB(Config.instance.getDbConnectionString());

    }

   @Subscribe
   public void onProxyShutdown(ProxyShutdownEvent event) {
       Config.saveConfig();
       PresetHandler.savePresets();
       database.closeConnection();
   }

    private void registerEventListeners(){
        this.server.getEventManager().register(this, new PlayerListener());
    }

    private void registerCommands(){
        CommandManager manager = this.server.getCommandManager();

        BrigadierCommand reportCommand = ReportCommand.createBrigadierCommand(this.server);
        CommandMeta reportMeta = manager.metaBuilder("report")
                .aliases("rep")
                .plugin(this)
                .build();


        BrigadierCommand dcBotCommand = DiscordBotCommand.createBrigadierCommand(this.server, this.logger);
        CommandMeta dcBotMeta = manager.metaBuilder("dcbot")
                .aliases("dc")
                .plugin(this)
                .build();

        BrigadierCommand notifCommand = NotificationCommand.createBrigadierCommand();
        CommandMeta notifMeta = manager.metaBuilder("notification")
                .aliases("notif")
                .plugin(this)
                .build();

        BrigadierCommand linkCommand = LinkCommand.createBrigadierCommand();
        CommandMeta linkMeta = manager.metaBuilder("link")
                .plugin(this)
                .build();

        BrigadierCommand reviewCommand = ReviewCommand.createBrigadierCommand(this.server);
        CommandMeta reviewMeta = manager.metaBuilder("review")
                .plugin(this)
                .build();

        BrigadierCommand presetCommand = PresetCommand.createBrigadierCommand(this.server);
        CommandMeta presetMeta = manager.metaBuilder("preset")
                .plugin(this)
                .build();

        //Actual register
        manager.register(reportMeta, reportCommand);
        manager.register(dcBotMeta, dcBotCommand);
        manager.register(notifMeta, notifCommand);
        manager.register(linkMeta, linkCommand);
        manager.register(reviewMeta, reviewCommand);
        manager.register(presetMeta, presetCommand);
    }

    public Logger getLogger(){
        return this.logger;
    }

    public ProxyServer getServer(){
        return this.server;
    }

    public ModerationDB getDatabase(){
        return this.database;
    }

}
