package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.stream.Collectors;

public class WarpPond implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;
    public WarpPond(FishingPonds plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            int size = args.length;
            if (size < 1){
                player.sendMessage(ChatColor.RED + "You need to say the region you'd like to warp to!");
                return false;
            }
            String id = args[0];
            Region region = fromId(id);
            if (region == null){
                player.sendMessage(ChatColor.RED + "Region with id " + id + " doesn't exist!");
                return false;
            }
            if (!region.canWarp(player)){
                player.sendMessage(ChatColor.RED + "You have not unlocked this pond to warp to yet!");
                return false;
            }
            region.warpPlayer(player);
            return true;
        }else sender.sendMessage(ChatColor.RED + "Only players can use this command");
        return false;
    }

    private Region fromId(String id){
        return Region.regions.stream()
                .filter(region->region.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return regionIds(args[0]);
        return null;
    }


    private List<String> regionIds(String match){
        return Region.regions.stream().map(Region::getId).collect(Collectors.toList());
    }
}
