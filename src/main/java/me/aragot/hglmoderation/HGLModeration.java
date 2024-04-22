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
import me.aragot.hglmoderation.repository.ReportRepository;
import me.aragot.hglmoderation.service.database.ModerationDB;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.events.PlayerListener;
import me.aragot.hglmoderation.service.player.PlayerUtils;
import org.slf4j.Logger;

import java.util.NoSuchElementException;
import java.util.UUID;

@Plugin(
        id = "hglmoderation",
        name = "HGLModeration",
        version = "1.0-SNAPSHOT",
        authors = {"Aragot"}
)

/*TODO:
 * Discord Log change message? Channel.retrieveMessageById();
 *  -> Push report AFTER report message was sent to Channel
 *  -> Maybe ignore and delete options on click if its already reviewed?
 * Report add message id from discord in DB
 */

public class HGLModeration {
    private final Logger logger;
    private final ProxyServer server;
    private ModerationDB database;

    public static HGLModeration instance;

    @Inject
    public HGLModeration(ProxyServer server, Logger logger) {
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
        ReportRepository reportRepository = new ReportRepository();
        reportRepository.fetchUnfinishedReports();
    }

   @Subscribe
   public void onProxyShutdown(ProxyShutdownEvent event) {
       Config.saveConfig();
       PresetHandler.savePresets();
       database.closeConnection();
   }

    private void registerEventListeners() {
        this.server.getEventManager().register(this, new PlayerListener());
    }

    private void registerCommands() {
        CommandManager manager = this.server.getCommandManager();

        BrigadierCommand reportCommand = ReportCommand.createBrigadierCommand(this.server);
        CommandMeta reportMeta = manager.metaBuilder("report")
                .aliases("rep")
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

        BrigadierCommand reviewCommand = ReviewCommand.createBrigadierCommand();
        CommandMeta reviewMeta = manager.metaBuilder("review")
                .plugin(this)
                .build();

        BrigadierCommand presetCommand = PresetCommand.createBrigadierCommand();
        CommandMeta presetMeta = manager.metaBuilder("preset")
                .plugin(this)
                .build();

        BrigadierCommand punishCommand = PunishCommand.createBrigadierCommand();
        CommandMeta punishMeta = manager.metaBuilder("punish")
                .plugin(this)
                .build();

        BrigadierCommand unpunishCommand = UnpunishCommand.createBrigadierCommand();
        CommandMeta unpunishMeta = manager.metaBuilder("unpunish")
                .plugin(this)
                .build();

        BrigadierCommand fetcherCommand = FetcherCommand.createBrigadierCommand();
        CommandMeta fetcherMeta = manager.metaBuilder("fetcher")
                .plugin(this)
                .build();

        //Actual register
        manager.register(reportMeta, reportCommand);
        manager.register(notifMeta, notifCommand);
        manager.register(linkMeta, linkCommand);
        manager.register(reviewMeta, reviewCommand);
        manager.register(presetMeta, presetCommand);
        manager.register(punishMeta, punishCommand);
        manager.register(unpunishMeta, unpunishCommand);
        manager.register(fetcherMeta, fetcherCommand);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getServer() {
        return this.server;
    }

    public ModerationDB getDatabase() {
        return this.database;
    }

    //Maybe remove this method? Cache is quite efficient?
    public String getPlayerNameEfficiently(String uuid) {
        try {
            return server.getPlayer(UUID.fromString(uuid)).orElseThrow().getUsername();
        } catch(NoSuchElementException x) {
            return PlayerUtils.Companion.getUsernameFromUUID(uuid);
        }
    }
}
