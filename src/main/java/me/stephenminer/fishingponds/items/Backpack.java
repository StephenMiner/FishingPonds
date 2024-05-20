package me.stephenminer.fishingponds.items;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Backpack {
    public static NamespacedKey BAG_KEY = new NamespacedKey(JavaPlugin.getPlugin(FishingPonds.class),"fp_bag");
    public static NamespacedKey INVENTORY_KEY = new NamespacedKey(JavaPlugin.getPlugin(FishingPonds.class),"fp_inventory");
    public static HashMap<UUID, Backpack> BY_UUID = new HashMap<>();
    private final FishingPonds plugin;
    private final UUID uuid;
    private ItemStack item;
    private Size size;
    private BagType type;
    private Inventory inventory;
    private Player viewer;
    public Backpack(Size size, BagType type){
        this(type.color() + size.title() + type.title() + "Bag", Material.CHEST_MINECART,size, type, UUID.randomUUID());
    }

    /**
     * For use when loading object from file
     */
    public Backpack(Size size, BagType type, UUID uuid, ItemStack item){
        this(type.color() + size.title() + type.title() + "Bag", Material.CHEST_MINECART,size, type, uuid);
        this.item = item;
    }
    public Backpack(String name, Material mat, Size size, BagType type, UUID uuid){
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
        this.size = size;
        this.type = type;
        this.uuid = uuid;
        loadItem(name, mat);
        //loadInventory();
        Backpack.BY_UUID.put(uuid, this);
    }


    public void loadItem(String name, Material mat){
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(BAG_KEY, PersistentDataType.STRING, uuid.toString());
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Can hold " + type.title() + "items");
        lore.add(ChatColor.YELLOW + "Capacity: " + size.slots());
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LURE,1,false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        this.item = item;
    }

    public void loadInventory(){
        Inventory inv = Bukkit.createInventory(null, size.slots(), item.getItemMeta().getDisplayName());
/*
        if (plugin.bagFile.getConfig().contains("bags." + uuid + ".items")){
            String path = "bags." + uuid + ".items";
            Set<String> slots = plugin.bagFile.getConfig().getConfigurationSection(path).getKeys(false);
            for (String entry : slots){
                ItemStack item = plugin.bagFile.getConfig().getItemStack(path + "." + entry);
                int slot = Integer.parseInt(entry);
                inv.setItem(slot, item);
            }
        }


 */

        InventoryManager manager = new InventoryManager();
        manager.loadInventory(item,inv);
        this.inventory = inv;
    }

    public void resetInventory(){
        save();
        inventory = null;
    }



    public boolean canAdd(ItemStack add){
        ItemMeta meta = add.getItemMeta();
        if (meta.getPersistentDataContainer().has(BAG_KEY, PersistentDataType.STRING)) return false;
        if (type == BagType.NONE) {
            return true;
        }
        if (type == BagType.FISH) {
            return isFish(add);
        }
        if (type == BagType.BAIT) {
            return isBait(add);
        }
        return false;
    }

    public void save(){
        plugin.bagFile.getConfig().set("bags." + uuid + ".type",type.name());
        plugin.bagFile.getConfig().set("bags." + uuid + ".size",size.name());
        plugin.bagFile.getConfig().set("bags." + uuid + ".name", item.getItemMeta().getDisplayName());
        plugin.bagFile.saveConfig();
        if (inventory == null) return;
        /*
        ItemStack[] contents = inventory.getContents();
        String base = "bags." + uuid + ".items";
        plugin.bagFile.getConfig().set(base, null);
        for (int i = 0; i < contents.length; i++){
            ItemStack entry = contents[i];
            if (entry == null || entry.getType().isAir()) continue;
            plugin.bagFile.getConfig().set(base + "." + i, entry);
        }
        plugin.bagFile.saveConfig();



         */
        InventoryManager manager = new InventoryManager();
        manager.writeInventory(item, inventory);
    }

    public void delete(){
        plugin.bagFile.getConfig().set("bags." + uuid, null);
    }


    private boolean isFish(ItemStack item){
        return plugin.checkLore(item, "fp:fished");
    }

    private boolean isBait(ItemStack item){
        Set<String> baits = plugin.baitFile.getConfig().getConfigurationSection("bait").getKeys(false);
        for (String id : baits){
            if (plugin.checkLore(item, "fp:" + id)) return true;
        }
        return false;
    }







    public ItemStack getItem(){
        return item;
    }
    public Inventory getInventory(){ return inventory; }

    public Size getSize(){ return size;}
    public BagType getType(){ return type; }

    public Player getViewer(){ return viewer; }
    public void setViewer(Player viewer){
       // if (this.viewer != null) this.viewer.closeInventory();
        this.viewer = viewer;
    }

    public boolean addItem(ItemStack item){
        if (inventory == null) loadInventory();
        HashMap<Integer,ItemStack> add = inventory.addItem(item);
        resetInventory();
        return add.isEmpty();

    }

    public boolean removeItem(String loreId){
        if (inventory == null) loadInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = contents.length-1; i >= 0; i--){
            ItemStack item = contents[i];
            if (item == null) continue;
            if (plugin.checkLore(item, loreId)) {
                inventory.removeItem(item);
                resetInventory();
                return true;
            }
        }
        resetInventory();
        return false;
    }
    public boolean removeItem(UUID uuid){
        if (inventory == null) loadInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = contents.length-1; i >= 0; i--){
            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(BaitItem.BAIT_TAG,PersistentDataType.STRING) && container.get(BaitItem.BAIT_TAG,PersistentDataType.STRING).equalsIgnoreCase(uuid.toString())) {
                inventory.removeItem(item);
                resetInventory();
                return true;
            }
        }
        resetInventory();
        return false;
    }


    public void setAmount(String loreId, int amount){
        if (inventory == null) loadInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = contents.length-1; i>=0; i--){
            ItemStack item = contents[i];
            if (item == null) continue;
            if (plugin.checkLore(item, loreId)) {
                item.setAmount(amount);
                resetInventory();
                return;
            }
        }
        resetInventory();
    }

    public boolean replace(ItemStack original, ItemStack replacer){
        if (inventory == null) loadInventory();
        if (!inventory.contains(original)) return false;
        boolean match = itemUUID(original).equals(itemUUID(replacer));
        if (match){
            inventory.removeItem(original);
            inventory.addItem(replacer);
            return true;
        }else return false;

    }

    private UUID itemUUID(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(BaitItem.BAIT_TAG,PersistentDataType.STRING)) return null;
        return UUID.fromString(container.get(BaitItem.BAIT_TAG,PersistentDataType.STRING));
    }





    public enum Size{
        SMALL(27, "Small "),
        MEDIUM(36, "Medium "),
        LARGE(54, "Large ");

        private Size(int slots, String title){
            this.slots = slots;
            this.title = title;
        }
        private final int slots;
        private final String title;

        public int slots(){ return slots; }
        public String title(){ return title; }
    }

    public enum BagType{
        NONE("", ChatColor.GOLD),
        FISH("Fish ",ChatColor.DARK_AQUA),
        BAIT("Bait ",ChatColor.GOLD);

        private BagType(String title, ChatColor color){
            this.title = title;
            this.color = color;
        }
        private final String title;
        private final ChatColor color;
        public String title(){ return title; }
        public ChatColor color(){return color; }
    }

    public boolean contains(ItemStack item){
        resetInventory();
        loadInventory();
        boolean contains = inventory.contains(item);
        resetInventory();
        return contains;
    }

}
