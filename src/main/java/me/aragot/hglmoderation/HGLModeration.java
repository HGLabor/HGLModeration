package me.aragot.hglmoderation;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.aragot.hglmoderation.commands.ReportCommand;
import me.aragot.hglmoderation.events.PlayerListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    @Inject
    public HGLModeration(ProxyServer server, Logger logger){
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerEventListeners();
        registerCommands();
    }

    private void registerEventListeners(){
        this.server.getEventManager().register(this, new PlayerListener());
    }

    private void registerCommands(){
        CommandManager manager = this.server.getCommandManager();

        //Create CommandMetas here
        CommandMeta reportMeta = manager.metaBuilder("report")
                .aliases("rep")
                .plugin(this)
                .build();

        BrigadierCommand reportCommand = ReportCommand.createBrigadierCommand(this.server);


        //Actual register
        manager.register(reportMeta, reportCommand);
    }
}
