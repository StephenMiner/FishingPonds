package me.stephenminer.fishingponds.events;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.items.Backpack;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class BagEvents implements Listener {
    private final FishingPonds plugin;

    public BagEvents(FishingPonds plugin){
        this.plugin = plugin;
    }


    @EventHandler
    public void openBag(PlayerInteractEvent event){
        if (event.getAction() == Action.PHYSICAL) return;
        if (!event.hasItem()) return;
        Player player = event.getPlayer();
        InventoryView view = player.getOpenInventory();
        if (view.getType() != InventoryType.CRAFTING && view.getType() != InventoryType.CREATIVE) return;
        ItemStack item = event.getItem();
        Backpack bag = getBag(item);

        if (bag == null) {
            Backpack loaded = loadBag(item);
            if (loaded == null) return;
            else {
                plugin.getLogger().info("Loaded new bag object");
                loaded.loadInventory();
                loaded.setViewer(player);
                player.openInventory(loaded.getInventory());
            }
            return;
        }
        bag.loadInventory();
        bag.setViewer(player);
        player.openInventory(bag.getInventory());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        ItemStack item = event.getCurrentItem();
        if (event.getClickedInventory() == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (item != null) {
            Backpack fromItem = getBag(item);
            if (fromItem != null) {
                if (player.getOpenInventory().getTopInventory().equals(fromItem.getInventory())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        Backpack bag = fromInv(player.getOpenInventory().getTopInventory());
        if (bag != null) {
            if (player.getOpenInventory().getTopInventory().equals(bag.getInventory())) {
                if (event.getClick()==ClickType.NUMBER_KEY){
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot use number keys in this inventory!");
                    return;
                }
                if (event.getClickedInventory().equals(bag.getInventory()) || item == null) return;
                boolean canAdd = bag.canAdd(item);
                if (!canAdd) event.setCancelled(true);
            }else event.setCancelled(true);

        }
    }

    @EventHandler
    public void closeBag(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        for (Backpack bag : Backpack.BY_UUID.values()){
            if (bag.getViewer() != null && bag.getViewer().equals(player)){
                bag.resetInventory();
                bag.setViewer(null);
            }
        }
    }



    private boolean needToLoad(ItemStack item){
        if (item == null || item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(Backpack.BAG_KEY,PersistentDataType.STRING)){
            String suuid = container.get(Backpack.BAG_KEY,PersistentDataType.STRING);
            if (suuid != null || suuid.isEmpty()) return false;
            UUID uuid = UUID.fromString(suuid);
            return plugin.bagFile.getConfig().contains("bags." + uuid);
        }
        return false;
    }

    private Backpack getBag(ItemStack item){
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(Backpack.BAG_KEY, PersistentDataType.STRING)) {
            String suuid = container.get(Backpack.BAG_KEY,PersistentDataType.STRING);
            if (suuid == null || suuid.isEmpty()) return null;
            UUID uuid = UUID.fromString(suuid);
            return Backpack.BY_UUID.getOrDefault(uuid,null);
        }return null;
    }

    private Backpack fromInv(Inventory inv){
        if (inv == null) return null;
        for (Backpack bag : Backpack.BY_UUID.values()){
            if (inv.equals(bag.getInventory())) return bag;
        }
        return null;
    }


    private Backpack loadBag(ItemStack item){
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(Backpack.BAG_KEY, PersistentDataType.STRING)){
            String suuid = container.get(Backpack.BAG_KEY, PersistentDataType.STRING);
            if (suuid == null || suuid.isEmpty()) return null;
            UUID uuid = UUID.fromString(suuid);
            if (!plugin.bagFile.getConfig().contains("bags." + uuid + ".size")) return null;
            String sSize = plugin.bagFile.getConfig().getString("bags." + uuid + ".size");
            if (sSize == null) return null;
            Backpack.Size size = Backpack.Size.valueOf(plugin.bagFile.getConfig().getString("bags." + uuid + ".size"));
            String sType = plugin.bagFile.getConfig().getString("bags."+ uuid + ".type");
            if (sType == null) return null;
            Backpack.BagType type = Backpack.BagType.valueOf(plugin.bagFile.getConfig().getString("bags." + uuid + ".type"));
            return new Backpack(size, type, uuid, item);
        }
        return null;
    }
}
