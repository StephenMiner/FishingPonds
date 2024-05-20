package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.items.BaitItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GiveBait implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;

    public GiveBait(FishingPonds plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("fp.commands.bait")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        int size = args.length;
        if (size >= 2){
            String baitId = ChatColor.stripColor(args[0]);
            if (!validId(baitId)){
                sender.sendMessage(ChatColor.RED + "The input id is not a real bait item!");
                return false;
            }
            try{
                Player player = Bukkit.getPlayerExact(ChatColor.stripColor(args[1]));
                int amount = 1;
                if (size >= 3)
                    amount = Math.max(amount,Integer.parseInt(ChatColor.stripColor(args[2])));
                for (int i = 0; i < amount; i++) {
                    ItemStack item = new BaitItem(baitId).loadItem();
                    HashMap<Integer, ItemStack> drop = player.getInventory().addItem(item);
                    World world = player.getWorld();
                    drop.values().stream().forEach(entry -> world.dropItem(player.getLocation(), entry));
                }
                sender.sendMessage(ChatColor.GREEN + "Gave player their items!");
                return true;
            }catch (Exception e){ e.printStackTrace(); }
        }else sender.sendMessage(ChatColor.RED + "Not enough arguments! You must say the bait item you wish to give, the player to give it to, and optionally how much you wish to give!");
        return false;
    }


    private boolean validId(String id){
        return plugin.baitFile.getConfig().contains("bait." + id);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return ids(args[0]);
        if (size == 2) return playerNames(args[1]);
        if (size == 3) return num();
        return null;
    }


    public List<String> ids(String match){
        Set<String> ids = plugin.baitFile.getConfig().getConfigurationSection("bait").getKeys(false);
        return plugin.filter(ids, match);
    }

    public List<String> playerNames(String match){
        List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        return plugin.filter(playerNames, match);
    }

    public List<String> num(){
        List<String> out = new ArrayList<>();
        out.add("[#-to-give]");
        return out;
    }
}
