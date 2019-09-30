package net.swordvale.hotprotato.listener;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityDamageListener implements Listener {
    private ItemPvP plugin;

    public EntityDamageListener(ItemPvP plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player damaged = (Player) e.getEntity();
                if ((damaged.getHealth() - e.getDamage()) <= 0){
                    if(plugin.getConfig().getBoolean("respawn-insteadof-death")) {
                        e.setCancelled(true);
                        damaged.teleport(damaged.getWorld().getSpawnLocation());
                        damaged.setHealth(20);
                    }
                    plugin.setKillstreak(damager.getUniqueId(), plugin.getKillstreak(damager.getUniqueId())+1);
                    plugin.setKillstreak(damaged.getUniqueId(), 0);
                    if(plugin.hasTopKillstreak(damaged.getUniqueId())){
                        plugin.findTopKillstreaks();
                        if(plugin.getLeaderboards() != null){
                            plugin.updateLeaderboards();
                        }
                    }
                    if(plugin.getTopKillstreaks().size() < plugin.getConfig().getInt("max-leaderboard-entries")){
                        plugin.addTopKillstreak(damager.getUniqueId());
                        plugin.calcuateLowest();
                        if(plugin.getLeaderboards() != null){
                            plugin.updateLeaderboards();
                        }

                    } else if(!plugin.hasTopKillstreak(damager.getUniqueId())){
                        if(plugin.getKillstreak(damager.getUniqueId()) > plugin.getLowestTopKillstreak().getValue()){
                            plugin.addTopKillstreak(damager.getUniqueId());
                            plugin.removeSmallestTopKillstreak();
                            plugin.calcuateLowest();
                            if(plugin.getLeaderboards() != null){
                                plugin.updateLeaderboards();
                            }
                        }
                    } else {
                        plugin.updateTopKillstreaks();
                        if(plugin.getLeaderboards() != null){
                            plugin.updateLeaderboards();
                        }
                    }
                    plugin.removeCooldown(damaged.getUniqueId());
                    String msg = plugin.getConfig().getString("messages.msg-on-kill");
                    if(!msg.equalsIgnoreCase("none")){
                        damager.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("{killstreak}", ""+plugin.getKillstreak(damager.getUniqueId()))));
                    }
                    return;
                } else if(plugin.getConfig().getBoolean("ignore-npcs") && (damaged.hasMetadata("NPC") || containsIllegals(damaged.getName()))){
                    return;
                } else if(!Utils.equals(damager.getItemInHand(), plugin.getWeapon()) && plugin.getConfig().getBoolean("weapon-required-to-damage-players")) {
                    e.setCancelled(true);
                    return;
                } else if (Utils.equals(damager.getItemInHand(), plugin.getWeapon())){
                    if(plugin.getConfig().getBoolean("reset-cooldown-if-damaged")){
                        plugin.addCooldown(damaged.getUniqueId());
                    }
                    if(plugin.getConfig().getBoolean("reset-cooldown-if-hit")){
                        plugin.addCooldown(damager.getUniqueId());
                    }
                }
                if (plugin.getConfig().getBoolean("weapon-required-to-be-hit")){
                    String msg = plugin.getConfig().getString("messages.player-missing-weapon");
                    e.setCancelled(true);
                    if (!msg.equalsIgnoreCase("none")){
                        damager.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    }
                }
            }
        }

    private boolean containsIllegals(String str) {
        Pattern pattern = Pattern.compile("[&~#@*+%{}<>\\[\\]|\"^]");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
