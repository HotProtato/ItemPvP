package net.swordvale.hotprotato.command;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SubCommand {
    private final List<String> aliases = new ArrayList<>();
    private final List<String> permissions = new ArrayList<>();
    private boolean playerOnly = false;
    private String description = "null";
    protected Options options = new Options();

    protected SubCommand(String... aliases){
        this.aliases.addAll(Arrays.asList(aliases));
    }

    protected abstract void execute(CommandSender sender, String[] args);

    protected List<String> getAliases(){
        return aliases;
    }

    protected boolean isPlayerOnly(){
        return playerOnly;
    }

    protected List<String> getPermissions(){
        return permissions;
    }

    public void run(CommandSender sender, String[] args) {
        execute(sender, args);
    }

    protected final class Options {
        public Options playerOnly(boolean value) {
            SubCommand.this.playerOnly = value;
            return this;
        }

        public Options addPermissions(String... permissions) {
            SubCommand.this.permissions.addAll(Arrays.asList(permissions));
            return this;
        }

        public Options setDescription(String description) {
            SubCommand.this.description = description;
            return this;
        }
    }
}