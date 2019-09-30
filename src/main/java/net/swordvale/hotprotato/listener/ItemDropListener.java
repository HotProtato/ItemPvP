package net.swordvale.hotprotato.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e){
        if(e.getPlayer().hasPermission("itempvp.bypass")) return;
            e.setCancelled(true);
    }
}
