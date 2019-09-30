package net.swordvale.hotprotato.listener;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class ItemScrollListener implements Listener {
    private ItemPvP plugin;

    public ItemScrollListener(ItemPvP plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        if(inv.getItem(e.getNewSlot()) != null && Utils.equals(inv.getItem(e.getNewSlot()), plugin.getWeapon())){
            if(inv.getItem(e.getPreviousSlot()) != null && Utils.equals(inv.getItem(e.getPreviousSlot()), plugin.getWeapon())){
                return;
            }
            List<ItemStack> armor = new ArrayList<>();
            armor.add(inv.getHelmet());
            armor.add(inv.getChestplate());
            armor.add(inv.getLeggings());
            armor.add(inv.getBoots());
            for(ItemStack is : armor){
                if(is != null && is.getType() != Material.AIR){
                    plugin.addPreviousArmor(p.getUniqueId(), armor);
                    break;
                }
            }
            plugin.setArmor(e.getPlayer());
            if(plugin.getConfig().getInt("cooldown") > 0 && plugin.getConfig().getBoolean("start-cooldown-instantly")){
                plugin.addCooldown(p.getUniqueId());
            }
            String msg = plugin.getConfig().getString("messages.msg-on-weapon-equip");
            if(msg.equalsIgnoreCase("none")) return;
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else if(inv.getItem(e.getPreviousSlot()) != null && Utils.equals(inv.getItem(e.getPreviousSlot()), plugin.getWeapon())){
            if(plugin.hasCooldown(p.getUniqueId()) && System.currentTimeMillis() <= plugin.getCooldown(p.getUniqueId())){
                String msg = plugin.getConfig().getString("messages.cannot-bypass-cooldown");
                msg = msg.replace("{seconds_left}", plugin.getTimeLeft(p.getUniqueId()));
                e.setCancelled(true);
                if(msg.equalsIgnoreCase("none")) return;
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return;
            }
            plugin.removeCooldown(p.getUniqueId());
            if(plugin.getConfig().getBoolean("restore-armor") && plugin.hasPreviousArmor(e.getPlayer().getUniqueId())){
                plugin.restoreArmor(e.getPlayer());
                plugin.removePreviousArmor(p.getUniqueId());
                String msg = plugin.getConfig().getString("messages.msg-on-restore");
                if(msg.equalsIgnoreCase("none")) return;
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return;
            }
            inv.setArmorContents(new ItemStack[4]);
        }
    }
}
