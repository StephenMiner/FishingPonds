package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RedefinePond implements CommandExecutor, TabCompleter {
    public static HashMap<UUID, String> resizing = new HashMap<>();
    private final FishingPonds plugin;
    public RedefinePond(FishingPonds plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.hasPermission("fp.commands.resize")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            if (args.length < 1){
                player.sendMessage(ChatColor.RED + "You must input the region to resize!");
                return false;
            }
            String id = args[0];
            resizing.put(player.getUniqueId(),id);
            player.sendMessage(ChatColor.GREEN + "Use the pond-wand to reset the locations for your region");
            return true;
        }else sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
        return false;
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
