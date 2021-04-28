package me.flamingosaurus.spigottestplugin;

import me.flamingosaurus.spigottestplugin.events.ChestTest;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpigotTestPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        //System.out.println("Hello world!");
        getServer().getPluginManager().registerEvents(new ChestTest(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //System.out.println("Goodbye world!");
    }
}
