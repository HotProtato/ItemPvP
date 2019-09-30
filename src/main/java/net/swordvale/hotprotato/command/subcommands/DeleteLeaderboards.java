package net.swordvale.hotprotato.command.subcommands;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DeleteLeaderboards extends SubCommand{
    private ItemPvP plugin;

    public DeleteLeaderboards(ItemPvP plugin){
        super("deleteleaderboards");
        options.addPermissions("itempvp.deleteleaderboards");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!plugin.hasHolograms()){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Holographic displays required!");
            return;
        } else if(plugin.getLeaderboards() == null){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "There is no hologram to delete!");
            return;
        } else if(plugin.getLeaderboards().isDeleted()){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "The hologram has already been deleted!");
            return;
        }
        plugin.deleteLeaderboards();
        sender.sendMessage(ChatColor.GREEN + "Leaderboards deleted!");
    }
}
