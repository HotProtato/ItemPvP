package net.swordvale.hotprotato.listener;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerJoinListener implements Listener {
    private ItemPvP plugin;

    public PlayerJoinListener(ItemPvP plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.loadKillstreak(p.getUniqueId());
        PlayerInventory inv = p.getInventory();
        if (!hasWeapon(p)){
            inv.setItem(plugin.getConfig().getInt("weapon-slot"), plugin.getWeapon());
            String msg = plugin.getConfig().getString("messages.msg-on-weapon-receive");
            if (plugin.isNewbie(p.getUniqueId())){
                plugin.removeNewbie(p.getUniqueId());
                if (!plugin.getConfig().getBoolean("send-msg-on-weapon-receieve-for-newbies")) return;
            }
            if (msg.equalsIgnoreCase("none")) return;
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else {
            if(Utils.equals(p.getItemInHand(), plugin.getWeapon())){
                if(plugin.getConfig().getBoolean("change-item-slot-on-join")) {
                    inv.setHeldItemSlot(plugin.getConfig().getInt("change-slot-to"));
                    if(plugin.hasPreviousArmor(p.getUniqueId()) && plugin.getConfig().getBoolean("restore-armor")){
                        plugin.restoreArmor(p);
                    } else {
                        inv.setArmorContents(new ItemStack[4]);
                    }
                } else {
                    plugin.setArmor(p);
                }
            }
        }
    }

    private boolean hasWeapon(Player p){
        for(ItemStack is : p.getInventory().getContents()){
            if(Utils.equals(is, plugin.getWeapon())) return true;
        }
        return false;
    }
}
