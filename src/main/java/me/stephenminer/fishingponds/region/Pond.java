package me.stephenminer.fishingponds.region;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Pond {
    private final FishingPonds plugin;
    private final Region region;
    private final HashMap<ItemStack, PondDrop> potentialDrops;

    private Set<PondDrop> fishDrops;
    private Set<PondDrop> treasureDrops;
    private Set<PondDrop> junkDrops;



    public Pond (Region region){
        this.plugin = FishingPonds.getPlugin(FishingPonds.class);
        this.region = region;
        this.potentialDrops = new HashMap<>();
        fishDrops = new HashSet<>();
        treasureDrops = new HashSet<>();
        junkDrops = new HashSet<>();
    }





    public void loadPotentialDrops(){
        potentialDrops.clear();
        String path = "regions." + region.getId() + ".pond.drops";
        if (!plugin.regionFile.getConfig().contains(path)) return;
        Set<String> itemEntries = plugin.regionFile.getConfig().getConfigurationSection(path).getKeys(false);
        for (String entry : itemEntries){
            try{
                ItemStack item = plugin.regionFile.getConfig().getItemStack(path + "." + entry + ".item");
                int weight = plugin.regionFile.getConfig().getInt(path + "." + entry + ".chance");
                if (weight < 0) weight = 50;
                DropType type = DropType.valueOf(plugin.regionFile.getConfig().getString(path + "." + entry + ".type"));
                addDrop(item, weight, type);
            }catch (Exception e){
                plugin.getLogger().warning("Something went wrong loading item with name " + entry + " for region " + region.getId());
            }
        }
    }

    public void save(){
        String path = "regions." + region.getId() + ".pond.drops";
        Set<String> existingEntries = null;
        if (plugin.regionFile.getConfig().contains(path)) {
            existingEntries = plugin.regionFile.getConfig().getConfigurationSection(path).getKeys(false);
        }
        for (PondDrop drop : potentialDrops.values()){
            ItemStack item = drop.item();
            String itemName = item.getItemMeta().getDisplayName();
            if (itemName.isEmpty()) {
                itemName = item.getType().name().toLowerCase().replace('_',' ');
            }
           // if (exexistingEntries.contains(itemName)) continue;
            plugin.regionFile.getConfig().set(path + "." + itemName + ".item",item);
            int weight = drop.weight();
            plugin.regionFile.getConfig().set(path + "." + itemName + ".chance",weight);
            DropType type = drop.type();
            plugin.regionFile.getConfig().set(path + "." + itemName + ".type",type.name());
        }
        plugin.regionFile.saveConfig();
    }




    public void addDrop(ItemStack item, int weight, DropType type){
        if (item == null) return;
        PondDrop drop = new PondDrop(item, weight, type);
        potentialDrops.put(item,drop);
        switch (type){
            case FISH:
                fishDrops.add(drop);
                break;
            case JUNK:
                junkDrops.add(drop);
                break;
            case TREASURE:
                treasureDrops.add(drop);
                break;
        }
    }

    public void removeDrop(ItemStack item){
        PondDrop drop = null;
        for (PondDrop entry : potentialDrops.values()){
            if (entry.item().equals(item)){
                drop = entry;
                break;
            }
        }
        if (drop == null) return;
        potentialDrops.remove(drop.item());
        treasureDrops.remove(drop);
        junkDrops.remove(drop);
        fishDrops.remove(drop);
        String itemName = item.getItemMeta().getDisplayName();
        String path = "regions." + region.getId() + ".pond.drops";
        if (plugin.regionFile.getConfig().contains(path + "." + itemName)){
            plugin.regionFile.getConfig().set(path + "." + itemName,null);
            plugin.regionFile.saveConfig();
        }
    }





    public Region getRegion(){ return region; }
    public HashMap<ItemStack,PondDrop> getPotentialDrops(){ return potentialDrops; }

    public Set<PondDrop> fishDrops(){ return fishDrops; }
    public Set<PondDrop> treasureDrops(){ return treasureDrops; }
    public Set<PondDrop> junkDrops(){ return junkDrops;}

}
