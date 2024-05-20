package me.stephenminer.fishingponds.items;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.entity.Fish;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * A Class for writing and reading inventories from a String
 * I'm not sure if this will work when Minecraft moves on from NBT data
 */
public class InventoryManager {
    private final FishingPonds plugin;
    public InventoryManager(){
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
    }

    public void writeInventory(ItemStack item, Inventory inv)  {
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String data = getAsBase64(inv);
        container.set(Backpack.INVENTORY_KEY, PersistentDataType.STRING, data);
        item.setItemMeta(meta);
    }


    private String getAsBase64(Inventory inv){
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(outStream);
            for (int i = 0; i < inv.getSize(); i++){
                bukkitStream.writeInt(i);
                bukkitStream.writeObject(inv.getItem(i));
            }
            bukkitStream.close();
            String encoded = Base64.getEncoder().encodeToString(outStream.toByteArray());
;           return encoded;
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("Unable to write inventory!");
        }
    }

    /**
     *
     * @param inv the inventory to fill
     * @param str Base64 string written with an inventory in it
     */
    private void fillFromBase64(Inventory inv, String str) {
        byte[] decoded = Base64.getDecoder().decode(str);
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(decoded);
            BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(inStream);
            for (int i = 0; i < inv.getSize(); i++){
                try {
                    int slot = bukkitStream.readInt();
                    Object obj = bukkitStream.readObject();
                    if (obj != null){
                        ItemStack item = (ItemStack) obj;
                        inv.setItem(slot, item);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    plugin.getLogger().warning("Error loading an item for a BAG");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("Unable to load inventory!");
        }
    }

    public void loadInventory(ItemStack item, Inventory inv){
        if (!item.hasItemMeta()) return;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(Backpack.INVENTORY_KEY,PersistentDataType.STRING)){
            String encodedString = container.get(Backpack.INVENTORY_KEY,PersistentDataType.STRING);
            fillFromBase64(inv, encodedString);
        }
    }
}
