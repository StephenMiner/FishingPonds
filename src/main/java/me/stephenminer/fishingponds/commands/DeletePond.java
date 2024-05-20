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

public class DeletePond implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;
    public DeletePond(FishingPonds plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("fp.commands.delete")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (args.length < 1){
            sender.sendMessage(ChatColor.RED + "You need to say what region you'd like to delete");
            return false;
        }
        String id = args[0];
        Region region = fromId(id);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "The region with id " + id + " does not exist!");
            return false;
        }
        region.delete();
        sender.sendMessage(ChatColor.GREEN + "Deleted region " + id);
        return true;
    }


    public Region fromId(String id){
        return Region.regions.stream()
                .filter(region -> region.getId().equalsIgnoreCase(id))
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
        return plugin.filter(Region.regions.stream().map(Region::getId).collect(Collectors.toList()),match);
    }




}
