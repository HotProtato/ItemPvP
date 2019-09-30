package net.swordvale.hotprotato;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.swordvale.hotprotato.listener.*;
import net.swordvale.hotprotato.command.ItemPvPCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ItemPvP extends JavaPlugin{
    private List<ItemStack> armor = new ArrayList<>();
    private Map<UUID, List<ItemStack>> previousArmor = new HashMap<>();
    private ItemStack weapon;
    private List<UUID> newbies = new ArrayList<>();
    private Map<UUID, Long> timeToScroll = new HashMap<>();
    private File dataFile;
    private YamlConfiguration data;
    private Map<UUID, Integer> killStreaks = new HashMap<>();
    private List<Map.Entry<UUID, Integer>> topKillstreaks = new ArrayList<>();
    private Map.Entry<UUID, Integer> lowestTopKillstreak;
    private boolean holograms = false;
    private Hologram leaderboards;

    @Override
    public void onEnable(){
        saveDefaultConfig();
        dataFile = new File(this.getDataFolder(), "data.yml");
        if(!dataFile.exists()){
            try {
                //noinspection ResultOfMethodCallIgnored
                dataFile.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        if(!data.isSet("leaderboards") || data.getConfigurationSection("leaderboards").getKeys(false).size() < getConfig().getInt("max-leaderboard-entries")){
            findTopKillstreaks();
        } else {
            for(String str : data.getConfigurationSection("leaderboards").getKeys(false)){
                topKillstreaks.add(new TempEntry<>(UUID.fromString(str), data.getInt("leaderboards." + str)));
            }
        }
        if(getServer().getPluginManager().isPluginEnabled("HolographicDisplays")){
            holograms = true;
            if(data.isSet("location")){
                createHologram(getLocationFromString(data.getString("location")));
            }
        }
        if(!getConfig().isSet("armor")){
            ItemStack diaHelm = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta meta = diaHelm.getItemMeta();
            meta.setLore(Collections.singletonList("A really cool helmet"));
            meta.setDisplayName(("Cool helmet"));
            diaHelm.setItemMeta(meta);
            diaHelm.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            getConfig().set("armor.helmet", diaHelm);
            getConfig().set("armor.chestplate", new ItemStack(Material.DIAMOND_CHESTPLATE));
            getConfig().set("armor.leggings", new ItemStack(Material.DIAMOND_LEGGINGS));
            getConfig().set("armor.boots", new ItemStack(Material.DIAMOND_BOOTS));
            saveConfig();
        }
        if(!getConfig().isSet("weapon")){
            getConfig().set("weapon", new ItemStack(Material.DIAMOND_SWORD));
            saveConfig();
        }

        armor.add(getConfig().getItemStack("armor.helmet"));
        armor.add(getConfig().getItemStack("armor.chestplate"));
        armor.add(getConfig().getItemStack("armor.leggings"));
        armor.add(getConfig().getItemStack("armor.boots"));
        weapon = getConfig().getItemStack("weapon");
        if(getConfig().getBoolean("restore-durability-on-hit")) {
            ItemMeta meta = weapon.getItemMeta();
            meta.spigot().setUnbreakable(true);
            weapon.setItemMeta(meta);
        }

        getServer().getPluginManager().registerEvents(new ItemScrollListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getCommand("itempvp").setExecutor(new ItemPvPCommand(this));
        if(getConfig().getBoolean("weapon-on-join")){
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        }
        if(!getConfig().getBoolean("send-msg-on-weapon-receieve-for-newbies")){
            getServer().getPluginManager().registerEvents(new PreJoinListener(this), this);
        }
        if(getConfig().getBoolean("restore-armor-on-leave")){
            getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        }
        getServer().getPluginManager().registerEvents(new ItemClickListener(this), this);
        if(getConfig().getBoolean("prevent-item-drops")){
            getServer().getPluginManager().registerEvents(new ItemDropListener(), this);
        }
        if(getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")){
            PlaceholderAPI.registerPlaceholder(this, "itempvp_time_left", new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent event){
                    String time = getTimeLeft(event.getPlayer().getUniqueId());
                    if(!isInteger(time) || Integer.parseInt(time) == 0){
                        return getConfig().getString("messages.time-left-placeholder-if-0");
                    }
                    return time;
                }
            });
            PlaceholderAPI.registerPlaceholder(this, "itempvp_killstreak", event -> getKillstreak(event.getPlayer().getUniqueId()) + "");
        }
    }

    @Override
    public void onDisable(){
        if(!getConfig().getBoolean("restore-armor-on-leave")){
            for(UUID uuid : killStreaks.keySet()){
                data.set(uuid + ".killstreak", killStreaks.get(uuid));
            }
        }
        for(UUID uuid : previousArmor.keySet()){
            if(previousArmor.containsKey(uuid)){
                if(getConfig().getBoolean("restore-armor-on-leave")) {
                    data.set(uuid + ".helmet", previousArmor.get(uuid).get(0));
                    data.set(uuid + ".chestplate", previousArmor.get(uuid).get(1));
                    data.set(uuid + ".leggings", previousArmor.get(uuid).get(2));
                    data.set(uuid + ".boots", previousArmor.get(uuid).get(3));
                } else {
                    data.set(uuid + ".helmet", new ItemStack(Material.AIR));
                    data.set(uuid + ".chestplate", new ItemStack(Material.AIR));
                    data.set(uuid + ".leggings", new ItemStack(Material.AIR));
                    data.set(uuid + ".boots", new ItemStack(Material.AIR));
                }
            }
        }
        for(Map.Entry<UUID, Integer> e : topKillstreaks){
            data.set("leaderboards." + e.getKey(), e.getValue());
        }
        try {
            data.save(dataFile);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public ItemStack getWeapon(){
        return weapon;
    }

    public void setArmor(Player p){
        setArmor(armor, p.getInventory());
    }

    public void restoreArmor(Player p){
        if(previousArmor.containsKey(p.getUniqueId())){
            setArmor(previousArmor.get(p.getUniqueId()), p.getInventory());
            previousArmor.remove(p.getUniqueId());
        } else if (data.isSet(p.getUniqueId() + ".helmet")) {
            PlayerInventory inv = p.getInventory();
            inv.setHelmet(data.getItemStack(p.getUniqueId() + ".helmet"));
            inv.setChestplate(data.getItemStack(p.getUniqueId() + ".chestplate"));
            inv.setLeggings(data.getItemStack(p.getUniqueId() + ".leggings"));
            inv.setBoots(data.getItemStack(p.getUniqueId() + ".boots"));
            data.set(p.getUniqueId() + ".helmet", null);
            data.set(p.getUniqueId() + ".chestplate", null);
            data.set(p.getUniqueId() + ".leggings", null);
            data.set(p.getUniqueId() + ".boots", null);
            try {
                data.save(dataFile);
            } catch(IOException e){
                e.printStackTrace();
            }

        }
    }

    private void setArmor(List<ItemStack> armor, PlayerInventory inv){
        inv.setHelmet(armor.get(0));
        inv.setChestplate(armor.get(1));
        inv.setLeggings(armor.get(2));
        inv.setBoots(armor.get(3));
    }

    public void addNewbie(UUID uuid){
        newbies.add(uuid);
    }

    public void removeNewbie(UUID uuid){
        newbies.remove(uuid);
    }

    public boolean isNewbie(UUID uuid){
        return newbies.contains(uuid);
    }

    public boolean hasPreviousArmor(UUID uuid){
            return previousArmor.containsKey(uuid);
    }

    public void removePreviousArmor(UUID uuid){
        previousArmor.remove(uuid);
    }

    public void addPreviousArmor(UUID uuid, List<ItemStack> armor){
        previousArmor.put(uuid, armor);
    }

    public Long getCooldown(UUID uuid){
        return timeToScroll.get(uuid);
    }

    public void addCooldown(UUID uuid){
        timeToScroll.put(uuid, System.currentTimeMillis() + (1000*getConfig().getInt("cooldown")));
    }

    public String getTimeLeft(UUID uuid){
        if(!timeToScroll.containsKey(uuid)){
            return "0";
        } else if(timeToScroll.get(uuid) - System.currentTimeMillis() <= 0){
            timeToScroll.remove(uuid);
            return "0";
        }
        long timeMillis = timeToScroll.get(uuid);
        return TimeUnit.MILLISECONDS.toSeconds(timeMillis - System.currentTimeMillis()) + "";
    }

    public boolean hasCooldown(UUID uuid){
        return timeToScroll.containsKey(uuid);
    }

    public void removeCooldown(UUID uuid){
        timeToScroll.remove(uuid);
    }

    public int getKillstreak(UUID uuid){
        if(killStreaks.containsKey(uuid)) {
            return killStreaks.get(uuid);
        }
        if(getConfig().isSet(uuid + ".killstreak")){
            return getConfig().getInt(uuid + ".killstreak");
        }
        return 0;
    }

    public void setKillstreak(UUID uuid, int killstreak){
        killStreaks.put(uuid, killstreak);
        data.set(uuid + ".killstreak", 0);
        try {
            data.save(dataFile);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void saveKillstreak(UUID uuid){
        if(killStreaks.containsKey(uuid)){
            data.set(uuid + ".killstreak", killStreaks.get(uuid));
            killStreaks.remove(uuid);
        }
        try {
            data.save(dataFile);
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    private List<Map.Entry<UUID, Integer>> findGreatest(Map<UUID, Integer> map, int n) {
        Map<UUID, Integer> tempMap = new HashMap<>(map);
        List<Map.Entry<UUID, Integer>> toReturn = new ArrayList<>();
        for(int i = 0; i < n; i++){
            Map.Entry<UUID, Integer> en = findHighestEntry(tempMap);
            if(en == null){
                return toReturn;
            }
            tempMap.remove(en.getKey());
            toReturn.add(en);
        }
        return toReturn;
    }

    private Map.Entry<UUID, Integer> findHighestEntry(Map<UUID, Integer> map){
        Map.Entry<UUID, Integer> currentHighest = null;
        for(Map.Entry<UUID, Integer> en : map.entrySet()){
            if(currentHighest == null){
                currentHighest = en;
                continue;
            }
            if(en.getValue() > currentHighest.getValue()){
                currentHighest = en;
            }
        }
        return currentHighest;
    }

    public List<Map.Entry<UUID, Integer>> getTopKillstreaks(){
        return topKillstreaks;
    }

    public void addTopKillstreak(UUID uuid){
        Map.Entry<UUID, Integer> e = new TempEntry<>(uuid, getKillstreak(uuid));
        topKillstreaks.add(e);
    }

    public void removeSmallestTopKillstreak(){
        topKillstreaks.remove(topKillstreaks.size()-1);
    }

    public Map.Entry<UUID, Integer> getLowestTopKillstreak(){
        return lowestTopKillstreak;
    }

    public void calcuateLowest(){
        lowestTopKillstreak = topKillstreaks.get(topKillstreaks.size()-1);
    }

    public void findTopKillstreaks(){
        Map<UUID, Integer> map = new HashMap<>();
        if(topKillstreaks.isEmpty()) {
            for(String str : data.getConfigurationSection("leaderboards").getKeys(false)){
                topKillstreaks.add(new TempEntry<>(UUID.fromString(str), data.getInt("leaderboards." + str)));
            }
        }
        for(Map.Entry<UUID, Integer> e : topKillstreaks){
            if(map.containsKey(e.getKey())){
                map.remove(e.getKey());
            }
        }
        map.putAll(topKillstreaks.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        for(Map.Entry<UUID, Integer> e : killStreaks.entrySet()){
            if(map.containsKey(e.getKey())){
                map.remove(e.getKey());
            }
        }
        map.putAll(killStreaks);
        List<UUID> toRemove = new ArrayList<>();
        for(Map.Entry<UUID, Integer> e : map.entrySet()){
            if(e.getValue()==0){
                toRemove.add(e.getKey());
            }
        }
        for(UUID uuid : toRemove){
            map.remove(uuid);
        }
        int x = map.size();
        if(getConfig().getInt("max-leaderboard-entries") < x){
            x = getConfig().getInt("max-leaderboard-entries");
        }
        topKillstreaks = findGreatest(map, x);
    }

    public void updateTopKillstreaks(){
        Map<UUID, Integer> top = new HashMap<>(topKillstreaks.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        List<UUID> toRemove = new ArrayList<>();
        for(Map.Entry<UUID, Integer> e : top.entrySet()){
            if(e.getValue()==0){
                toRemove.add(e.getKey());
            }
        }
        for(UUID uuid : toRemove){
            top.remove(uuid);
        }
        topKillstreaks = findGreatest(top, getConfig().getInt("max-leaderboard-entries"));
    }

    public void loadKillstreak(UUID uuid){
        if(!killStreaks.containsKey(uuid)){
            killStreaks.put(uuid, data.getInt(uuid + ".killstreak"));
            data.set(uuid + ".killstreak", null);
            try {
                data.save(dataFile);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public boolean hasTopKillstreak(UUID uuid){
        for(Map.Entry<UUID, Integer> e : topKillstreaks){
            if(e.getKey().equals(uuid)){
                return true;
            }
        }
        return false;
    }

    public boolean hasHolograms(){
        return holograms;
    }

    class TempEntry<K, V> implements Map.Entry<K, V>{
        private final K key;
        private V value;

        TempEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }

    public void createHologram(Location loc){
        leaderboards = HologramsAPI.createHologram(this, loc);
        leaderboards.getVisibilityManager().setVisibleByDefault(true);
        updateLeaderboards();
        data.set("location", loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch());
    }

    public synchronized void updateLeaderboards(){
        leaderboards.clearLines();
        leaderboards.appendTextLine(ChatColor.translateAlternateColorCodes('&', getConfig().getString("leaderboards-title")));
        for(Map.Entry<UUID, Integer> e : topKillstreaks){
            String format = getConfig().getString("leaderboards-entry-format");
            format = format.replace("{player}", getServer().getOfflinePlayer(e.getKey()).getName());
            if(e.getValue() == null || e.getValue() < 1){
                format = format.replace("{killstreak}", 0 + "");
            } else {
                format = format.replace("{killstreak}", e.getValue() + "");
            }
            leaderboards.appendTextLine(ChatColor.translateAlternateColorCodes('&', format));
        }
    }

    public Hologram getLeaderboards(){
        return leaderboards;
    }

    public void deleteLeaderboards(){
        data.set("location", null);
        if(leaderboards != null && !leaderboards.isDeleted()){
            leaderboards.delete();
        }
    }

    public void teleportLeaderboards(Location loc){
        leaderboards.teleport(loc);
        data.set("location", loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch());
    }

    private Location getLocationFromString(String s) {
        if (s == null || s.trim().equals("")){
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 6) {
            World w = getServer().getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(w, x, y, z, yaw, pitch);
        }
        return null;
    }

    private boolean isInteger(String str){
        try{
            Integer.parseInt(str);
        } catch(NumberFormatException e){
            return false;
        }
        return true;
    }
}