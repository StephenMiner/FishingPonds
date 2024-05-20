package me.stephenminer.fishingponds.events;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.items.Backpack;
import me.stephenminer.fishingponds.items.BaitItem;
import me.stephenminer.fishingponds.region.FishHelper;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FishingEvents implements Listener {
    private final FishingPonds plugin;
    private final HashMap<UUID, FishHelper.Pair<ItemStack,Backpack>> baitUse;
    public FishingEvents(){
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
        baitUse = new HashMap<>();
    }

    @EventHandler
    public void onReelIn(PlayerFishEvent event){

        if (event.getCaught() instanceof Item) {
            Item item = (Item) event.getCaught();
            Region regionIn = regionIn(event.getHook());
            if (regionIn == null) {
                item.remove();
                return;
            }
            Player player = event.getPlayer();
            FishHelper helper = new FishHelper(regionIn);
            boolean goldFish = getGoldenFish();
            ItemStack drop;
            if (goldFish){
                drop = plugin.timer.fish();
                plugin.timer.removeFish();
            }else drop = helper.reelIn(event.getPlayer());
            ItemMeta meta = drop.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.BLACK + "fp:fished");
            meta.setLore(lore);
            drop.setItemMeta(meta);
            ItemStack add = helper.depositFish(player,drop);
            if (add == null) {
                item.remove();
                player.sendMessage(ChatColor.GOLD + meta.getDisplayName() + " added to your bags");
            }
            else item.setItemStack(drop);
            if (baitUse.containsKey(player.getUniqueId())){
                updateBait(player,baitUse.get(player.getUniqueId()));
                baitUse.remove(player.getUniqueId());
            }
        }
    }

    private boolean getGoldenFish(){
        ItemStack available = plugin.timer.fish();
        if (available == null) return false;
        double rawChance = plugin.getChance();
        int chance = (int) rawChance * 10;
        int roll = ThreadLocalRandom.current().nextInt(1000);
        return roll <= chance;
    }

    private void updateBait(Player player, FishHelper.Pair<ItemStack,Backpack> data){
        if (data == null) return;
        ItemStack item = data.entry1();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
        ItemStack copy = item.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++){
            String entry = lore.get(i);
            int uses = parseUses(entry);
            if (uses < 0) continue;
            int newUses = uses-1;
            if (newUses > 0){
                entry = entry.replace(""+uses, ""+newUses);
                lore.set(i, entry);
            }else item.setAmount(0);
            break;
        }
        if (item.getAmount() > 0) {
            meta.setLore(lore);
            item.setItemMeta(meta);
            if (data.entry2() != null)
                data.entry2().replace(copy, item);
        }else if(data.entry2()!=null){
            UUID uuid = getUUID(item);
            if (uuid != null)
                data.entry2().removeItem(uuid);
        }
    }
    private UUID getUUID(ItemStack item){
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(BaitItem.BAIT_TAG, PersistentDataType.STRING)) return UUID.fromString(container.get(BaitItem.BAIT_TAG,PersistentDataType.STRING));
        return null;
    }
    private int parseUses(String line){
        line = ChatColor.stripColor(line);
        String num = line.replace("Uses: ", "");
        try {
            return Integer.parseInt(num);
        }catch (Exception ignored){}
        return -1;
    }

    @EventHandler
    public void failFish(PlayerFishEvent event){
        if (event.getState() == PlayerFishEvent.State.REEL_IN){
            Player player = event.getPlayer();
            baitUse.remove(player.getUniqueId());
        }

    }

    @EventHandler
    public void shootHook(PlayerFishEvent event){
        if (event.getState() == PlayerFishEvent.State.FISHING){
            FishHook hook = event.getHook();
            Player player = event.getPlayer();

            new BukkitRunnable(){
                int min = hook.getMinWaitTime();
                int max = hook.getMaxWaitTime();
                @Override
                public void run(){
                    if (hook.isDead()) {
                        this.cancel();
                    }
                    if (hook.isInWater()){
                        Region region = regionIn(hook);
                        if (region == null) return;
                        FishHelper helper = new FishHelper(region);
                        FishHelper.Pair<Float, FishHelper.Pair<ItemStack, Backpack>> data = helper.highestMultiplier(player);
                        float divider = data.entry1() * region.getModifier();
                        min = (int) (min / divider);
                        max = (int) (max / divider);
                     //   hook.setMaxWaitTime(Math.max(1,max));
                      //  hook.setMinWaitTime(Math.max(1,min));
                        hook.setMaxWaitTime(max);
                        hook.setMinWaitTime(min);
                        baitUse.put(player.getUniqueId(),data.entry2());
                        this.cancel();
                        return;
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);

        }
    }

    private Region regionIn(Entity entity){
        if (entity == null) return null;
        for (Region region : Region.regions){
            if (region.inBounds(entity.getLocation())) return region;
        }
        return null;
    }
}
