package me.stephenminer.fishingponds.items;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaitItem {
    public static NamespacedKey BAIT_TAG = new NamespacedKey(JavaPlugin.getPlugin(FishingPonds.class),"fp_bait");
    private final String id;
    private final FishingPonds plugin;
    private ItemStack item;

    public BaitItem(String id){
        this.id = id;
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
;    }


    public ItemStack loadItem(){
        String path = "bait." + id;
        String name = plugin.baitFile.getConfig().getString(path + ".name");
        Material mat = Material.matchMaterial(plugin.baitFile.getConfig().getString(path + ".material"));
        List<String> tempList = plugin.baitFile.getConfig().getStringList(path + ".lore");
        List<String> lore = new ArrayList<>();
        tempList.forEach(str->lore.add(ChatColor.translateAlternateColorCodes('&',str)));
        if (plugin.baitFile.getConfig().contains(path + ".uses")){
            int uses = plugin.baitFile.getConfig().getInt(path + ".uses");
            lore.add(ChatColor.YELLOW + "Uses: " + uses);
        }
        lore.add(ChatColor.BLACK + "fp:" + id);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        makeUnstackable(item);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin,"rand-id"),PersistentDataType.STRING, UUID.randomUUID().toString());
        return item;
    }


    private void makeUnstackable(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(BaitItem.BAIT_TAG, PersistentDataType.STRING, UUID.randomUUID().toString());
        item.setItemMeta(meta);
    }

}
