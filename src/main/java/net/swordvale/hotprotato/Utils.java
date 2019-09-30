package net.swordvale.hotprotato;

import org.bukkit.inventory.ItemStack;

public class Utils {
    public static boolean equals(ItemStack a, ItemStack b){
        if(a == null || b == null)
            return false;
        if(a.getType() != b.getType())
            return false;
        if(a.hasItemMeta() != b.hasItemMeta())
            return false;
        if(a.hasItemMeta() && !a.getItemMeta().equals(b.getItemMeta()))
            return false;
        return true;
    }
}
