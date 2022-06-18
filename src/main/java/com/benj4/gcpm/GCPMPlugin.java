package com.benj4.gcpm;

import com.benj4.gcpm.commands.GCPMCommand;
import com.benj4.gcpm.handlers.GCPMPermissionHandler;
import com.benj4.gcpm.objects.GCPMGroup;
import com.google.gson.reflect.TypeToken;
import emu.grasscutter.Configuration;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.DefaultPermissionHandler;
import emu.grasscutter.data.DataLoader;
import emu.grasscutter.game.gacha.GachaBanner;
import emu.grasscutter.plugin.Plugin;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.player.PlayerJoinEvent;
import org.slf4j.Logger;
import com.benj4.gcpm.objects.PluginConfig;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Grasscutter Permission Manager
 * This is the main class for the plugin.
 */
public final class GCPMPlugin extends Plugin {
    /* Turn the plugin into a singleton. */
    private static GCPMPlugin instance;

    /**
     * Gets the plugin instance.
     * @return A plugin singleton.
     */
    public static GCPMPlugin getInstance() {
        return instance;
    }

    private final Logger logger = Grasscutter.getLogger();
    private PluginConfig configuration;
    private HashMap<String, GCPMGroup> Groups;
    
    /**
     * This method is called immediately after the plugin is first loaded into system memory.
     */
    @Override public void onLoad() {
        // Set the plugin instance.
        instance = this;
        Groups = new HashMap<String, GCPMGroup>();
        
        // Get the configuration file.
        File config = new File(this.getDataFolder(), "config.json");
        
        // Load the configuration.
        try {
            if(!config.exists() && !config.createNewFile()) {
                this.logger.error("Failed to create config file.");
            } else {
                try (FileWriter writer = new FileWriter(config)) {
                    InputStream configStream = this.getResource("config.json");
                    if(configStream == null) {
                        this.logger.error("Failed to save default config file.");
                    } else {
                        writer.write(new BufferedReader(
                                new InputStreamReader(configStream)).lines().collect(Collectors.joining("\n"))
                        ); writer.close();
                        
                        this.logger.info("Saved default config file.");
                    }
                }
            }

            // Put the configuration into an instance of the config class.
        } catch (IOException exception) {
            this.logger.error("Failed to create config file.", exception);
        }

        // Check if the groups file exists
        File groups = new File(this.getDataFolder(), "groups.json");

        try {
            if(!groups.exists()) {
                if(groups.createNewFile()) {
                    try(FileWriter writer = new FileWriter(groups)) {
                        InputStream groupStream = this.getResource("defaultGroups.json");
                        if(groupStream != null) {
                            writer.write(new BufferedReader(
                                    new InputStreamReader(groupStream)).lines().collect(Collectors.joining("\n"))
                            ); writer.close();

                            this.logger.info("Saved default groups file.");
                        } else {
                            this.logger.error("Failed to save default groups file.");
                        }
                    }
                } else {
                    this.logger.error("Failed to create failed default groups file.");
                }
            }
        } catch (IOException exception) {
            this.logger.error("Failed to create default groups file.", exception);
        }
    }

    /**
     * This method is called before the servers are started, or when the plugin enables.
     */
    @Override public void onEnable() {
        // Register event listeners.
        Grasscutter.getLogger().info("GCPM Enabled.");
        Grasscutter.setPermissionHandler(new GCPMPermissionHandler());
        getServer().getCommandMap().registerCommand("gcpm", new GCPMCommand());
        try {
            loadGroups();
        } catch (FileNotFoundException e) {
            Grasscutter.getLogger().error("Unable to load GCPMGroups file. ", e);
        }

        try {
            loadConfig();
        } catch (FileNotFoundException e) {
            Grasscutter.getLogger().error("Unable to load GCPMConfig file. ", e);
        }
    }

    /**
     * This method is called when the plugin is disabled.
     */
    @Override public void onDisable() {
        Grasscutter.getLogger().info("GCPM Disabled.");
        Grasscutter.setPermissionHandler(new DefaultPermissionHandler());
        getServer().getCommandMap().unregisterCommand("gcpm");
        this.Groups.clear();
    }

    /**
     * Gets the plugin's configuration.
     * @return A plugin config instance.
     */
    public PluginConfig getConfiguration() {
        return this.configuration;
    }

    public HashMap<String, GCPMGroup> getGroups() {
        return this.Groups;
    }

    public void loadConfig() throws FileNotFoundException {
        this.configuration = Grasscutter.getGsonFactory().fromJson(new FileReader(new File(this.getDataFolder(), "config.json")), PluginConfig.class);
    }

    public boolean loadGroups() throws FileNotFoundException {
        InputStream is = new FileInputStream(new File(this.getDataFolder(), "groups.json"));
        InputStreamReader fileReader = new InputStreamReader(is);

        List<GCPMGroup> groups = (List)Grasscutter.getGsonFactory().fromJson(fileReader, TypeToken.getParameterized(Collection.class, new Type[]{GCPMGroup.class}).getType());
        if(groups.size() > 0) {
            this.Groups.clear();
            for (GCPMGroup group : groups) {
                this.Groups.put(group.getName(), group);
            }
            Grasscutter.getLogger().info("Groups successfully loaded.");
            return true;
        } else {
            Grasscutter.getLogger().error("Unable to load groups. Groups size is 0.");
            return false;
        }
    }
}
