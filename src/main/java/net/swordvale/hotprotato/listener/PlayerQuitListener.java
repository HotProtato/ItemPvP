package net.swordvale.hotprotato.listener;

import net.swordvale.hotprotato.ItemPvP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private ItemPvP plugin;

    public PlayerQuitListener(ItemPvP plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if(plugin.getConfig().getBoolean("reset-killstreak-on-cooldown-logout") && plugin.hasCooldown(p.getUniqueId()) && System.currentTimeMillis() <= plugin.getCooldown(p.getUniqueId())){
            plugin.setKillstreak(p.getUniqueId(), 0);
            if(plugin.hasTopKillstreak(p.getUniqueId())){
                plugin.findTopKillstreaks();
                if(plugin.getLeaderboards() != null){
                    plugin.updateLeaderboards();
                }
            }
        }
        if(plugin.getConfig().getBoolean("remove-cooldown-on-logout")) {
            plugin.removeCooldown(p.getUniqueId());
        }
        plugin.saveKillstreak(e.getPlayer().getUniqueId());
    }
}
