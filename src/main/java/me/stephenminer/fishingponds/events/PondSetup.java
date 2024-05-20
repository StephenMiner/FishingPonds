package me.stephenminer.fishingponds.events;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.RegionHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class PondSetup implements Listener {
    private final FishingPonds plugin;
    public PondSetup(FishingPonds plugin){
        this.plugin = plugin;
    }




    @EventHandler
    public void mainMenuClicks(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if (!RegionHelper.helpers.containsKey(player.getUniqueId())) return;
        RegionHelper helper = RegionHelper.helpers.get(player.getUniqueId());
        if (helper.getStatus() == RegionHelper.InvType.MAIN && event.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Main Menu")){
            int slot = event.getSlot();
            if (slot == 5) {
                player.closeInventory();
                player.openInventory(helper.fishMenu());
            }else if (slot == 6){
                player.closeInventory();
                player.openInventory(helper.junkMenu());
            }else if (slot == 7){
                player.closeInventory();
                player.openInventory(helper.treasureMenu());
            }
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void menuClicks(InventoryClickEvent event){
        Player player = (Player)  event.getWhoClicked();
        if (!RegionHelper.helpers.containsKey(player.getUniqueId())) return;
        RegionHelper helper = RegionHelper.helpers.get(player.getUniqueId());
        if (helper.getStatus() == RegionHelper.InvType.MAIN) return;
        if (helper.getStatus() == RegionHelper.InvType.CHANCE && event.getSlotType() == InventoryType.SlotType.RESULT && event.getInventory() instanceof AnvilInventory){
            event.setCancelled(true);
            player.closeInventory();
            player.openInventory(helper.mainMenu());
        }else if (event.getView().getTitle().contains(" Drops")){
            if (event.getSlot() >= 45){
                event.setCancelled(true);
                return;
            }
            if (event.getClick() == ClickType.RIGHT) {
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    player.closeInventory();
                    helper.chance(item);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if (!RegionHelper.helpers.containsKey(player.getUniqueId())) return;
        RegionHelper helper = RegionHelper.helpers.get(player.getUniqueId());
        if (helper.getStatus() != RegionHelper.InvType.MAIN){
            helper.onClose();
        }

    }

    @EventHandler
    public void listenChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if (!RegionHelper.helpers.containsKey(player.getUniqueId())) return;
        RegionHelper helper = RegionHelper.helpers.get(player.getUniqueId());
        String msg = event.getMessage();
        try{
            helper.onChat(Integer.parseInt(ChatColor.stripColor(msg)));
            player.sendMessage(ChatColor.GREEN + "Set new weight for your item");
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                player.openInventory(helper.mainMenu());
            },1);

        }catch (Exception e){
            player.sendMessage(ChatColor.RED + "Try again, you must input a whole number only");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        RegionHelper.helpers.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event){
        RegionHelper.helpers.remove(event.getPlayer().getUniqueId());
    }
}
