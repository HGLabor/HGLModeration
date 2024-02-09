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
import me.aragot.hglmoderation.commands.DiscordBotCommand;
import me.aragot.hglmoderation.commands.LinkCommand;
import me.aragot.hglmoderation.commands.NotificationCommand;
import me.aragot.hglmoderation.commands.ReportCommand;
import me.aragot.hglmoderation.data.reports.Report;
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

        HGLBot.init(this.server, this.logger);
        ModerationDB database = new ModerationDB(Config.instance.getDbConnectionString());
        this.database = database;

    }

   @Subscribe
   public void onProxyShutdown(ProxyShutdownEvent event) {
       Config.saveConfig();
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


        //Actual register
        manager.register(reportMeta, reportCommand);
        manager.register(dcBotMeta, dcBotCommand);
        manager.register(notifMeta, notifCommand);
        manager.register(linkMeta, linkCommand);
    }


    public Logger getLogger(){
        return this.logger;
    }

    public ModerationDB getDatabase(){
        return this.database;
    }

}
