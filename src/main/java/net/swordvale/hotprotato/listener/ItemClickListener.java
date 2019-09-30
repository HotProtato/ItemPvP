package net.swordvale.hotprotato.listener;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ItemClickListener implements Listener {
    private ItemPvP plugin;

    public ItemClickListener(ItemPvP plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e){
        if(e.getWhoClicked().hasPermission("itempvp.bypass")) return;
        if(Utils.equals(e.getCurrentItem(), plugin.getWeapon())){
            e.setCancelled(true);
            return;
        }

        if(Utils.equals(e.getWhoClicked().getInventory().getItemInHand(), plugin.getWeapon())){
            e.setCancelled(true);
        }
    }
}
