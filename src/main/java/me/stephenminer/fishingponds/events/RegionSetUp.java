package me.stephenminer.fishingponds.events;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.commands.RedefinePond;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RegionSetUp implements Listener {
    private final FishingPonds plugin;
    private final HashMap<UUID, Location> loc1s,loc2s;
    private final List<UUID> canName;
    public RegionSetUp(FishingPonds plugin){
        this.plugin = plugin;
        this.loc1s = new HashMap<>();
        this.loc2s = new HashMap<>();
        this.canName = new ArrayList<>();
    }


    @EventHandler
    public void setPositions(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (!plugin.checkLore(item, "fp:pond-wand")) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        switch (event.getAction()){
            case LEFT_CLICK_BLOCK:
                loc1s.put(uuid, Objects.requireNonNull(event.getClickedBlock()).getLocation());
                player.sendMessage(ChatColor.GREEN + "set position 1");
                break;
            case LEFT_CLICK_AIR:
                loc1s.put(uuid, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "set position 1");
                break;
            case RIGHT_CLICK_BLOCK:
                loc2s.put(uuid, Objects.requireNonNull(event.getClickedBlock()).getLocation());
                player.sendMessage(ChatColor.GREEN + "set position 2");
                break;
            case RIGHT_CLICK_AIR:
                loc2s.put(uuid, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "set position 2");
                break;
        }

        if (loc1s.containsKey(uuid) && loc2s.containsKey(uuid)){
            if (RedefinePond.resizing.containsKey(uuid)){
                Region region = getRegion(RedefinePond.resizing.get(uuid));
                if (region == null){
                    player.sendMessage(ChatColor.RED + "Failed to resize region!");
                    return;
                }else{
                    region.setLoc1(loc1s.get(uuid));
                    region.setLoc2(loc2s.get(uuid));
                    player.sendMessage(ChatColor.GREEN + "Resized region!");
                    loc1s.remove(uuid);
                    loc2s.remove(uuid);
                }
                RedefinePond.resizing.remove(uuid);
                return;
            }
            canName.add(uuid);
            player.sendMessage(ChatColor.GOLD + "Please type the name of your region into chat!");
        }
    }

    private Region getRegion(String id){
        for (Region region : Region.regions){
            if (region.getId().equalsIgnoreCase(id)) return region;
        }
        return null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!canName.contains(uuid)) return;
        event.setCancelled(true);
        String msg = ChatColor.stripColor(event.getMessage()).replace(' ','_').toLowerCase();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
            Location loc1 = loc1s.get(uuid);
            Location loc2 = loc2s.get(uuid);
            Region region = new Region(msg,loc1,loc2);
            region.save();
            loc1s.remove(uuid);
            loc2s.remove(uuid);
            canName.remove(uuid);
        }, 0);

    }
}
