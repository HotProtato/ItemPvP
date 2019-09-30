package net.swordvale.hotprotato.listener;

import net.swordvale.hotprotato.ItemPvP;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PreJoinListener implements Listener {
    private ItemPvP plugin;

    public PreJoinListener(ItemPvP plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e){
        if(!Bukkit.getOfflinePlayer(e.getUniqueId()).hasPlayedBefore()){
            plugin.addNewbie(e.getUniqueId());
        }
    }
}
