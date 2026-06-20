package com.diasmp.diaswords;

import com.diasmp.diaswords.listeners.AbilityListener;
import com.diasmp.diaswords.managers.CooldownManager;
import com.diasmp.diaswords.managers.SwordManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entry point for DiaSwords.
 * Custom ability swords for DiaSMP's FFA server.
 */
public class DiaSwords extends JavaPlugin {

    private SwordManager swordManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.swordManager = new SwordManager(this);
        this.cooldownManager = new CooldownManager();

        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);

        DiaSwordsCommand commandExecutor = new DiaSwordsCommand(this);
        getCommand("diaswords").setExecutor(commandExecutor);
        getCommand("diaswords").setTabCompleter(commandExecutor);

        getLogger().info("DiaSwords enabled — " + SwordType.values().length + " sword types loaded.");
    }

    @Override
    public void onDisable() {
        if (cooldownManager != null) {
            cooldownManager.clearAll();
        }
        getLogger().info("DiaSwords disabled.");
    }

    public SwordManager getSwordManager() {
        return swordManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
