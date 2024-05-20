package me.stephenminer.fishingponds.events;

import me.stephenminer.fishingponds.FishingPonds;

import me.stephenminer.fishingponds.region.Region;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

public class PondProtection implements Listener {
    private final FishingPonds plugin;
    public PondProtection(){
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        boolean cancel = guardRegion(event.getPlayer(),event.getBlock());
        if (cancel) event.setCancelled(true);
    }

    private boolean guardRegion(Player player, Block block){
        for (Region region : Region.regions){
            if (region.inBounds(block.getBoundingBox())) {
                if (!region.tryBreak(player,block))
                    return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        for (Region region : Region.regions){
            if (region.inBounds(event.getBlock().getBoundingBox()))
                if (!region.tryBreak(event.getPlayer(), event.getBlock()))
                    event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event){
        for (Region region : Region.regions){
            if (region.inBounds(event.getBlock().getBoundingBox())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void fromTo(BlockFromToEvent event){
        for (Region region : Region.regions) {
            if (!region.getWorld().equals(event.getBlock().getWorld())) continue;
            if (!region.getBounds().overlaps(BoundingBox.of(event.getBlock())) && region.getBounds().overlaps(BoundingBox.of(event.getToBlock()))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent event){
        for (Region region : Region.regions){
            if (region.inBounds(event.getBlock().getBoundingBox())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockExplode(BlockExplodeEvent event){
        for (Region region : Region.regions) {
            for (Block block : event.blockList()) {
                if (region.inBounds(block.getBoundingBox())){
                    event.blockList().clear();
                    return;
                }
            }
        }
    }
    @EventHandler
    public void entityExplode(EntityExplodeEvent event){
        for (Region region : Region.regions) {
            for (Block block : event.blockList()) {
                if (region.inBounds(block.getBoundingBox())){
                    event.blockList().clear();
                    return;
                }
            }
        }
    }

}
