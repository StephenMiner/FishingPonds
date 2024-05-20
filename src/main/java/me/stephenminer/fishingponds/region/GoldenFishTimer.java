package me.stephenminer.fishingponds.region;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class GoldenFishTimer {
    private final FishingPonds plugin;
    private int countTo;
    private int count;
    private ItemStack item;

    public GoldenFishTimer(FishingPonds plugin){
        this.plugin = plugin;
    }





    public int generateTime(){
        int min = plugin.minTime();
        int max = plugin.maxTime();
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    public void timer(){
        countTo = generateTime();
        count = 0;
        new BukkitRunnable(){
            @Override
            public void run(){
                if (item != null) return;
                if (count >= countTo){
                    item = plugin.getGoldFish();
                    count = 0;
                    Bukkit.broadcastMessage(ChatColor.GOLD + "A Shiny Fish has appeared!");
                    /*
                    for (Region region : Region.regions){
                        region.players().forEach(player -> player.sendMessage());
                    }

                     */
                }
                count++;
            }
        }.runTaskTimer(plugin,1, 1);
    }

    public void removeFish(){item = null;}



    public int ticksLeft(){
        return countTo-count;
    }
    public int getTime(){ return countTo; }

    public boolean canFish(){ return item != null; }
    public ItemStack fish(){ return item; }
}
