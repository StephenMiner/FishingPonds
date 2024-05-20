package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PondSpawn implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;

    public PondSpawn(FishingPonds plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.hasPermission("fp.commands.spawn")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            if (args.length < 1){
                player.sendMessage(ChatColor.RED + "You must say what region you want to set to spawn of");
                return false;
            }
            String id = args[0];
            Region region = fromId(id);
            if (region == null){
                player.sendMessage(ChatColor.RED + "There is no region with id " + id);
                return false;
            }
            region.setSpawn(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Set spawn location for pond");
            return true;
        }else sender.sendMessage(ChatColor.RED + "Only players can use this command!");
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
        return plugin.filter(Region.regions.stream().map(Region::getId).collect(Collectors.toList()), match);
    }


}
