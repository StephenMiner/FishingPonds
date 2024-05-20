package me.stephenminer.fishingponds;

import me.stephenminer.fishingponds.commands.*;
import me.stephenminer.fishingponds.events.*;
import me.stephenminer.fishingponds.items.Backpack;
import me.stephenminer.fishingponds.items.Items;
import me.stephenminer.fishingponds.region.GoldenFishTimer;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.*;
import java.util.stream.Collectors;

public final class FishingPonds extends JavaPlugin {
    public ConfigFile regionFile;
    public ConfigFile fishFile;
    public ConfigFile baitFile;
    public ConfigFile bagFile;
    public ConfigFile settings;
    public GoldenFishTimer timer;
    @Override
    public void onEnable() {
        this.regionFile = new ConfigFile(this, "regions");
        this.fishFile = new ConfigFile(this, "fish");
        this.baitFile = new ConfigFile(this, "bait");
        this.bagFile = new ConfigFile(this, "bags");
        this.settings = new ConfigFile(this, "settings");
        registerCommands();
        registerEvents();
        loadRegions();
        this.timer = new GoldenFishTimer(this);
        this.timer.timer();
    }

    @Override
    public void onDisable() {
        saveRegions();
        saveBags();
    }

    private void saveRegions(){
        for (Region region : Region.regions){
            region.save();
        }
        Region.regions.clear();
    }
    private void saveBags(){
        for (Backpack bag : Backpack.BY_UUID.values()){
            bag.save();
        }
        Backpack.BY_UUID.clear();
    }


    private void registerCommands(){
        PondCmd pondCmd = new PondCmd();
        RedefinePond redefinePond = new RedefinePond(this);
        ShowBorder showBorder = new ShowBorder(this);
        DeletePond deletePond = new DeletePond(this);
        UnlockPond unlockPond = new UnlockPond(this);
        WarpPond warpPond = new WarpPond(this);
        PondSpawn pondSpawn = new PondSpawn(this);
        GiveBait giveBait = new GiveBait(this);
        GiveBag giveBag = new GiveBag(this);
        GoldFishCmd goldFishCmd = new GoldFishCmd(this);

        getCommand("giveBait").setExecutor(giveBait);
        getCommand("giveBait").setTabCompleter(giveBait);
        getCommand("giveBag").setExecutor(giveBag);
        getCommand("giveBag").setTabCompleter(giveBag);
        getCommand("pondSpawn").setExecutor(pondSpawn);
        getCommand("pondSpawn").setTabCompleter(pondSpawn);
        getCommand("pondWarp").setExecutor(warpPond);
        getCommand("pondWarp").setTabCompleter(warpPond);
        getCommand("unlockPond").setExecutor(unlockPond);
        getCommand("unlockPond").setTabCompleter(unlockPond);
        getCommand("deletePond").setExecutor(deletePond);
        getCommand("deletePond").setTabCompleter(deletePond);
        getCommand("pondBorder").setExecutor(showBorder);
        getCommand("pondBorder").setTabCompleter(showBorder);
        getCommand("pond").setExecutor(pondCmd);
        getCommand("pond").setTabCompleter(pondCmd);
        getCommand("resize-pond").setTabCompleter(redefinePond);
        getCommand("resize-pond").setExecutor(redefinePond);
        getCommand("pondWand").setExecutor(new GiveWand());
        getCommand("goldFish").setExecutor(goldFishCmd);
        getCommand("goldFish").setTabCompleter(goldFishCmd);
    }

    private void registerEvents(){
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new RegionSetUp(this), this);
        pm.registerEvents(new PondSetup(this), this);
        pm.registerEvents(new FishingEvents(),this);
        pm.registerEvents(new BagEvents(this), this);
        pm.registerEvents(new PondProtection(), this);
    }


    public String fromLoc(Location loc){
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public String fromBLoc(Location loc){
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }


    public Location fromString(String str){
        String[] contents = str.split(",");
        String sWorld = contents[0];
        try{
            World world = Bukkit.getWorld(sWorld);
            if (world == null){
                world = new WorldCreator(sWorld).createWorld();
            }
            double x = Double.parseDouble(contents[1]);
            double y = Double.parseDouble(contents[2]);
            double z = Double.parseDouble(contents[3]);
            if (contents.length >= 6) {
                float yaw = Float.parseFloat(contents[4]);
                float pitch = Float.parseFloat(contents[5]);
                return new Location( world, x,y,z,yaw,pitch);
            }
            return new Location( world, x,y, z);
        }catch (Exception e){
            this.getLogger().warning("Something went wrong loading a location from a string: " + str);
        }
        return null;
    }


    public boolean checkLore(ItemStack item, String check){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return check == null || check.isEmpty();
        List<String> lore = item.getItemMeta().getLore();
        for (String entry : lore){
            String temp = ChatColor.stripColor( entry);
            if (temp.equalsIgnoreCase(check)) return true;
        }
        return false;
    }

    public List<String> filter(Collection<String> base, String match){
        match = match.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = ChatColor.stripColor(entry).toLowerCase();
            if (temp.contains(match)) filtered.add(entry);
        }
        return filtered;
    }




    public void loadRegions(){
        Region.regions.clear();
        if (!this.regionFile.getConfig().contains("regions")) return;
        Set<String> regionIds = this.regionFile.getConfig().getConfigurationSection("regions").getKeys(false);
        for (String id : regionIds){
            Location loc1 = fromString(this.regionFile.getConfig().getString("regions." + id + ".loc1"));
            Location loc2 = fromString(this.regionFile.getConfig().getString("regions." + id + ".loc2"));
            Region region = new Region(id, loc1, loc2);
            if (this.regionFile.getConfig().contains("regions." + id + ".spawn")){
                Location spawn = fromString(this.regionFile.getConfig().getString("regions." + id + ".spawn"));
                region.setSpawn(spawn);
            }
            if (this.regionFile.getConfig().contains("regions." + id + ".unlocked")){
                Set<UUID> unlocked = this.regionFile.getConfig().getStringList("regions." + id + ".unlocked")
                        .stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
                region.setUnlocked(unlocked);
            }
            boolean needUnlock = this.regionFile.getConfig().getBoolean("regions." + id + ".need-unlock" );
            region.setNeedUnlock(needUnlock);
        }
    }

    public boolean fishEnabled(){
        return this.settings.getConfig().getBoolean("settings.disable-nat-fishing");
    }


    public ItemStack getGoldFish(){
        if (!this.settings.getConfig().contains("settings.golden-fish.item")){
            Items items = new Items();
            return items.goldenFish();
        }else{
            return this.settings.getConfig().getItemStack("settings.golden-fish.item");
        }
    }

    public int minTime(){ return this.settings.getConfig().getInt("settings.golden-fish.min-time"); }
    public int maxTime(){ return this.settings.getConfig().getInt("settings.golden-fish.max-time"); }

    public double getChance(){ return this.settings.getConfig().getDouble("settings.golden-fish.chance"); }
}
