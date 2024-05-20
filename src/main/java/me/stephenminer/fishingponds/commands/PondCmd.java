package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.Region;
import me.stephenminer.fishingponds.region.RegionHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PondCmd implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;
    public PondCmd(){
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.hasPermission("fp.commands.ponds")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            int size = args.length;
            if (size < 1){
                player.sendMessage(ChatColor.RED + "You must input the region you wish to edit!");
                return false;
            }
            String regionId = args[0];
            Region region = fromId(regionId);
            if (region == null){
                player.sendMessage(ChatColor.RED + regionId + " is not a valid region id!");
                return false;
            }
            if (size >= 3){
                String sub = args[1];
                if (sub.equalsIgnoreCase("needs-unlock")){
                    region.setNeedUnlock(Boolean.parseBoolean(args[2]));
                    region.save();
                    player.sendMessage(ChatColor.GREEN + "Set unlock status for region");
                    return true;
                }
            }

            RegionHelper helper = new RegionHelper(player,region);
            RegionHelper.helpers.put(player.getUniqueId(),helper);
            return true;
        }
        return false;
    }

    private Region fromId(String id){
        for (Region region : Region.regions){
            if (region.getId().equalsIgnoreCase(id)) return region;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return regionList(args[0]);
        if (size == 2) return subs();
        if (size == 3) return bools();
        return null;
    }


    private List<String> regionList(String match){
        return plugin.filter(Region.regions.stream().map(Region::getId).collect(Collectors.toList()), match);
    }

    private List<String> subs(){
        List<String> subs = new ArrayList<>();
        subs.add("needs-unlock");
        return subs;
    }
    private List<String> bools(){
        List<String> bools = new ArrayList<>();
        bools.add("true");
        bools.add("false");
        return bools;
    }
}
