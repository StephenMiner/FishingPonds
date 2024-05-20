package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UnlockPond implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;
    public UnlockPond(FishingPonds plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("fp.commands.unlock")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        int size = args.length;
        if (size < 3){
            sender.sendMessage(ChatColor.RED + "Not enough arguments, you must input the region to unlock and the player to unlock it for and a true/false value to unlock or revoke unlock status");
            return false;
        }
        String id = ChatColor.stripColor(args[0]);
        Region region = fromId(id);
        boolean unlock = Boolean.parseBoolean(args[2]);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + id + " is not a valid region!");
            return false;
        }
        try{
            Player player = Bukkit.getPlayerExact(ChatColor.stripColor(args[1]));
            if (player == null){
                sender.sendMessage(ChatColor.RED + "Player isn't online or doesn't exist!");
                return false;
            }
            region.setUnlocked(player, unlock);
            player.sendMessage(ChatColor.GOLD + "Updated unlock status for pond " + id + " for player " + args[1] + " to " + args[2]);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    private Region fromId(String str){
        return Region.regions.stream()
                .filter(region->region.getId().equalsIgnoreCase(str))
                .findFirst()
                .orElse(null);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return regionIds(args[0]);
        if (size == 2) return playerNames(args[1]);
        if (size == 3) return boolVals();
        return null;
    }



    private List<String> regionIds(String match){
        return plugin.filter(Region.regions.stream().map(Region::getId).collect(Collectors.toList()), match);
    }

    private List<String> playerNames(String match){
        return plugin.filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), match);
    }

    private List<String> boolVals(){
        List<String> vals = new ArrayList<>();
        vals.add("true");
        vals.add("false");
        return vals;
    }
}
