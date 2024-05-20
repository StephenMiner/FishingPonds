package me.stephenminer.fishingponds.region;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.stream.Collectors;

public class Region {
    public static List<Region> regions = new ArrayList<>();
    private final FishingPonds plugin;
    private final String id;
    private Location loc1,loc2, spawn;
    private Pond pond;

    private Set<UUID> unlocked;
    private boolean needUnlock;
    private Set<UUID> onBar;
    private BossBar progress;

    private float modifier;


    private HashMap<UUID, Boolean> showBorder;

    public Region(String id, Location loc1, Location loc2){
        this.plugin = FishingPonds.getPlugin(FishingPonds.class);
        this.id = id;
        this.loc1 = loc1.getBlock().getLocation().clone().add(0.5,0.5,0.5);
        this.loc2 = loc2.getBlock().getLocation().clone().add(0.5,0.5,0.5);
        this.unlocked = new HashSet<>();
        pond = new Pond(this);
        showBorder = new HashMap<>();
        onBar = new HashSet<>();
        pond.loadPotentialDrops();
        Region.regions.add(this);
        progress = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
        modifier = 1;
        fishWithFriends();
    }





    public BoundingBox getBounds(){
        return BoundingBox.of(loc1,loc2);
    }


    public boolean inBounds(Location loc){
        BoundingBox blockBox = loc.getBlock().getBoundingBox();
        return getBounds().contains(loc.toVector());
    }

    public boolean inBounds(BoundingBox box){
        return getBounds().overlaps(box);
    }

    public boolean tryBreak(Player player, Block block){
        if (player.hasPermission("fp.regions.edit")) return true;
        else return  !inBounds(block.getBoundingBox());
    }

    public void save(){
        String path = "regions." + id;
        plugin.regionFile.getConfig().set(path + ".loc1",plugin.fromBLoc(loc1));
        plugin.regionFile.getConfig().set(path + ".loc2", plugin.fromBLoc(loc2));
        plugin.regionFile.getConfig().set(path + ".need-unlock",needUnlock);
        if (spawn != null) plugin.regionFile.getConfig().set(path + ".spawn",plugin.fromLoc(spawn));
        plugin.regionFile.getConfig().set(path + ".unlocked",unlocked.stream().map(UUID::toString).collect(Collectors.toList()));
        pond.save();
        plugin.regionFile.saveConfig();
    }

    public void delete(){
        plugin.regionFile.getConfig().set("regions." + id, null);
        plugin.regionFile.saveConfig();
        Region.regions.remove(this);
    }


    public void warpPlayer(Player player){
        if (spawn != null){
            player.teleport(spawn);
        }
    }

    public void showBorder(Player player){
        showBorder.put(player.getUniqueId(),true);
        Set<Location> locs = loadBorderLocs();
        new BukkitRunnable(){
            @Override
            public void run(){
                if (!showBorder.containsKey(player.getUniqueId()) || !showBorder.get(player.getUniqueId())){
                    this.cancel();
                    return;
                }
                for (Location loc : locs){
                    player.spawnParticle(Particle.VILLAGER_HAPPY,loc.clone().add(0.5,0.5,0.5),1);
                }
            }
        }.runTaskTimer(plugin, 1, 10);
    }

    private Set<Location> loadBorderLocs(){
        Set<Location> locs = new HashSet<>();
        World world = loc1.getWorld();
        int minX = Math.min( loc1.getBlockX(),loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(),loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        for (int y = minY; y <= maxY; y++){
            for (int x = minX; x <= maxX; x++){
                locs.add(world.getBlockAt(x,y,minZ).getLocation());
                locs.add(world.getBlockAt(x,y,maxZ).getLocation());
            }
            for (int z = minZ; z <= maxZ; z++){
                locs.add(world.getBlockAt(maxX,y,z).getLocation());
                locs.add(world.getBlockAt(minX,y,z).getLocation());
            }
        }
        return locs;
    }

    public Set<Player> players(){
        BoundingBox bounds = getBounds();
        World world = loc1.getWorld();
        Set<Player> players = new HashSet<>();
        world.getNearbyEntities(getBounds()).stream().filter(entity-> entity instanceof Player).forEach((Entity e)->players.add((Player) e));
        return players;
    }

    public void fishWithFriends(){
        final Region instance = this;
        new BukkitRunnable(){
            @Override
            public void run(){
                if (!Region.regions.contains(instance)){
                    this.cancel();
                    progress.removeAll();
                    return;
                }
                Set<Player> players = players();
                List<Player> onBar = progress.getPlayers();
                for (int i = onBar.size()-1; i >= 0; i--){
                    Player player = onBar.get(i);
                    if (!players.contains(player)) progress.removePlayer(player);
                }
                players.forEach(( player -> {
                    if (!progress.getPlayers().contains(player))progress.addPlayer(player);
                }));
                float fill = Math.min(1, players.size()/5f);
                progress.setProgress(fill);
                modifier = (float) Math.min(1 + (players.size()-1) *0.5, 5);
                progress.setTitle(players.size() + " players | x" + modifier + " fish-speed");
            }
        }.runTaskTimer(plugin,1, 5);

    }



    public float getModifier(){ return modifier; }
    public String getId(){ return id; }
    public Location getLoc1(){ return loc1; }
    public Location getLoc2(){ return loc2; }
    public Location getSpawn(){ return spawn; }
    public Pond pond(){ return pond; }
    public Set<UUID> getUnlocked(){ return unlocked; }
    public boolean needsUnlock(){ return needUnlock; }
    public void setUnlocked(Set<UUID> unlocked){ this.unlocked = unlocked;}

    public void setUnlocked(Player player, boolean unlock){
        if (unlock) unlocked.add(player.getUniqueId());
        else unlocked.remove(player.getUniqueId());
    }

    public boolean canWarp(Player player){ return !needUnlock || unlocked.contains(player.getUniqueId()); }

    public World getWorld(){ return loc1.getWorld(); }

    public void setLoc1(Location loc1){ this.loc1 = loc1.getBlock().getLocation().clone().add(0.5,0.5,0.5); }
    public void setLoc2(Location loc2){ this.loc2 = loc2.getBlock().getLocation().clone().add(0.5,0.5,0.5); }
    public void setSpawn(Location spawn){ this.spawn = spawn; }

    public boolean isShowing(Player player){
        return showBorder.getOrDefault(player.getUniqueId(), false);
    }
    public void setShowing(Player player, boolean showing){
        showBorder.put(player.getUniqueId(),showing);
    }

    public void setNeedUnlock(boolean need){this.needUnlock = need; }



}
