package net.swordvale.hotprotato.command;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.command.subcommands.DeleteLeaderboards;
import net.swordvale.hotprotato.command.subcommands.Leaderboards;
import net.swordvale.hotprotato.command.subcommands.SetLeaderboards;
import net.swordvale.hotprotato.command.subcommands.TPLeaderboards;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemPvPCommand implements CommandExecutor {
    private final ItemPvP plugin;
    private final List<SubCommand> subCmds = new ArrayList<>();

    public ItemPvPCommand(ItemPvP plugin){
        this.plugin = plugin;
        subCmds.add(new SetLeaderboards(plugin));
        subCmds.add(new TPLeaderboards(plugin));
        subCmds.add(new DeleteLeaderboards(plugin));
        subCmds.add(new Leaderboards(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("itempvp")){
            if(args.length >= 1){
            for (SubCommand sub : subCmds) {
                if (isAlias(sub.getAliases(), args[0])) {
                    if (sub.isPlayerOnly() && !(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Player required.");
                        return true;
                    } else if (sub.getPermissions().isEmpty() || sub.getPermissions().stream().anyMatch(sender::hasPermission)) {
                        sub.run(sender, args);
                    } else {
                        sender.sendMessage(plugin.getConfig().getString("messages.no-permission"));
                    }
                    return true;
                }
            }
            } else {
                sender.sendMessage("/itempvp deleteleaderboards");
                sender.sendMessage("/itempvp setleaderboards");
                sender.sendMessage("/itempvp tpleaderboards");
            }
        }
        return false;
    }

    public boolean isAlias(List<String> aliases, String arg){
        for(String str : aliases){
            if(arg.equalsIgnoreCase(str)){
                return true;
            }
        }
        return false;
    }
}
